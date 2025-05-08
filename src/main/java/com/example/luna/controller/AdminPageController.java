package com.example.luna.controller;

import com.example.luna.entity.BannerEntity;
import com.example.luna.entity.TableEntity;
import com.example.luna.repository.BannerRepository;
import com.example.luna.repository.TableRepository;
import com.example.luna.service.BannerService;
import com.example.luna.service.TableService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
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

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminPageController {
    private final BannerRepository bannerRepository;
    private final BannerService bannerService;

    private final TableRepository tableRepository;
    private final TableService tableService;

    @GetMapping("/index")
    public String adminPage() {
        return "admin/index";
    }

    @GetMapping("/mainitems")
    public String adminItems() {
        return "admin/mainitem";
    }

    @GetMapping("/banneredit")
    public String bannerEdit(Model model) {
        List<BannerEntity> bannerList = bannerRepository.findAll(Sort.by(Sort.Order.asc("bindex")));
        model.addAttribute("bannerList", bannerList);
        return "admin/index";
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
    public String boardEdit(Model model) {
        List<TableEntity> boardList = tableRepository.findAll(Sort.by(Sort.Order.asc("tbindex")));
        model.addAttribute("boardList", boardList);
        return "admin/boardedit";
    }

    @PostMapping("/saveBoards")
    public String saveBoards(@RequestParam("jsonData") String jsonData) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, String>> boardList = mapper.readValue(jsonData, new TypeReference<>() {});
        tableService.saveBoardList(boardList);
        return "redirect:/admin/boardedit";

    }
}
