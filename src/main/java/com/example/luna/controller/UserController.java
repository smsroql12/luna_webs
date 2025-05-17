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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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
    private Map<String, TokenInfo> tokenStorage = new ConcurrentHashMap<>();

    @GetMapping("/account")
    public String showProfile(HttpSession session, Model model) {
        SiteUser user = (SiteUser) session.getAttribute("user");

        if (user == null) {
            model.addAttribute("message", "로그인이 필요합니다.");
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

        model.addAttribute("message", "회원정보가 수정되었습니다.");
        return "message";
    }

    @GetMapping("/memberchangepw")
    public String showPasswordForm(Model model, HttpSession session) {
        if (session.getAttribute("user") == null) {
            model.addAttribute("message", "로그인이 필요합니다.");
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
            model.addAttribute("message", "로그인이 필요합니다.");
            model.addAttribute("link", "main");
            return "message";
        }

        if (!passwordEncoder.matches(form.getCurrentPassword(), user.getPassword())) {
            result.rejectValue("currentPassword", "wrongPassword", "현재 비밀번호가 올바르지 않습니다.");
        }

        if (!form.isNewPasswordMatching()) {
            result.rejectValue("newPasswordConfirm", "notMatching", "새 비밀번호가 일치하지 않습니다.");
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

        model.addAttribute("message", "비밀번호가 성공적으로 변경되었습니다.");
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
            model.addAttribute("loginError", "이메일 또는 비밀번호가 틀렸습니다.");
            return "signin";
        }
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
            result.rejectValue("password2", "passwordInCorrect", "비밀번호가 일치하지 않습니다.");
        }

        // 이메일 중복 체크
        if (userService.isEmailExist(form.getEmail())) {
            result.rejectValue("email", "emailDuplicate", "이미 사용 중인 이메일입니다.");
        }

        // 이메일 필수값 체크
        if (form.getEmail() == null || form.getEmail().isEmpty()) {
            result.rejectValue("email", "required", "이메일은 필수 항목입니다.");
        }

        // 인증코드 유효성 검사
        String sessionCode = (String) session.getAttribute("authCode");
        Long codeTime = (Long) session.getAttribute("authCodeTime");
        Boolean emailVerified = (Boolean) session.getAttribute("emailVerified");
        String verifiedEmail = (String) session.getAttribute("verifiedEmail");

        if (emailCode == null || emailCode.isEmpty()) {
            result.rejectValue("email", "noCode", "인증코드를 입력해주세요.");
        } else if (sessionCode == null || !sessionCode.equals(emailCode)) {
            result.rejectValue("email", "invalidCode", "인증코드가 올바르지 않습니다.");
        } else if (codeTime == null || System.currentTimeMillis() - codeTime > 10 * 60 * 1000) {
            result.rejectValue("email", "codeExpired", "인증코드가 만료되었습니다.");
        } else if (!form.getEmail().equals(verifiedEmail)) {
            result.rejectValue("email", "emailMismatch", "인증받은 이메일과 다릅니다.");
        } else {
            session.setAttribute("emailVerified", true);
        }

        // 이메일 인증 완료 여부 체크
        emailVerified = (Boolean) session.getAttribute("emailVerified");
        if (emailVerified == null || !emailVerified) {
            result.rejectValue("email", "notVerified", "이메일 인증이 완료되지 않았습니다.");
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
    public String findMyPassword() {
        return "findpassword";
    }

    @PostMapping("/send-reset-link")
    public String sendResetLink(@RequestParam("email") String email, Model model, HttpServletRequest request) {
        if (!userService.isEmailExist(email)) {
            model.addAttribute("error", "가입된 이메일이 아닙니다.");
            return "findpassword"; // 다시 이메일 입력 페이지로
        }

        // 토큰 생성 및 이메일 전송
        String token = UUID.randomUUID().toString();
        tokenStorage.put(token, new TokenInfo(email, System.currentTimeMillis())); // 메모리 저장

        String rootUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        String resetLink = rootUrl + "/reset_password?token=" + token;
        emailService.sendPasswordResetEmail(email, resetLink);
        model.addAttribute("message","입력하신 메일로 비밀번호 재설정 링크를 보내드렸습니다. 링크는 2시간 동안만 유효하니 그 전에 비밀번호를 변경해 주세요.");
        return "message";  // 이메일 전송 안내 페이지
    }

    @GetMapping("/reset_password")
    public String showResetForm(@RequestParam("token") String token, Model model) {
        TokenInfo info = tokenStorage.get(token);

        if (info == null || System.currentTimeMillis() - info.getCreatedAt() > 2 * 60 * 60 * 1000) {
            model.addAttribute("message", "비밀번호 재설정 링크가 만료되었거나 잘못되었습니다. 다시 시도 해 주세요.");
            model.addAttribute("close", "true");
            return "message"; // 에러 페이지
        }

        model.addAttribute("token", token);
        model.addAttribute("passwordResetForm", new PasswordResetForm());
        return "resetpassword"; // 비밀번호 재설정 폼 페이지
    }

    @PostMapping("/reset_password")
    public String handleReset(@RequestParam String token,
                              @ModelAttribute("passwordResetForm") @Valid PasswordResetForm form,
                              BindingResult result, Model model) {
        TokenInfo info = tokenStorage.get(token);
        if (info == null || System.currentTimeMillis() - info.getCreatedAt() > 2 * 60 * 60 * 1000) {
            model.addAttribute("message", "유효하지 않거나 만료된 토큰입니다.");
            model.addAttribute("close", "true");
            return "message";
        }

        if (!form.getPassword1().equals(form.getPassword2())) {
            model.addAttribute("token", token);
            model.addAttribute("passwordMismatch", "비밀번호가 일치하지 않습니다.");
            return "resetpassword";
        }

        userService.updatePassword(info.getEmail(), form.getPassword1());
        tokenStorage.remove(token); // 사용한 토큰 제거
        model.addAttribute("message", "비밀번호가 성공적으로 변경되었습니다.");
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

    @GetMapping("/cart")
    public String cartPage(Model model, HttpSession session) {
        SiteUser user = (SiteUser) session.getAttribute("user");

        if (user == null) {
            model.addAttribute("message", "로그인이 필요합니다.");
            model.addAttribute("link", "main");
            return "message";
        }

        String email = user.getEmail();
        List<Cart> cartList = cartRepository.findByEmailOrderByRegdateAsc(email);
        List<Product> cartProducts = productService.getProductDetails(cartList);
        model.addAttribute("cartProducts", cartProducts);
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
    public String submitOrder(@RequestParam("jsonData") String jsonData, HttpSession session, Model model, HttpServletRequest request) throws Exception {
        SiteUser user = (SiteUser) session.getAttribute("user");

        if (user == null) {
            model.addAttribute("message", "로그인이 필요합니다.");
            model.addAttribute("link", "signin");
            return "message";
        }

        ObjectMapper mapper = new ObjectMapper();
        OrderRequestDto orderRequest = mapper.readValue(jsonData, OrderRequestDto.class);

        Long userId = user.getId();

        Order order = new Order();
        String orderId = generateOrderCode(); // 주문번호
        order.setId(orderId);
        order.setUserid(userId);
        order.setTotal(orderRequest.getTotal());
        order.setStatus(0);
        order.setRequests(orderRequest.getRequests());
        orderRepository.save(order);

        for (OrderItemDto dto : orderRequest.getItems()) {
            OrderItem item = new OrderItem();
            item.setOrderid(orderId);
            item.setProductid(dto.getProductid());
            item.setQuantity(dto.getQuantity());
            item.setPrice(dto.getPrice());
            orderItemRepository.save(item);
        }

        if (!userService.isEmailExist(user.getEmail())) {
            model.addAttribute("message", "가입된 회원이 아닙니다.");
            model.addAttribute("link", "main");
            return "message";
        }
        else {
            String rootUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
            String orderCompleteLink = rootUrl + "/order/complete?orderid=" + orderId;
            emailService.orderEmail(user.getEmail(), orderCompleteLink);
        }

        // 주문완료 페이지로 주문번호 전달
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
            model.addAttribute("message", "주문 정보가 존재하지 않습니다.");
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
                    .orElseThrow(() -> new IllegalArgumentException("해당 상품이 존재하지 않습니다. 상품 ID: " + item.getProductid()));

            Map<String, Object> map = new HashMap<>();
            map.put("product", product);
            map.put("quantity", item.getQuantity());
            map.put("price", item.getPrice());
            itemDetails.add(map);
        }

        model.addAttribute("order", order);
        model.addAttribute("user", user);
        model.addAttribute("items", itemDetails);

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
            model.addAttribute("message", "로그인이 필요합니다.");
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

        return "orderlist";
    }



}