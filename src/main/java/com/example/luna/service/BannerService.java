package com.example.luna.service;

import com.example.luna.entity.BannerEntity;
import com.example.luna.repository.BannerRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
public class BannerService {

    private final BannerRepository bannerRepository;
    private final String uploadPath = "C:/upload/banner/";

    public BannerService(BannerRepository bannerRepository) {
        this.bannerRepository = bannerRepository;
    }

    public void processBannerData(Map<String, String> params, MultiValueMap<String, MultipartFile> files) throws IOException {
        Set<Long> incomingIds = new HashSet<>();
        File dir = new File(uploadPath);
        if (!dir.exists()) dir.mkdirs();


        int index = 0;
        while (true) {
            String prefix = "items[" + index + "]";
            if (!params.containsKey(prefix + "[bannerid]")) break;

            String link = params.get(prefix + "[link]");
            int bindex = Integer.parseInt(params.get(prefix + "[bindex]"));
            String bannerIdStr = params.get(prefix + "[bannerid]");
            boolean active = "1".equals(params.get(prefix + "[active]"));

            MultipartFile file = files.getFirst(prefix + "[file]");
            BannerEntity banner;

            if ("#".equals(bannerIdStr)) {
                banner = new BannerEntity();
            } else {
                Long id = Long.parseLong(bannerIdStr);
                banner = bannerRepository.findById(id).orElse(new BannerEntity());
            }

            // 공통 처리
            banner.setLink(link);
            banner.setBindex(bindex);
            banner.setBactive(active); // 누락 가능성 있음

            if (file != null && !file.isEmpty()) {
                if (banner.getImage() != null) {
                    Path oldImage = Paths.get(uploadPath + banner.getImage());
                    Files.deleteIfExists(oldImage);
                }
                String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
                Path path = Paths.get(uploadPath + filename);
                file.transferTo(path.toFile());
                banner.setImage(filename);
            }

            bannerRepository.save(banner);

            // 무조건 저장한 아이디 추가 (신규든 기존이든)
            incomingIds.add(banner.getId());

            index++;
        }


        // DB에 남아있는 배너 중 전송된 항목에 없는 것들 삭제
        List<BannerEntity> allBanners = bannerRepository.findAll();
        for (BannerEntity banner : allBanners) {
            if (!incomingIds.contains(banner.getId())) {
                // 이미지도 삭제
                if (banner.getImage() != null) {
                    Files.deleteIfExists(Paths.get(uploadPath + banner.getImage()));
                }
                bannerRepository.delete(banner);
            }
        }
    }
}