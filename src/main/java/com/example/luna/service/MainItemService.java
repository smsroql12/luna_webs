package com.example.luna.service;

import com.example.luna.entity.MainItemEntity;
import com.example.luna.repository.MainItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Transactional
@Service
public class MainItemService {
    private final MainItemRepository mainItemRepository;
    public MainItemService(MainItemRepository mainItemRepository) {
        this.mainItemRepository = mainItemRepository;

    }

    public void processMainItemData(Map<String, String> params) throws IOException {
        Set<Long> incomingIds = new HashSet<>();
        int index = 0;
        while (true) {
            String prefix = "items[" + index + "]";
            if (!params.containsKey(prefix + "[no]")) break; // <-- 여기 'no' 로 수정

            int mindex = Integer.parseInt(params.get(prefix + "[mindex]"));
            String itemNoStr = params.get(prefix + "[no]");
            boolean active = "1".equals(params.get(prefix + "[active]"));

            int itemcode = 0;
            if (params.containsKey(prefix + "[itemcode]")) {
                try {
                    itemcode = Integer.parseInt(params.get(prefix + "[itemcode]"));
                } catch (NumberFormatException e) {
                    itemcode = 0; // 또는 예외 처리
                }
            }

            MainItemEntity item;
            if ("#".equals(itemNoStr)) {
                item = new MainItemEntity(); // 신규
            } else {
                Long id = Long.parseLong(itemNoStr);
                item = mainItemRepository.findById(id).orElse(new MainItemEntity());
            }

            item.setMindex(mindex);
            item.setActive(active);
            item.setItemcode(itemcode);

            mainItemRepository.save(item);
            incomingIds.add(item.getNo());

            index++;
        }


        // DB에 남아있는 아이템 중 전송된 항목에 없는 것들 삭제
        List<MainItemEntity> allItems = mainItemRepository.findAll();
        for (MainItemEntity item : allItems) {
            if (!incomingIds.contains(item.getNo())) {
                mainItemRepository.delete(item);
            }
        }
    }
}
