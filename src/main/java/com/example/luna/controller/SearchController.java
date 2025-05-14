package com.example.luna.controller;

import com.example.luna.entity.Product;
import com.example.luna.entity.TableEntity;
import com.example.luna.repository.TableRepository;
import com.example.luna.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class SearchController {
    private final ProductService productService;
    private final TableRepository tableRepository;

    public SearchController(ProductService productService, TableRepository tableRepository) {
        this.productService = productService;
        this.tableRepository = tableRepository;
    }

    @GetMapping("/search")
    public String searchProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "8") int size,
            Model model
    ) {
        if (category == null || category.isEmpty()) {
            model.addAttribute("message", "파라미터에 오류가 있습니다.");
            return "message";
        }

        int zeroBasedPage = (page <= 1) ? 0 : page - 1;
        if (productPage == null) {
            model.addAttribute("message", "잘못된 접근입니다.");
            return "message";
        }

        long totalItems = productPage.getTotalElements();

        if (totalItems == 0) {
            model.addAttribute("noResult", true);
            model.addAttribute("blockStart", 1);
            model.addAttribute("blockEnd", 0);
        }
        else {
            int totalPages = productPage.getTotalPages();
            int currentPage = productPage.getNumber() + 1; // 1-based

            int blockSize = 5;
            int currentBlock = (currentPage - 1) / blockSize;
            int blockStart = currentBlock * blockSize + 1;
            int blockEnd = Math.min(blockStart + blockSize - 1, totalPages);

            // 이전/다음 블록의 페이지 계산
            int prevBlockPage = blockStart - 1;
            int nextBlockPage = blockEnd + 1;

            model.addAttribute("noResult", false);
            model.addAttribute("blockStart", blockStart);
            model.addAttribute("blockEnd", blockEnd);
            model.addAttribute("prevBlockPage", prevBlockPage);
            model.addAttribute("nextBlockPage", nextBlockPage);
            model.addAttribute("userPage", currentPage); // 현재 페이지 번호 1-based
        }

        List<TableEntity> boardList = tableRepository.findByActiveOrderByTbindexAsc(1);
        model.addAttribute("boardList", boardList);
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("page", productPage);
        model.addAttribute("count", totalItems);
        model.addAttribute("search", search);
        model.addAttribute("category", category);
        model.addAttribute("selectedCategory", category);
        if(!category.equals("all")) {
            String name = tableRepository.findNameById(Long.parseLong(category));
            model.addAttribute("categoryName", name);
        }
        else {
            model.addAttribute("categoryName", "All Items");
        }

        return "item";
    }

}
