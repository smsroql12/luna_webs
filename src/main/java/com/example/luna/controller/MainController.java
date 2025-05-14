package com.example.luna.controller;

import com.example.luna.entity.*;
import com.example.luna.repository.BannerRepository;
import com.example.luna.repository.ProductRepository;
import com.example.luna.repository.TableRepository;
import com.example.luna.repository.WishlistRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class MainController {
    private final BannerRepository bannerRepository;
    private final TableRepository tableRepository;
    private final ProductRepository productRepository;
    private final WishlistRepository wishlistRepository;

    @GetMapping("/")
    public String getMain(Model model) {
        List<BannerEntity> bannerList = bannerRepository.findByBactiveTrueOrderByBindexAsc();
        model.addAttribute("bannerList", bannerList);

        List<TableEntity> boardList = tableRepository.findByActiveOrderByTbindexAsc(1);
        model.addAttribute("boardList", boardList);

        return "index";
    }

    @GetMapping("/view")
    public String viewProduct(@RequestParam("id") Long id, HttpSession session, Model model) {
        Optional<Product> productOpt = productRepository.findById(id);

        if (productOpt.isPresent()) {
            String name = tableRepository.findNameById(Long.valueOf(productOpt.get().getCategory()));
            model.addAttribute("categoryName", name);
            model.addAttribute("product", productOpt.get());
            SiteUser user = (SiteUser) session.getAttribute("user");
            boolean isWished = false;
            if (user != null) {
                isWished = wishlistRepository.findByEmailAndProductid(user.getEmail(), id.intValue()).isPresent();
            }
            model.addAttribute("isWished", isWished);
            return "view";
        } else {
            model.addAttribute("message", "해당하는 상품 정보가 없습니다.");
            model.addAttribute("link", "main");
            return "message"; // 성공 안내 페이지
        }
    }

    @GetMapping("/wishlist")
    public String getWishlist(Model model, HttpSession session,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "8") int size) {
        SiteUser user = (SiteUser) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Wishlist> wishlistPage = wishlistRepository.findByEmailOrderByWishdateDesc(user.getEmail(), pageable);

        List<Product> wishlistProducts = wishlistPage.getContent().stream()
                .map(wish -> productRepository.findById(Long.valueOf(wish.getProductid())).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        model.addAttribute("wishlist", wishlistPage);
        model.addAttribute("products", wishlistProducts);
        return "wishlist";
    }

    @PostMapping("/wishlist/setwishlist")
    @ResponseBody
    public Map<String, Object> toggleWishlist(@RequestParam int productid, HttpSession session) {
        SiteUser user = (SiteUser) session.getAttribute("user");
        Map<String, Object> response = new HashMap<>();

        if (user == null) {
            response.put("status", "unauthorized");
            return response;
        }

        Optional<Wishlist> existing = wishlistRepository.findByEmailAndProductid(user.getEmail(), productid);
        if (existing.isPresent()) {
            wishlistRepository.delete(existing.get());
            response.put("status", "removed");
        } else {
            Wishlist wish = new Wishlist();
            wish.setEmail(user.getEmail());
            wish.setProductid(productid);
            wish.setWishdate(LocalDateTime.now());
            wishlistRepository.save(wish);
            response.put("status", "added");
        }
        return response;
    }
}