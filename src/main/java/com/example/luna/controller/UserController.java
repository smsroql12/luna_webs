package com.example.luna.controller;

import com.example.luna.dto.*;
import com.example.luna.entity.*;
import com.example.luna.form.PasswordResetForm;
import com.example.luna.form.PasswordUpdateForm;
import com.example.luna.form.UserCreateForm;
import com.example.luna.repository.*;
import com.example.luna.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final PasswordResetTokenService tokenService;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartService cartService;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final UserRepository userRepository;
    private final WishlistRepository wishlistRepository;
    private final TableRepository tableRepository;
    private Map<String, TokenInfo> tokenStorage = new ConcurrentHashMap<>();

    @GetMapping("/account")
    public String showProfile(HttpSession session, Model model) {
        SiteUser user = (SiteUser) session.getAttribute("user");

        if (user == null) {
            model.addAttribute("message", "Please Sign in.");
            model.addAttribute("link", "main");
            return "message";
        }

        model.addAttribute("user", user);
        return "account_member";
    }

    @PostMapping("/account")
    public String updateProfile(@ModelAttribute("user") SiteUser updatedUser, HttpSession session, Model model) {


        SiteUser user = (SiteUser) session.getAttribute("user");
        SiteUser existingUser = userService.getByEmail(user.getEmail());

        existingUser.setFirstname(updatedUser.getFirstname());
        existingUser.setLastname(updatedUser.getLastname());
        existingUser.setPhone(updatedUser.getPhone());
        existingUser.setAddress1(updatedUser.getAddress1());
        existingUser.setAddress2(updatedUser.getAddress2());

        userService.save(existingUser);

        session.setAttribute("user", existingUser);

        //시큐리티 세션도 갱신
        UserDetails userDetails = userService.loadUserByUsername(existingUser.getEmail());
        Authentication newAuth = new UsernamePasswordAuthenticationToken(
                userDetails, userDetails.getPassword(), userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(newAuth);

        model.addAttribute("message", "Your account has been modified.");
        return "message";
    }

    @GetMapping("/memberchangepw")
    public String showPasswordForm(Model model, HttpSession session) {
        if (session.getAttribute("user") == null) {
            model.addAttribute("message", "Please Sign in.");
            model.addAttribute("link", "main");
            return "message";
        }
        model.addAttribute("passwordForm", new PasswordUpdateForm());
        return "change_password";
    }

    @PostMapping("/memberchangepw")
    public String updatePassword(@Valid @ModelAttribute("passwordForm") PasswordUpdateForm form,
                                 BindingResult result,
                                 HttpSession session,
                                 Model model) {
        SiteUser user = (SiteUser) session.getAttribute("user");
        if (user == null) {
            model.addAttribute("message", "Please Sign in.");
            model.addAttribute("link", "main");
            return "message";
        }

        if (!passwordEncoder.matches(form.getCurrentPassword(), user.getPassword())) {
            result.rejectValue("currentPassword", "wrongPassword", "The current password is not valid.");
        }

        if (!form.isNewPasswordMatching()) {
            result.rejectValue("newPasswordConfirm", "notMatching", "New password does not match.");
        }

        if (result.hasErrors()) {
            return "change_password";
        }

        user.setPassword(passwordEncoder.encode(form.getNewPassword()));
        userService.save(user);
        session.setAttribute("user", user); // 세션 업데이트

        UserDetails userDetails = userService.loadUserByUsername(user.getEmail());
        Authentication newAuth = new UsernamePasswordAuthenticationToken(
                userDetails, userDetails.getPassword(), userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(newAuth);

        model.addAttribute("message", "Password changed successfully.");
        model.addAttribute("link", "main");
        return "message";
    }

    @GetMapping("/signin")
    public String loginForm() {
        return "signin";
    }

    @PostMapping("/signin")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {
        SiteUser user = userService.login(email, password);
        if (user == null) {
            model.addAttribute("loginError", "Email or password is incorrect.");
            return "signin";
        }

        List<TableEntity> boardList = tableRepository.findByActiveOrderByTbindexAsc(1);
        model.addAttribute("boardList", boardList);

        session.setAttribute("user", user); // 세션에 사용자 정보 저장
        return "redirect:/";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // 세션 제거
        return "redirect:/";
    }

    @GetMapping("/signup")
    public String signupForm(Model model) {
        model.addAttribute("userCreateForm", new UserCreateForm());

        List<TableEntity> boardList = tableRepository.findByActiveOrderByTbindexAsc(1);
        model.addAttribute("boardList", boardList);

        return "signup_form"; // 템플릿 이름 (signup_form.html)
    }

    @PostMapping("/signup")
    public String signup(@Valid UserCreateForm form,
                         BindingResult result,
                         @RequestParam String emailCode,
                         HttpSession session,
                         Model model) {

        // 비밀번호 확인
        if (!form.isPasswordMatching()) {
            result.rejectValue("password2", "passwordInCorrect", "Password doesn't match.");
        }

        // 이메일 중복 체크
        if (userService.isEmailExist(form.getEmail())) {
            result.rejectValue("email", "emailDuplicate", "This email is already in use.");
        }

        // 이메일 필수값 체크
        if (form.getEmail() == null || form.getEmail().isEmpty()) {
            result.rejectValue("email", "required", "Email must be entered.");
        }

        // 인증코드 유효성 검사
        String sessionCode = (String) session.getAttribute("authCode");
        Long codeTime = (Long) session.getAttribute("authCodeTime");
        Boolean emailVerified = (Boolean) session.getAttribute("emailVerified");
        String verifiedEmail = (String) session.getAttribute("verifiedEmail");

        if (emailCode == null || emailCode.isEmpty()) {
            result.rejectValue("email", "noCode", "Please enter the authentication code.");
        } else if (sessionCode == null || !sessionCode.equals(emailCode)) {
            result.rejectValue("email", "invalidCode", "Invalid authentication code.");
        } else if (codeTime == null || System.currentTimeMillis() - codeTime > 10 * 60 * 1000) {
            result.rejectValue("email", "codeExpired", "The authentication code has expired.");
        } else if (!form.getEmail().equals(verifiedEmail)) {
            result.rejectValue("email", "emailMismatch", "It's not an authenticated email.");
        } else {
            session.setAttribute("emailVerified", true);
        }

        // 이메일 인증 완료 여부 체크
        emailVerified = (Boolean) session.getAttribute("emailVerified");
        if (emailVerified == null || !emailVerified) {
            result.rejectValue("email", "notVerified", "Email authentication not completed.");
        }

        // 에러가 있으면 다시 폼으로
        if (result.hasErrors()) {
            return "signup_form";
        }

        // 회원가입 처리
        userService.create(form);

        // 세션 정리
        session.removeAttribute("authCode");
        session.removeAttribute("authCodeTime");
        session.removeAttribute("emailVerified");
        session.removeAttribute("verifiedEmail");

        return "redirect:/signin";
    }

    @PostMapping("/send-code")
    @ResponseBody
    public String sendCode(@RequestParam String email, HttpSession session) {
        String code = generateCode();
        session.setAttribute("authCode", code);
        session.setAttribute("authCodeTime", System.currentTimeMillis());
        session.setAttribute("authEmail", email); // 추가
        emailService.sendVerificationEmail(email, code);
        return "ok";
    }

    @GetMapping("/verify-code")
    @ResponseBody
    public Map<String, Object> verifyCode(@RequestParam String code, HttpSession session) {
        String savedCode = (String) session.getAttribute("authCode");
        Long savedTime = (Long) session.getAttribute("authCodeTime");
        String email = (String) session.getAttribute("authEmail"); // 인증요청할 때 저장해둔 이메일

        boolean isValid = savedCode != null && savedCode.equals(code)
                && savedTime != null && System.currentTimeMillis() - savedTime < 10 * 60 * 1000;

        Map<String, Object> res = new HashMap<>();
        if (isValid) {
            session.setAttribute("emailVerified", true);
            session.setAttribute("verifiedEmail", email); // 인증 받은 이메일 저장
        }
        res.put("success", isValid);
        return res;
    }


    private String generateCode() {
        return String.valueOf((int)(Math.random() * 900000) + 100000); // 6자리 숫자
    }

    @GetMapping("/findmypassword")
    public String findMyPassword(Model model) {
        List<TableEntity> boardList = tableRepository.findByActiveOrderByTbindexAsc(1);
        model.addAttribute("boardList", boardList);
        return "findpassword";
    }

    @PostMapping("/send-reset-link")
    public String sendResetLink(@RequestParam("email") String email, Model model, HttpServletRequest request) {
        if (!userService.isEmailExist(email)) {
            model.addAttribute("error", "This is not a registered email");
            return "findpassword"; // 다시 이메일 입력 페이지로
        }

        // 토큰 생성 및 이메일 전송
        String token = UUID.randomUUID().toString();
        tokenStorage.put(token, new TokenInfo(email, System.currentTimeMillis())); // 메모리 저장

        String rootUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        String resetLink = rootUrl + "/reset_password?token=" + token;
        emailService.sendPasswordResetEmail(email, resetLink);
        model.addAttribute("message","We sent you a link to reset your password in the mail you entered. The link is only valid for 2 hours, so please change your password before then.");
        return "message";  // 이메일 전송 안내 페이지
    }

    @GetMapping("/reset_password")
    public String showResetForm(@Valid PasswordResetForm prForm, BindingResult result, @RequestParam("token") String token, Model model) {
        TokenInfo info = tokenStorage.get(token);

        if (info == null || System.currentTimeMillis() - info.getCreatedAt() > 2 * 60 * 60 * 1000) {
            model.addAttribute("message", "Password reset link expired or invalid, please try again.");
            model.addAttribute("close", "true");
            return "message"; // 에러 페이지
        }

        if (!prForm.isPasswordMatching()) {
            result.rejectValue("password2", "passwordInCorrect", "Password doesn't match.");
        }

        if (result.hasErrors()) {
            return "resetpassword";
        }

        model.addAttribute("token", token);
        model.addAttribute("passwordResetForm", new PasswordResetForm());
        List<TableEntity> boardList = tableRepository.findByActiveOrderByTbindexAsc(1);
        model.addAttribute("boardList", boardList);

        return "resetpassword"; // 비밀번호 재설정 폼 페이지
    }

    @PostMapping("/reset_password")
    public String handleReset(@RequestParam String token,
                              @ModelAttribute("passwordResetForm") @Valid PasswordResetForm form,
                              BindingResult result, Model model) {
        TokenInfo info = tokenStorage.get(token);
        if (info == null || System.currentTimeMillis() - info.getCreatedAt() > 2 * 60 * 60 * 1000) {
            model.addAttribute("message", "Invalid or expired token.");
            model.addAttribute("close", "true");
            return "message";
        }

        if (!form.getPassword1().equals(form.getPassword2())) {
            model.addAttribute("token", token);
            model.addAttribute("passwordMismatch", "Password doesn't match.");
            return "resetpassword";
        }

        userService.updatePassword(info.getEmail(), form.getPassword1());
        tokenStorage.remove(token); // 사용한 토큰 제거
        model.addAttribute("message", "Your password has been changed successfully.");
        model.addAttribute("link", "signin");
        return "message"; // 성공 안내 페이지
    }

    @Getter
    public static class TokenInfo {
        private String email;
        private long createdAt;

        public TokenInfo(String email, long createdAt) {
            this.email = email;
            this.createdAt = createdAt;
        }
    }

    @GetMapping("/withdrawal")
    public String showWithdrawalForm(Model model, HttpSession session) {
        SiteUser user = (SiteUser) session.getAttribute("user");
        if (user == null) {
            model.addAttribute("message", "Please Sign in.");
            model.addAttribute("link", "main");
            return "message";
        }

        model.addAttribute("passwordForm", new PasswordForm());
        return "withdrawal";
    }

    //회원탈퇴
    @PostMapping("/withdrawal")
    @Transactional
    public String deleteUser(
            @Valid @ModelAttribute("passwordForm") PasswordForm passwordForm,
            BindingResult result,
            HttpSession session,
            Model model) {

        SiteUser user = (SiteUser) session.getAttribute("user");
        if (user == null) {
            model.addAttribute("message", "Please Sign in.");
            model.addAttribute("link", "main");
            return "message";
        }

        String email = user.getEmail();
        SiteUser users = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(passwordForm.getCurrentPassword(), users.getPassword())) {
            result.rejectValue("currentPassword", "wrongPassword", "Password is Wrong.");
        }

        if (result.hasErrors()) {
            return "withdrawal";
        }

        cartRepository.deleteByEmail(email);
        wishlistRepository.deleteByEmail(email);
        userRepository.delete(users);
        session.invalidate();

        return "withdrawalcomplete";
    }


    @GetMapping("/cart")
    public String cartPage(Model model, HttpSession session) {
        SiteUser user = (SiteUser) session.getAttribute("user");

        if (user == null) {
            model.addAttribute("message", "Please Sign in.");
            model.addAttribute("link", "main");
            return "message";
        }

        String email = user.getEmail();
        List<Cart> cartList = cartRepository.findByEmailOrderByRegdateAsc(email);
        List<Product> cartProducts = productService.getProductDetails(cartList);
        model.addAttribute("address1", user.getAddress1());
        model.addAttribute("address2", user.getAddress2());
        model.addAttribute("cartProducts", cartProducts);

        List<TableEntity> boardList = tableRepository.findByActiveOrderByTbindexAsc(1);
        model.addAttribute("boardList", boardList);

        return "cart";
    }

    @GetMapping("/cart/items")
    @ResponseBody
    public List<CartItemDTO> getCartItems(HttpSession session) {
        String email = (String) session.getAttribute("email");
        return cartService.getAllItemsForUser(email);
    }

    @PostMapping("/cart/toggle")
    @ResponseBody
    public Map<String, Object> toggleCart(@RequestParam int productid, HttpSession session) {
        SiteUser user = (SiteUser) session.getAttribute("user");
        Map<String, Object> response = new HashMap<>();

        if (user == null) {
            response.put("status", "unauthorized");
            return response;
        }

        Optional<Cart> existing = cartRepository.findByEmailAndProductid(user.getEmail(), productid);
        Optional<Product> productOpt = productRepository.findByNo((long)productid);
        if (existing.isPresent()) {
            cartRepository.delete(existing.get());
            response.put("status", "removed");
        } else {
            Cart cart = new Cart();
            cart.setEmail(user.getEmail());
            cart.setProductid(productid);
            cart.setProductName(productOpt.get().getName());
            cart.setRegdate(LocalDateTime.now());
            cartRepository.save(cart);
            response.put("status", "added");
        }
        return response;
    }

    @PostMapping("/cart/delete")
    @ResponseBody
    public Map<String, Object> deleteCartItem(@RequestParam int productid, HttpSession session) {
        SiteUser user = (SiteUser) session.getAttribute("user");
        Map<String, Object> response = new HashMap<>();

        if (user == null) {
            response.put("status", "unauthorized");
            return response;
        }

        Optional<Cart> cartItem = cartRepository.findByEmailAndProductid(user.getEmail(), productid);
        if (cartItem.isPresent()) {
            cartRepository.delete(cartItem.get());
            response.put("status", "deleted");
        } else {
            response.put("status", "not_found");
        }

        return response;
    }

    @PostMapping("/cart/deleteAll")
    @ResponseBody
    public Map<String, Object> deleteAllCartItems(HttpSession session) {
        SiteUser user = (SiteUser) session.getAttribute("user");
        Map<String, Object> response = new HashMap<>();

        if (user == null) {
            response.put("status", "unauthorized");
            return response;
        }

        List<Cart> items = cartRepository.findByEmail(user.getEmail());
        if (!items.isEmpty()) {
            cartRepository.deleteAll(items);
            response.put("status", "deleted");
        } else {
            response.put("status", "empty");
        }

        return response;
    }

    @PostMapping("/order/submit")
    @Transactional
    public String submitOrder(@RequestParam("jsonData") String jsonData, HttpSession session, Model model, HttpServletRequest request) throws Exception {
        SiteUser user = (SiteUser) session.getAttribute("user");

        if (user == null) {
            model.addAttribute("message", "Please Sign in.");
            model.addAttribute("link", "signin");
            return "message";
        }

        ObjectMapper mapper = new ObjectMapper();
        OrderRequestDto orderRequest = mapper.readValue(jsonData, OrderRequestDto.class);
        Long userId = user.getId();

        float calculatedTotal = 0;

        for (OrderItemDto dto : orderRequest.getItems()) {
            Product product = productRepository.findById(dto.getProductid()).orElse(null);

            if (product == null) {
                model.addAttribute("message", "Items that don't exist are included.");
                model.addAttribute("link", "cart");
                return "message";
            }

            float expectedPrice;

            // 세일 여부 판단
            boolean isSale = product.isSale();
            boolean saleValid = isSale && product.getEndsaledate() != null && product.getEndsaledate().isAfter(LocalDateTime.now());

            if (saleValid) {
                expectedPrice = product.getSaleprice();
            } else {
                expectedPrice = product.getPrice();
            }

            // 클라이언트가 전송한 가격이 서버 기준 가격과 다르면 반려
            if (dto.getPrice() != expectedPrice) {
                model.addAttribute("message", "The item price information has been changed. Please check again and make the payment.");
                model.addAttribute("link", "cart");
                return "message";
            }

            // 수량 유효성 검사 (선택)
            if (dto.getQuantity() == null || dto.getQuantity() <= 0) {
                model.addAttribute("message", "There is a item with the wrong quantity.");
                model.addAttribute("link", "cart");
                return "message";
            }

            calculatedTotal += expectedPrice * dto.getQuantity();
        }

        // 총액 검증
        if (calculatedTotal != orderRequest.getTotal()) {
            model.addAttribute("message", "The total payment amount does not match, please check again.");
            model.addAttribute("link", "cart");
            return "message";
        }

        // 주문 생성
        Order order = new Order();
        String orderId = generateOrderCode();
        order.setId(orderId);
        order.setUserid(userId);
        order.setTotal(calculatedTotal);
        order.setStatus(0);
        order.setRequests(orderRequest.getRequests());
        order.setAddress1(orderRequest.getAddress1());
        order.setAddress2(orderRequest.getAddress2());
        orderRepository.save(order);

        // 주문 아이템 저장
        for (OrderItemDto dto : orderRequest.getItems()) {
            OrderItem item = new OrderItem();
            item.setOrderid(orderId);
            item.setProductid(dto.getProductid());
            item.setQuantity(dto.getQuantity());
            item.setPrice(dto.getPrice()); // 이건 위에서 검증 완료된 값
            orderItemRepository.save(item);
        }

        //카트 아이템 삭제
        cartRepository.deleteByEmail(user.getEmail());

        // 이메일 전송
        if (!userService.isEmailExist(user.getEmail())) {
            model.addAttribute("message", "You are not a registered member.");
            model.addAttribute("link", "main");
            return "message";
        } else {
            String rootUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
            String orderCompleteLink = rootUrl + "/order/complete?orderid=" + orderId;
            emailService.orderEmail(user.getEmail(), orderCompleteLink);
        }

        return "redirect:/order/complete?orderid=" + orderId;
    }


    public String generateOrderCode() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String timestamp = LocalDateTime.now().format(formatter);
        int random = (int)(Math.random() * 9000) + 1000; // 1000~9999
        return timestamp + random;
    }

    @GetMapping("/order/complete")
    public String orderComplete(@RequestParam("orderid") String orderId, Model model) {
        // 주문 정보
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            model.addAttribute("message", "Order information does not exist.");
            return "message";
        }

        // 주문자 정보
        SiteUser user = userRepository.findById(order.getUserid()).orElse(null);

        // 주문 아이템 목록
        List<OrderItem> items = orderItemRepository.findByOrderid(orderId);

        // 상품 정보 매핑
        List<Map<String, Object>> itemDetails = new ArrayList<>();
        for (OrderItem item : items) {
            Product product = productRepository.findByNo(item.getProductid())
                    .orElseThrow(() -> new IllegalArgumentException("The product does not exist. Item ID : " + item.getProductid()));

            Map<String, Object> map = new HashMap<>();
            map.put("product", product);
            map.put("quantity", item.getQuantity());
            map.put("price", item.getPrice());
            itemDetails.add(map);
        }

        model.addAttribute("order", order);
        model.addAttribute("user", user);
        model.addAttribute("items", itemDetails);

        List<TableEntity> boardList = tableRepository.findByActiveOrderByTbindexAsc(1);
        model.addAttribute("boardList", boardList);

        return "ordercomplete"; // order/complete.html
    }

    @GetMapping("/order/list")
    public String orderList(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model,
            HttpSession session) {

        SiteUser user = (SiteUser) session.getAttribute("user");
        if (user == null) {
            model.addAttribute("message", "Please Sign in.");
            model.addAttribute("link", "main");
            return "message";
        }

        LocalDateTime startDateTime = (startDate != null) ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = (endDate != null) ? endDate.atTime(LocalTime.MAX) : null;

        int zeroBasedPage = (page <= 1) ? 0 : page - 1;
        Pageable pageable = PageRequest.of(zeroBasedPage, size, Sort.by(Sort.Direction.DESC, "created"));

        Page<Order> orderPage = orderRepository.searchByFilters(user.getId(), search, startDateTime, endDateTime, pageable);
        List<OrderView> orderViews = new ArrayList<>();

        for (Order order : orderPage.getContent()) {
            List<OrderItem> items = orderItemRepository.findByOrderid(order.getId());
            List<OrderViewItem> itemViews = new ArrayList<>();
            for (OrderItem item : items) {
                Product product = productRepository.findById(item.getProductid()).orElse(null);
                itemViews.add(new OrderViewItem(item, product));
            }
            orderViews.add(new OrderView(order, itemViews));
        }

        int totalPages = orderPage.getTotalPages();
        int currentPage = orderPage.getNumber() + 1;
        int blockSize = 5;
        int blockStart = ((currentPage - 1) / blockSize) * blockSize + 1;
        int blockEnd = Math.min(blockStart + blockSize - 1, totalPages);

        model.addAttribute("orderViews", orderViews);
        model.addAttribute("page", orderPage);
        model.addAttribute("count", orderPage.getTotalElements());
        model.addAttribute("search", search);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("userPage", currentPage);
        model.addAttribute("blockStart", blockStart);
        model.addAttribute("blockEnd", blockEnd);
        model.addAttribute("prevBlockPage", blockStart - 1);
        model.addAttribute("nextBlockPage", blockEnd + 1);
        model.addAttribute("noResult", orderPage.getTotalElements() == 0);

        List<TableEntity> boardList = tableRepository.findByActiveOrderByTbindexAsc(1);
        model.addAttribute("boardList", boardList);

        return "orderlist";
    }

    @GetMapping("/order/received")
    public String completeDelivery(@RequestParam String ordercode, RedirectAttributes redirectAttributes) {
        Optional<Order> optionalOrder = orderRepository.findById(ordercode);
        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();
            if (order.getCancel() == 0 && order.getStatus() == 3) {
                order.setStatus(4);
                orderRepository.save(order);
            }
        }
        return "redirect:/order/list";
    }

    @GetMapping("/order/cancel")
    public String cancelOrder(@RequestParam String ordercode, HttpSession session, RedirectAttributes redirectAttributes, Model model, HttpServletRequest request) {
        SiteUser user = (SiteUser) session.getAttribute("user");

        if (user == null) {
            model.addAttribute("message", "Please Sign in.");
            model.addAttribute("link", "signin");
            return "message";
        }

        Optional<Order> optionalOrder = orderRepository.findById(ordercode);
        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();
            if (order.getStatus() >= 3) {
                redirectAttributes.addFlashAttribute("cancelError", "The item has already departed. Please use the return function.");
                return "redirect:/order/list"; // 적절한 경로
            }
            order.setCancel(1);
            orderRepository.save(order);
        }

        // 이메일 전송
        if (!userService.isEmailExist(user.getEmail())) {
            model.addAttribute("message", "You are not a registered member.");
            model.addAttribute("link", "main");
            return "message";
        } else {
            String rootUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
            String orderCompleteLink = rootUrl + "/order/complete?orderid=" + ordercode;
            emailService.cancelEmail(user.getEmail(), orderCompleteLink);
        }

        return "redirect:/order/list";
    }

    @GetMapping("/order/returnpage")
    public String returnPage(@RequestParam("ordercode") String ordercode, Model model) {
        Optional<Order> optionalOrder = orderRepository.findById(ordercode);
        if (optionalOrder.isEmpty()) {
            model.addAttribute("error", "It's a non-existent order.");
            return "errorpage"; // 오류 페이지
        }

        Order order = optionalOrder.get();

        if ((order.getStatus() != 3 && order.getStatus() != 4) || order.getCancel() == 1) {
            model.addAttribute("error", "Returnable status not available.");
            return "errorpage";
        }

        List<OrderItem> items = orderItemRepository.findByOrderid(ordercode);
        List<OrderViewItem> itemViews = new ArrayList<>();
        for (OrderItem item : items) {
            Product product = productRepository.findById(item.getProductid()).orElse(null);
            if (product != null) {
                itemViews.add(new OrderViewItem(item, product));
            }
        }

        model.addAttribute("orderView", new OrderView(order, itemViews));
        List<TableEntity> boardList = tableRepository.findByActiveOrderByTbindexAsc(1);
        model.addAttribute("boardList", boardList);

        return "returnitem";
    }


    @PostMapping("/order/return")
    public String submitReturn(@RequestParam("orderid") String orderid,
                               @RequestParam("returnmsg") String returnmsg,
                               RedirectAttributes redirectAttributes,
                               HttpSession session,
                               HttpServletRequest request,
                               Model model) {

        SiteUser user = (SiteUser) session.getAttribute("user");
        if (user == null) {
            model.addAttribute("message", "Please Sign in.");
            model.addAttribute("link", "signin");
            return "message";
        }

        Optional<Order> optionalOrder = orderRepository.findById(orderid);
        if (optionalOrder.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "It's a non-existent order");
            return "redirect:/errorpage";
        }

        Order order = optionalOrder.get();

        if ((order.getStatus() != 3 && order.getStatus() != 4) || order.getCancel() == 1) {
            redirectAttributes.addFlashAttribute("error", "Returnable status not available.");
            return "redirect:/errorpage";
        }

        order.setReturnitem(1);
        order.setReturnmsg(returnmsg);
        orderRepository.save(order);

        // 이메일 전송
        if (!userService.isEmailExist(user.getEmail())) {
            model.addAttribute("message", "You are not a registered member.");
            model.addAttribute("link", "main");
            return "message";
        } else {
            String rootUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
            String orderCompleteLink = rootUrl + "/order/complete?orderid=" + orderid;
            emailService.returnEmail(user.getEmail(), orderCompleteLink);
        }

        return "redirect:/order/returncomplete?orderid=" + orderid;
    }

    @GetMapping("/order/returncomplete")
    public String returnComplete(@RequestParam("orderid") String orderid, Model model) {
        model.addAttribute("orderid", orderid);
        List<TableEntity> boardList = tableRepository.findByActiveOrderByTbindexAsc(1);
        model.addAttribute("boardList", boardList);

        return "returnrequestcomplete";
    }


    @GetMapping("/order/return-cancel")
    public String cancelReturn(@RequestParam String ordercode,
                               HttpSession session,
                               HttpServletRequest request,
                               Model model) {

        SiteUser user = (SiteUser) session.getAttribute("user");
        if (user == null) {
            model.addAttribute("message", "Please Sign in.");
            model.addAttribute("link", "signin");
            return "message";
        }

        Optional<Order> optionalOrder = orderRepository.findById(ordercode);
        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();
            order.setReturnitem(0);
            orderRepository.save(order);
        }

        // 이메일 전송
        if (!userService.isEmailExist(user.getEmail())) {
            model.addAttribute("message", "You are not a registered member.");
            model.addAttribute("link", "main");
            return "message";
        } else {
            String rootUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
            String orderCompleteLink = rootUrl + "/order/complete?orderid=" + ordercode;
            emailService.returnCancelEmail(user.getEmail(), orderCompleteLink);
        }

        return "redirect:/order/list";
    }

    @PostMapping("/order/return-tracking")
    @ResponseBody
    public ResponseEntity<?> submitReturnTrackingAjax(@RequestParam String ordercode,
                                                      @RequestParam String trackingnum) {
        //System.out.println(ordercode);
        Optional<Order> optionalOrder = orderRepository.findById(ordercode);
        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();
            order.setReturntrackingnum(trackingnum);
            orderRepository.save(order);
            return ResponseEntity.ok("Your invoice number has been saved.");
        }
        return ResponseEntity.badRequest().body("Order not found.");
    }

    @GetMapping("/howtouse")
    public String Howtouse() {
        return "howtouse";
    }

}