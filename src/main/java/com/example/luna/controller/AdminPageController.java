package com.example.luna.controller;

import com.example.luna.dto.AdminOrderDTO;
import com.example.luna.dto.OrderView;
import com.example.luna.dto.OrderViewItem;
import com.example.luna.entity.*;
import com.example.luna.repository.*;
import com.example.luna.service.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/bgf1bm51yww")
public class AdminPageController {
    private final BannerRepository bannerRepository;
    private final BannerService bannerService;

    private final TableRepository tableRepository;
    private final TableService tableService;

    private final ProductRepository productRepository;
    private final ProductService productService;

    private final MainItemRepository mainItemRepository;
    private final MainItemService mainitemService;

    private final UserRepository userRepository;
    private final UserService userService;

    private final AdminUserRepository adminUserRepository;
    private final AdminUserService adminUserService;

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    private final PasswordEncoder passwordEncoder;

    @GetMapping(value = {"", "/", "/index"})
    public String adminPage(HttpSession session) {
        AdminUser user = (AdminUser) session.getAttribute("adminUser");
        if (user == null) {
            return "redirect:/bgf1bm51yww/signin";
        }
        return "admin/index";
    }

    @GetMapping("/signin")
    public String loginPage() {
        return "admin/login";
    }

    @PostMapping("/signin")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {
        AdminUser admin = adminUserService.login(username, password);  // 서비스에서 아이디+암호 체크 후 AdminUser 반환

        if (admin == null) {
            model.addAttribute("loginError", "아이디 또는 비밀번호가 틀렸습니다.");
            return "admin/login";  // 로그인 페이지로 다시 이동 (오류 메시지 출력)
        }

        session.setAttribute("adminUser", admin);  // 세션에 관리자 정보 저장
        return "redirect:/bgf1bm51yww/";  // 로그인 성공 후 관리자 메인 페이지로 이동
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();  // 세션 전체 종료
        return "redirect:/bgf1bm51yww/";  // 로그인 페이지로 이동
    }

    @GetMapping("/mainitems")
    public String adminItems(HttpSession session, Model model) {
        AdminUser user = (AdminUser) session.getAttribute("adminUser");
        if (user == null) {
            model.addAttribute("message", "로그인이 필요합니다.");
            model.addAttribute("link", "signin");
            return "admin/message";
        }

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
    public String bannerEdit(Model model, HttpSession session) {
        AdminUser user = (AdminUser) session.getAttribute("adminUser");
        if (user == null) {
            model.addAttribute("message", "로그인이 필요합니다.");
            model.addAttribute("link", "signin");
            return "admin/message";
        }

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

    @GetMapping("/boardedit")
    public String boardEdit(HttpSession session, Model model) {
        AdminUser user = (AdminUser) session.getAttribute("adminUser");
        if (user == null) {
            model.addAttribute("message", "로그인이 필요합니다.");
            model.addAttribute("link", "signin");
            return "admin/message";
        }

        List<TableEntity> boardList = tableRepository.findAll(Sort.by(Sort.Order.asc("tbindex")));
        model.addAttribute("boardList", boardList);
        return "admin/boardedit";
    }

    @PostMapping("/saveBoards")
    public String saveBoards(@RequestParam("jsonData") String jsonData, HttpSession session, Model model) throws JsonProcessingException {
        AdminUser user = (AdminUser) session.getAttribute("adminUser");
        if (user == null) {
            model.addAttribute("message", "로그인이 필요합니다.");
            model.addAttribute("link", "signin");
            return "admin/message";
        }

        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, String>> boardList = mapper.readValue(jsonData, new TypeReference<>() {});
        tableService.saveBoardList(boardList);
        return "redirect:/bgf1bm51yww/boardedit";
    }

    @GetMapping("/products")
    public String productList(
            @RequestParam(required = false) String type, // "name" or "code"
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "8") int size,
            @RequestParam(defaultValue = "new") String sort,
            HttpSession session,
            Model model
    ) {
        AdminUser user = (AdminUser) session.getAttribute("adminUser");
        if (user == null) {
            model.addAttribute("message", "로그인이 필요합니다.");
            return "admin/message";
        }

        int zeroBasedPage = (page <= 1) ? 0 : page - 1;
        Page<Product> productPage = productService.searchProductsByType(type, search, zeroBasedPage, size, sort);

        if (productPage == null) {
            model.addAttribute("message", "잘못된 접근입니다.");
            return "admin/message";
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

    @GetMapping("/usermanagement")
    public String userManage(
            @RequestParam(required = false) String type, // email, firstname, lastname
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "18") int size,
            @RequestParam(defaultValue = "new") String sort, // new, old
            HttpSession session,
            Model model
    ) {
        AdminUser user = (AdminUser) session.getAttribute("adminUser");
        if (user == null) {
            model.addAttribute("message", "로그인이 필요합니다.");
            model.addAttribute("link", "signin");
            return "admin/message";
        }

        int zeroBasedPage = Math.max(page - 1, 0);

        Sort.Direction direction = sort.equals("old") ? Sort.Direction.ASC : Sort.Direction.DESC;
        PageRequest pageable = PageRequest.of(zeroBasedPage, size, Sort.by(direction, "regdate"));

        Page<SiteUser> userPage = userService.searchUsers(type, search, pageable);

        long totalItems = userPage.getTotalElements();

        if (totalItems == 0) {
            model.addAttribute("noResult", true);
            model.addAttribute("blockStart", 1);
            model.addAttribute("blockEnd", 0);
        } else {
            int totalPages = userPage.getTotalPages();
            int currentPage = userPage.getNumber() + 1;
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

        model.addAttribute("users", userPage.getContent());
        model.addAttribute("page", userPage);
        model.addAttribute("count", totalItems);
        model.addAttribute("search", search);
        model.addAttribute("type", type);
        model.addAttribute("sort", sort);

        return "admin/usermanagement";
    }

    @GetMapping("/usermanagement/edit/{id}")
    public String editUser(@PathVariable Long id, Model model, HttpSession session) {
        AdminUser users = (AdminUser) session.getAttribute("adminUser");
        if (users == null) {
            model.addAttribute("message", "로그인이 필요합니다.");
            model.addAttribute("link", "signin");
            return "admin/message";
        }

        SiteUser user = userService.getById(id);
        model.addAttribute("user", user);
        return "admin/usermanagementedit";
    }

    @PostMapping("usermanagement/edit/{id}")
    public String updateUser(@PathVariable Long id,
                             @ModelAttribute("user") SiteUser updatedUser,
                             HttpSession session,
                             Model model) {
        AdminUser user = (AdminUser) session.getAttribute("adminUser");
        if (user == null) {
            model.addAttribute("message", "로그인이 필요합니다.");
            return "admin/message";
        }

        SiteUser existingUser = userService.getById(id);

        existingUser.setFirstname(updatedUser.getFirstname());
        existingUser.setLastname(updatedUser.getLastname());
        existingUser.setPhone(updatedUser.getPhone());
        existingUser.setAddress1(updatedUser.getAddress1());
        existingUser.setAddress2(updatedUser.getAddress2());

        userService.save(existingUser);

        model.addAttribute("message", "회원 정보가 수정되었습니다.");
        return "admin/message";
    }

    // 회원 삭제
    @GetMapping("/usermanagement/delete/{id}")
    public String deleteUser(@PathVariable Long id, Model model, HttpSession session) {
        AdminUser user = (AdminUser) session.getAttribute("adminUser");
        if (user == null) {
            model.addAttribute("message", "로그인이 필요합니다.");
            model.addAttribute("link", "signin");
            return "admin/message";
        }

        userService.deleteById(id);
        model.addAttribute("message", "회원이 삭제되었습니다.");
        model.addAttribute("link", "usermanagement");
        return "admin/message";
    }

    @GetMapping("/adminmanagement")
    public String adminManagement(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "3") int size,
            HttpSession session,
            Model model
    ) {
        AdminUser user = (AdminUser) session.getAttribute("adminUser");
        if (user == null) {
            model.addAttribute("message", "로그인이 필요합니다.");
            model.addAttribute("link", "signin");
            return "admin/message";
        }

        if(user.getRole() != 1) {
            model.addAttribute("message", "Only super administrators have access.");
            model.addAttribute("link", "main");
            return "admin/message";
        }

        int zeroBasedPage = Math.max(0, page - 1);
        Pageable pageable = PageRequest.of(zeroBasedPage, size);

        Page<AdminUser> adminPage = adminUserService.searchAdmins(search, pageable);

        long totalItems = adminPage.getTotalElements();
        int totalPages = adminPage.getTotalPages();
        int currentPage = adminPage.getNumber() + 1;
        int blockSize = 5;
        int currentBlock = (currentPage - 1) / blockSize;
        int blockStart = currentBlock * blockSize + 1;
        int blockEnd = Math.min(blockStart + blockSize - 1, totalPages);

        model.addAttribute("admins", adminPage.getContent());
        model.addAttribute("page", adminPage);
        model.addAttribute("adminPage", currentPage);
        model.addAttribute("blockStart", blockStart);
        model.addAttribute("blockEnd", blockEnd);
        model.addAttribute("prevBlockPage", blockStart - 1);
        model.addAttribute("nextBlockPage", blockEnd + 1);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("search", search);
        model.addAttribute("noResult", totalItems == 0);

        return "admin/adminlist";
    }

    @GetMapping("/adminmanagement/add")
    public String showAddForm(HttpSession session, Model model) {
        AdminUser user = (AdminUser) session.getAttribute("adminUser");
        if (user == null) {
            model.addAttribute("message", "로그인이 필요합니다.");
            model.addAttribute("link", "signin");
            return "admin/message";
        }

        if(user.getRole() != 1) {
            model.addAttribute("message", "Only super administrators have access.");
            model.addAttribute("link", "main");
            return "admin/message";
        }

        return "admin/adminsignup"; // 등록 폼 페이지 (위에서 작성한 HTML)
    }

    @PostMapping("/adminmanagement/add")
    public String addAdminUser(@RequestParam String id, @RequestParam String password, RedirectAttributes redirectAttributes, HttpSession session, Model model) {
        AdminUser user = (AdminUser) session.getAttribute("adminUser");
        if (user == null) {
            model.addAttribute("message", "로그인이 필요합니다.");
            model.addAttribute("link", "signin");
            return "admin/message";
        }

        if(user.getRole() != 1) {
            model.addAttribute("message", "Only super administrators have access.");
            model.addAttribute("link", "main");
            return "admin/message";
        }

        if (adminUserRepository.existsByAdminid(id)) {
            redirectAttributes.addFlashAttribute("error", "이미 사용 중인 ID입니다.");
            return "redirect:/bgf1bm51yww/adminmanagement/add";
        }

        AdminUser newAdmin = new AdminUser();
        newAdmin.setAdminid(id);
        newAdmin.setPassword(passwordEncoder.encode(password)); // 보안 위해 실제론 암호화 필요

        adminUserRepository.save(newAdmin);

        //redirectAttributes.addFlashAttribute("success", "관리자 계정이 등록되었습니다.");
        model.addAttribute("message", "관리자 계정이 등록되었습니다.");
        model.addAttribute("link", "adminmanagement");
        return "admin/message";
    }

    @Transactional
    @GetMapping("/adminmanagement/delete/{id}")
    public String deleteAdmin(@PathVariable String id, RedirectAttributes redirectAttributes, HttpSession session, Model model) {
        AdminUser user = (AdminUser) session.getAttribute("adminUser");
        if (user == null) {
            model.addAttribute("message", "로그인이 필요합니다.");
            model.addAttribute("link", "signin");
            return "admin/message";
        }

        if(user.getRole() != 1) {
            model.addAttribute("message", "Only super administrators have access.");
            model.addAttribute("link", "main");
            return "admin/message";
        }

        adminUserService.deleteById(id);
        redirectAttributes.addFlashAttribute("message", "관리자 계정이 삭제되었습니다.");
        return "redirect:/bgf1bm51yww/adminmanagement";
    }

    @GetMapping("/order")
    public String showAdminOrders(
            @RequestParam(required = false) String orderId,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false, defaultValue = "0") int cancelOnly,
            @RequestParam(required = false, defaultValue = "0") int includeCanceled,
            @RequestParam(required = false, defaultValue = "0") int returnOnly,
            @RequestParam(required = false) String returnStatus,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpSession session,
            Model model)
    {
        AdminUser user = (AdminUser) session.getAttribute("adminUser");
        if (user == null) {
            model.addAttribute("message", "로그인이 필요합니다.");
            model.addAttribute("link", "signin");
            return "admin/message";
        }

        String searchOrderId = (orderId != null && !orderId.isBlank()) ? "%" + orderId + "%" : null;

        Integer statusValue = null;
        if (status != null && !status.isBlank()) {
            try {
                statusValue = Integer.parseInt(status);
            } catch (NumberFormatException ignored) {}
        }

        Integer returnStatusValue = null;
        if (returnStatus != null && !returnStatus.isBlank()) {
            try {
                returnStatusValue = Integer.parseInt(returnStatus);
            } catch (NumberFormatException ignored) {}
        }

        List<Long> userIds = null;
        if (email != null && !email.isBlank()) {
            List<SiteUser> users = userRepository.findByEmailLike(email);
            if (!users.isEmpty()) {
                userIds = users.stream().map(SiteUser::getId).toList();
            } else {
                model.addAttribute("orders", Collections.emptyList());
                model.addAttribute("noResult", true);
                return "admin/adminorderlist";
            }
        }

        LocalDateTime startDateTime = (startDate != null) ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = (endDate != null) ? endDate.atTime(23, 59, 59) : null;

        Pageable pageable = PageRequest.of((page <= 1) ? 0 : page - 1, size, Sort.by(Sort.Direction.DESC, "created"));

        Page<AdminOrderDTO> orderPage = orderRepository.findAdminOrdersWithCancelAndReturn(
                searchOrderId,
                userIds,
                startDateTime,
                endDateTime,
                statusValue,
                cancelOnly == 1,
                includeCanceled == 1,
                returnOnly == 1,
                (returnOnly == 1) ? returnStatusValue : null, // returnOnly 체크 안했으면 returnStatus 무시
                pageable
        );

        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("page", orderPage);
        model.addAttribute("count", orderPage.getTotalElements());
        model.addAttribute("orderId", orderId);
        model.addAttribute("status", statusValue);
        model.addAttribute("email", email);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("cancelOnly", cancelOnly);
        model.addAttribute("includeCanceled", includeCanceled);
        model.addAttribute("returnOnly", returnOnly);
        model.addAttribute("returnStatus", returnStatus);

        int currentPage = orderPage.getNumber() + 1;
        int totalPages = orderPage.getTotalPages();
        int blockSize = 5;
        int blockStart = ((currentPage - 1) / blockSize) * blockSize + 1;
        int blockEnd = Math.min(blockStart + blockSize - 1, totalPages);

        model.addAttribute("userPage", currentPage);
        model.addAttribute("blockStart", blockStart);
        model.addAttribute("blockEnd", blockEnd);
        model.addAttribute("prevBlockPage", blockStart - 1);
        model.addAttribute("nextBlockPage", blockEnd + 1);
        model.addAttribute("noResult", orderPage.getTotalElements() == 0);

        return "admin/adminorderlist";
    }


    @GetMapping("/orderedit")
    public String orderEdit(@RequestParam("orderid") String orderId, HttpSession session, Model model) {
        AdminUser user = (AdminUser) session.getAttribute("adminUser");
        if (user == null) {
            model.addAttribute("message", "로그인이 필요합니다.");
            model.addAttribute("link", "signin");
            return "admin/message";
        }

        Optional<Order> orderOptional = orderRepository.findById(orderId);
        if (orderOptional.isEmpty()) {
            return "redirect:/orders";
        }

        Order order = orderOptional.get();
        List<OrderViewItem> items = orderItemRepository.findItemsWithProductByOrderId(orderId);
        OrderView orderView = new OrderView(order, items);

        Optional<SiteUser> userOptional = userRepository.findById(order.getUserid());

        if (userOptional.isPresent()) {
            model.addAttribute("user", userOptional.get());
            model.addAttribute("userDeleted", 0);
        } else {
            model.addAttribute("userDeleted", 1);
        }

        model.addAttribute("orderView", orderView);
        model.addAttribute("order", order);
        model.addAttribute("items", items);

        return "admin/adminorderedit";
    }


    @PostMapping("/orderupdate")
    public String updateOrder(
            @RequestParam String id,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String trackingnum,
            @RequestParam(required = false) String shipcompany,
            @RequestParam(required = false) String returncomplete,
            HttpSession session,
            Model model
    ) {
        AdminUser user = (AdminUser) session.getAttribute("adminUser");
        if (user == null) {
            model.addAttribute("message", "로그인이 필요합니다.");
            model.addAttribute("link", "signin");
            return "admin/message";
        }

        Order order = orderRepository.findById(id).orElse(null);
        if (order == null) {
            model.addAttribute("message", "수정에 실패 하였습니다.");
            return "admin/message";
        }
        order.setStatus(status);
        order.setTrackingnum(trackingnum);
        order.setShipcom(shipcompany);
        order.setReturncomplete(Integer.parseInt(returncomplete));
        orderRepository.save(order);

        return "redirect:/bgf1bm51yww/orderedit?orderid=" + id; // 다시 상세보기 페이지로
    }

    @GetMapping("/returnapply")
    public String applyReturnRequest(@RequestParam("orderid") String orderId, HttpSession session, Model model) {
        AdminUser user = (AdminUser) session.getAttribute("adminUser");
        if (user == null) {
            model.addAttribute("message", "로그인이 필요합니다.");
            model.addAttribute("link", "signin");
            return "admin/message";
        }

        Optional<Order> optionalOrder = orderRepository.findById(orderId);
        if (optionalOrder.isEmpty()) {
            model.addAttribute("message", "해당 상품을 찾을 수 없습니다.");
            return "admin/message";
        }

        Order order = optionalOrder.get();

        if (order.getReturnitem() == 1) {
            model.addAttribute("message", "이미 반품 요청된 상품입니다.");
            return "admin/message";
        }

        order.setReturnitem(1); // 반품 상태 설정
        orderRepository.save(order);

        return "redirect:/bgf1bm51yww/orderedit?orderid="+orderId;
    }


    @GetMapping("/returncancel")
    public String cancelReturnRequest(@RequestParam("orderid") String orderId, HttpSession session, Model model) {
        AdminUser user = (AdminUser) session.getAttribute("adminUser");
        if (user == null) {
            model.addAttribute("message", "로그인이 필요합니다.");
            model.addAttribute("link", "signin");
            return "admin/message";
        }

        Optional<Order> optionalOrder = orderRepository.findById(orderId);
        if (optionalOrder.isEmpty()) {
            model.addAttribute("message", "해당 상품을 찾을 수 없습니다.");
            return "admin/message";
        }

        Order order = optionalOrder.get();

        if (order.getReturnitem() == 0) {
            model.addAttribute("message", "반품 상품이 아닙니다.");
            return "admin/message";
        }

        order.setReturnitem(0); // 반품 상태 취소
        orderRepository.save(order);

        return "redirect:/bgf1bm51yww/orderedit?orderid="+orderId;
    }

}
