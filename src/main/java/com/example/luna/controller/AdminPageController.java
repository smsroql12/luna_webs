package com.example.luna.controller;

import com.example.luna.entity.BannerEntity;
import com.example.luna.entity.MainItemEntity;
import com.example.luna.entity.Product;
import com.example.luna.repository.BannerRepository;
import com.example.luna.repository.MainItemRepository;
import com.example.luna.repository.ProductRepository;
import com.example.luna.repository.TableRepository;
import com.example.luna.service.BannerService;
import com.example.luna.service.MainItemService;
import com.example.luna.service.ProductService;
import com.example.luna.service.TableService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminPageController {
    private final BannerRepository bannerRepository;
    private final BannerService bannerService;

    private final TableService tableService;

    private final ProductRepository productRepository;
    private final ProductService productService;

    private final MainItemRepository mainItemRepository;
    private final MainItemService mainitemService;

    @GetMapping("/index")
    public String adminPage() {
        return "admin/index";
    }

    @GetMapping("/mainitems")
    public String adminItems(Model model) {
        List<MainItemEntity> itemList = mainItemRepository.findAll(Sort.by(Sort.Order.asc("mindex")));

        // 각 아이템의 product image를 함께 세팅
        for (MainItemEntity item : itemList) {
            Long itemcode = (long) item.getItemcode(); // itemcode가 product.no
            if (itemcode != null) {
                productRepository.findById(itemcode).ifPresent(product -> {
                    item.setTempName(product.getName());
                    item.setTempImage(product.getImage()); // 임시 필드
                });
            }
        }

        model.addAttribute("itemList", itemList);
        return "admin/mainitem"; // 예시 템플릿명
    }

    @PostMapping("/saveItem")
    public ResponseEntity<?> saveItemData(@RequestParam Map<String, String> allParams) {
        try {
            mainitemService.processMainItemData(allParams);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("저장 실패");
        }
    }

    @GetMapping("/findProduct")
    public ResponseEntity<?> findProductByNo(@RequestParam("no") Long no) {
        Optional<Product> productOpt = productRepository.findById(no);

        if (productOpt.isPresent()) {
            Product p = productOpt.get();

            Map<String, Object> result = new HashMap<>();
            result.put("no", p.getNo());
            result.put("name", p.getName());
            result.put("image", p.getImage());

            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("상품을 찾을 수 없습니다.");
        }
    }

    @GetMapping("/banneredit")
    public String bannerEdit(Model model) {
        List<BannerEntity> bannerList = bannerRepository.findAll(Sort.by(Sort.Order.asc("bindex")));
        model.addAttribute("bannerList", bannerList);
        return "admin/banner";
    }

    @PostMapping("/saveBanner")
    public ResponseEntity<?> saveBannerData(@RequestParam Map<String, String> allParams,
                                            @RequestParam MultiValueMap<String, MultipartFile> fileMap) {
        try {
            bannerService.processBannerData(allParams, fileMap);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("저장 실패");
        }
    }

    @PostMapping("/saveBoards")
    public String saveBoards(@RequestParam("jsonData") String jsonData) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, String>> boardList = mapper.readValue(jsonData, new TypeReference<>() {});
        tableService.saveBoardList(boardList);
        return "redirect:/admin/boardedit";
    }

    @GetMapping("/products")
    public String productList(
            @RequestParam(required = false) String type, // "name" or "code"
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "8") int size,
            @RequestParam(defaultValue = "new") String sort,
            Model model
    ) {
        int zeroBasedPage = (page <= 1) ? 0 : page - 1;
        Page<Product> productPage = productService.searchProductsByType(type, search, zeroBasedPage, size, sort);

        if (productPage == null) {
            model.addAttribute("message", "잘못된 접근입니다.");
            return "message";
        }

        long totalItems = productPage.getTotalElements();

        if (totalItems == 0) {
            model.addAttribute("noResult", true);
            model.addAttribute("blockStart", 1);
            model.addAttribute("blockEnd", 0);
        } else {
            int totalPages = productPage.getTotalPages();
            int currentPage = productPage.getNumber() + 1;

            int blockSize = 5;
            int currentBlock = (currentPage - 1) / blockSize;
            int blockStart = currentBlock * blockSize + 1;
            int blockEnd = Math.min(blockStart + blockSize - 1, totalPages);

            model.addAttribute("noResult", false);
            model.addAttribute("blockStart", blockStart);
            model.addAttribute("blockEnd", blockEnd);
            model.addAttribute("prevBlockPage", blockStart - 1);
            model.addAttribute("nextBlockPage", blockEnd + 1);
            model.addAttribute("userPage", currentPage);
        }

        model.addAttribute("products", productPage.getContent());
        model.addAttribute("page", productPage);
        model.addAttribute("count", totalItems);
        model.addAttribute("search", search);
        model.addAttribute("type", type);

        return "admin/productlist";
    }

}
