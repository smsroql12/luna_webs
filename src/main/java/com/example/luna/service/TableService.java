package com.example.luna.service;

import com.example.luna.entity.TableEntity;
import com.example.luna.repository.ProductRepository;
import com.example.luna.repository.TableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TableService {

    private final TableRepository tableRepository;
    private final ProductRepository productRepository;

    @Transactional
    public void saveBoardList(List<Map<String, String>> boardList) {
        // 1. 기존 DB에서 boardid 전부 가져오기
        List<Long> existingIds = tableRepository.findAllIds();
        Set<Long> incomingIds = new HashSet<>();

        for (Map<String, String> board : boardList) {
            String boardIdStr = board.get("boardid");
            String boardName = board.get("boardname");
            int boardIndex = Integer.parseInt(board.get("boardindex"));
            int active = "y".equalsIgnoreCase(board.get("boardactive")) ? 1 : 0;

            if ("#".equals(boardIdStr)) {
                // 새로 추가된 항목 (INSERT)
                TableEntity newBoard = new TableEntity();
                newBoard.setName(boardName);
                newBoard.setTbindex(boardIndex);
                newBoard.setActive(active);
                tableRepository.save(newBoard);
            } else {
                Long boardId = Long.parseLong(boardIdStr);
                incomingIds.add(boardId);
                tableRepository.findById(boardId).ifPresent(existing -> {
                    existing.setName(boardName);
                    existing.setTbindex(boardIndex);
                    existing.setActive(active);
                    tableRepository.save(existing); // (UPDATE)
                });
            }
        }

        // 2. 삭제 처리: 기존 DB에만 있고 JSON에 없는 id 제거
        List<Long> idsToDelete = existingIds.stream()
                .filter(id -> !incomingIds.contains(id))
                .collect(Collectors.toList());

        if (!idsToDelete.isEmpty()) {
            // 1. 상품 먼저 삭제
            productRepository.deleteByCategoryIn(idsToDelete);

            // 2. 게시판 삭제
            tableRepository.deleteAllById(idsToDelete);
        }
    }
}
