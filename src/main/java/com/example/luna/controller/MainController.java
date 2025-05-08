package com.example.luna.controller;

import com.example.luna.entity.BannerEntity;
import com.example.luna.entity.Product;
import com.example.luna.repository.BannerRepository;
import com.example.luna.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class MainController {
    private final BannerRepository bannerRepository;

    @GetMapping("/")
    public String getMain(Model model) {
        List<BannerEntity> bannerList = bannerRepository.findByBactiveTrueOrderByBindexAsc();
        model.addAttribute("bannerList", bannerList);
        return "index";
    }

    private final ProductRepository productRepository;


    @GetMapping("/view")
    public String viewProduct(@RequestParam("id") Long id, Model model) {
        Optional<Product> productOpt = productRepository.findById(id);

        if (productOpt.isPresent()) {
            model.addAttribute("product", productOpt.get());
            return "view";
        } else {
            model.addAttribute("message", "해당하는 상품 정보가 없습니다.");
            model.addAttribute("link", "main");
            return "message"; // 성공 안내 페이지
        }
    }
}