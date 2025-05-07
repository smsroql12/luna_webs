package com.example.luna.controller;

import com.example.luna.entity.Product;
import com.example.luna.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/admin/product")
public class ProductController {
    private final ProductRepository productRepository;
    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping("/form")
    public String form(@RequestParam(value = "id", required = false) Long id, Model model) {
        Product product = (id != null) ? productRepository.findById(id).orElse(new Product()) : new Product();
        model.addAttribute("product", product);
        return "admin/add_product";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Product product,
                       @RequestParam("imageFile") MultipartFile file,
                       RedirectAttributes redirectAttributes) throws IOException {

        boolean isEdit = (product.getNo() != null);

        if (!file.isEmpty()) {
            // 이미지가 새로 첨부된 경우 → 새 이미지 저장 및 교체
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            String savePath = "C:/upload/" + fileName;

            File saveFile = new File(savePath);
            saveFile.getParentFile().mkdirs(); // 폴더 없으면 생성
            file.transferTo(saveFile);

            product.setImage(fileName);
        } else if (isEdit) {
            // 수정인데 이미지가 없을 경우 기존 이미지 유지
            Product existing = productRepository.findById(product.getNo()).orElse(null);
            if (existing != null) {
                product.setImage(existing.getImage());
            }
        }
        productRepository.save(product);
        return "redirect:/admin/product/list";
    }

    @PostMapping("/upload/summernote")
    @ResponseBody
    public Map<String, Object> uploadSummernoteImage(@RequestParam("file") MultipartFile file) throws IOException {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        String savePath = "C:/upload/content/" + fileName;

        File saveFile = new File(savePath);
        saveFile.getParentFile().mkdirs(); // 폴더 없으면 생성
        file.transferTo(saveFile);

        Map<String, Object> response = new HashMap<>();
        response.put("url", "/upload/content/" + fileName); // 클라이언트에 삽입될 이미지 경로
        response.put("responseCode", "success");

        return response;
    }

    @PostMapping("/delete/summernote")
    @ResponseBody
    public ResponseEntity<String> deleteSummernoteImage(@RequestParam String imageUrl) {
        try {
            // 1. 업로드 루트 경로
            String uploadRootPath = "C:/upload/content/";
            Path uploadRoot = Paths.get(uploadRootPath).toRealPath(); // 정규화된 경로로 변경

            // 2. 전달받은 URL에서 파일명 추출
            String filename = Paths.get(new URI(imageUrl).getPath()).getFileName().toString();
            Path filePath = Paths.get(uploadRootPath, filename).toRealPath();

            // 3. 경로 제한 확인: uploadRoot 내부인지 검사
            if (!filePath.startsWith(uploadRoot)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("잘못된 경로입니다.");
            }

            // 4. 삭제
            File file = filePath.toFile();
            if (file.exists()) {
                file.delete();
                return ResponseEntity.ok("삭제 완료");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("파일 없음");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("에러 발생");
        }
    }
}
