package com.example.luna.controller;

import com.example.luna.entity.TableEntity;
import com.example.luna.repository.TableRepository;
import com.example.luna.service.TableService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminPageController {
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
