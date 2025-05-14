package com.example.luna.controller;

import com.example.luna.entity.Product;
import com.example.luna.entity.TableEntity;
import com.example.luna.repository.ProductRepository;
import com.example.luna.repository.TableRepository;
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
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
@RequestMapping("/admin/product")
public class ProductController {
    private final ProductRepository productRepository;
    private final TableRepository tableRepository;
    public ProductController(ProductRepository productRepository, TableRepository tableRepository) {
        this.productRepository = productRepository;
        this.tableRepository = tableRepository;
    }

    @GetMapping("/form")
    public String form(@RequestParam(value = "id", required = false) Long id, Model model) {
        Product product = (id != null) ? productRepository.findById(id).orElse(new Product()) : new Product();
        model.addAttribute("product", product);

        List<TableEntity> boardList = tableRepository.findByActiveOrderByTbindexAsc(1);
        model.addAttribute("boardList", boardList);

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
            String savePath = "C:/upload/thumb/" + fileName;

            File saveFile = new File(savePath);
            saveFile.getParentFile().mkdirs(); // 폴더 없으면 생성
            file.transferTo(saveFile);

            product.setImage(fileName);
            product.setRegdate(LocalDateTime.now());
        } else if (isEdit) {
            // 수정인데 이미지가 없을 경우 기존 이미지 유지
            Product existing = productRepository.findById(product.getNo()).orElse(null);
            if (existing != null) {
                product.setImage(existing.getImage());
            }
        }
        productRepository.save(product);
        return "redirect:/admin/products?category=all";
    }

    @GetMapping("/deleteitem")
    public String deleteProduct(@RequestParam Long id, RedirectAttributes redirectAttributes) {
        Optional<Product> optionalProduct = productRepository.findById(id);
        if (!optionalProduct.isPresent()) {
            redirectAttributes.addFlashAttribute("message", "해당 상품이 존재하지 않습니다.");
            return "redirect:/admin/products";
        }

        Product product = optionalProduct.get();

        // 썸네일 이미지 삭제
        if (product.getImage() != null) {
            File thumbFile = new File("C:/upload/thumb/" + product.getImage());
            if (thumbFile.exists()) {
                thumbFile.delete();
            }
        }

        // 본문 content 안의 이미지 경로 추출 후 삭제
        if (product.getContent() != null) {
            // 정규식으로 <img src="/upload/content/파일명"> 추출
            Pattern pattern = Pattern.compile("src=\"/upload/content/([^\"]+)\"");
            Matcher matcher = pattern.matcher(product.getContent());

            while (matcher.find()) {
                String filename = matcher.group(1);
                File contentImage = new File("C:/upload/content/" + filename);
                if (contentImage.exists()) {
                    contentImage.delete();
                }
            }
        }

        // DB에서 삭제
        productRepository.deleteById(id);

        redirectAttributes.addFlashAttribute("message", "상품이 성공적으로 삭제되었습니다.");
        return "redirect:/admin/products";
    }

    //summernote 이미지 삽입, 이미지 파일 업로드
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

    //summernote 에서 이미지 제거시 이미지 파일 삭제
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
