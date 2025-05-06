package com.example.luna.controller;

import com.example.luna.entity.SiteUser;
import com.example.luna.form.PasswordUpdateForm;
import com.example.luna.form.UserCreateForm;
import com.example.luna.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/account")
    public String showProfile(HttpSession session, Model model) {
        SiteUser user = (SiteUser) session.getAttribute("user");

        if (user == null) {
            model.addAttribute("message", "로그인이 필요합니다.");
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
        model.addAttribute("userCreateForm", new UserCreateForm()); // 🔑 userCreateForm 객체 추가
        return "signup_form"; // 템플릿 이름 (signup_form.html)
    }

    @PostMapping("/signup")
    public String signup(@Valid UserCreateForm form, BindingResult result, Model model) {
        if (!form.isPasswordMatching()) {
            result.rejectValue("password2", "passwordInCorrect", "비밀번호가 일치하지 않습니다.");
        }

        if (userService.isEmailExist(form.getEmail())) {
            result.rejectValue("email", "emailDuplicate", "이미 사용 중인 이메일입니다.");
        }

        if (result.hasErrors()) {
            return "signup_form";
        }

        userService.create(form);
        return "redirect:/signin";
    }
}