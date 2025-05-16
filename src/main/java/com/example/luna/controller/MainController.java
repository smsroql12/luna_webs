package com.example.luna.controller;

import com.example.luna.entity.*;
import com.example.luna.repository.*;
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
    private final MainItemRepository mainItemRepository;
    private final CartRepository cartRepository;

    @GetMapping("/")
    public String getMain(Model model) {
        List<BannerEntity> bannerList = bannerRepository.findByBactiveTrueOrderByBindexAsc();
        model.addAttribute("bannerList", bannerList);

        List<TableEntity> boardList = tableRepository.findByActiveOrderByTbindexAsc(1);
        model.addAttribute("boardList", boardList);

        List<MainItemEntity> itemList = mainItemRepository.findByActiveOrderByMindexAsc(true);

        List<Product> productList = itemList.stream()
                .map(item -> productRepository.findById((long) item.getItemcode()).orElse(null))
                .filter(Objects::nonNull)
                .toList();

        model.addAttribute("products", productList);

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

            //장바구니
            boolean isInCart = false;
            if (user != null) {
                isInCart = cartRepository.findByEmailAndProductid(user.getEmail(), id.intValue()).isPresent();
            }

            model.addAttribute("isInCart", isInCart);

            return "view";
        } else {
            model.addAttribute("message", "해당하는 상품 정보가 없습니다.");
            model.addAttribute("link", "main");
            return "message"; // 성공 안내 페이지
        }
    }

    @GetMapping("/wishlist")
    public String getWishlist(Model model, HttpSession session,
                              @RequestParam(defaultValue = "1") int page,
                              @RequestParam(defaultValue = "8") int size) {
        SiteUser user = (SiteUser) session.getAttribute("user");
        if (user == null) {
            model.addAttribute("message", "로그인 후 이용해 주세요.");
            model.addAttribute("link", "main");
            return "message";
        }

        int zeroBasedPage = (page <= 1) ? 0 : page - 1;
        Pageable pageable = PageRequest.of(zeroBasedPage, size);
        Page<Wishlist> wishlistPage = wishlistRepository.findByEmailOrderByWishdateDesc(user.getEmail(), pageable);
        List<Wishlist> wishItems = wishlistPage.getContent();

        List<Product> wishlistProducts = wishlistPage.getContent().stream()
                .map(wish -> productRepository.findById(Long.valueOf(wish.getProductid())).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        List<LocalDateTime> wishdateList = wishItems.stream()
                .map(Wishlist::getWishdate)
                .collect(Collectors.toList());

        long totalItems = wishlistPage.getTotalElements();
        // 페이지네이션 계산
        int totalPages = wishlistPage.getTotalPages();
        int currentPage = wishlistPage.getNumber() + 1;

        int blockSize = 5;
        int currentBlock = (currentPage - 1) / blockSize;
        int blockStart = currentBlock * blockSize + 1;
        int blockEnd = Math.min(blockStart + blockSize - 1, totalPages);

        int prevBlockPage = blockStart - 1;
        int nextBlockPage = blockEnd + 1;

        model.addAttribute("wishlist", wishlistPage);
        model.addAttribute("count", totalItems);
        model.addAttribute("products", wishlistProducts);
        model.addAttribute("wishdates", wishdateList);
        model.addAttribute("userPage", currentPage);
        model.addAttribute("blockStart", blockStart);
        model.addAttribute("blockEnd", blockEnd);
        model.addAttribute("prevBlockPage", prevBlockPage);
        model.addAttribute("nextBlockPage", nextBlockPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("noResult", wishlistPage.getTotalElements() == 0);

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

    @PostMapping("/wishlist/delete")
    @ResponseBody
    public Map<String, Object> deleteWishlist(@RequestParam int productid, HttpSession session) {
        SiteUser user = (SiteUser) session.getAttribute("user");
        Map<String, Object> response = new HashMap<>();

        if (user == null) {
            response.put("status", "unauthorized");
            return response;
        }

        Optional<Wishlist> wish = wishlistRepository.findByEmailAndProductid(user.getEmail(), productid);
        if (wish.isPresent()) {
            wishlistRepository.delete(wish.get());
            response.put("status", "deleted");
        } else {
            response.put("status", "not_found");
        }

        return response;
    }

}