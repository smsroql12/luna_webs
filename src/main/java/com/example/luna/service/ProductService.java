package com.example.luna.service;

import com.example.luna.entity.Product;
import com.example.luna.repository.ProductRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Page<Product> searchProducts(String category, String search, int page, int size, String sortOption) {
        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (!"all".equalsIgnoreCase(category)) {
                predicates.add(cb.equal(root.get("category"), Integer.parseInt(category)));
            }

            if (search != null && !search.isEmpty()) {
                Predicate nameLike = cb.like(root.get("name"), "%" + search + "%");
                Predicate descLike = cb.like(root.get("description"), "%" + search + "%");
                predicates.add(cb.or(nameLike, descLike));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 정렬 처리
        Sort sort;
        switch (sortOption) {
            case "old":
                sort = Sort.by(Sort.Direction.ASC, "regdate");
                break;
            case "phigh":
            case "plow":
                // 가격 정렬은 DB에서 처리 어려움. 나중에 리스트로 받아서 정렬함.
                Page<Product> result = productRepository.findAll(spec, PageRequest.of(page, size));
                List<Product> sorted = result.getContent().stream()
                        .sorted((a, b) -> {
                            int aPrice = a.isSale() ? a.getSaleprice() : a.getPrice();
                            int bPrice = b.isSale() ? b.getSaleprice() : b.getPrice();
                            return sortOption.equals("phigh") ? Integer.compare(bPrice, aPrice) : Integer.compare(aPrice, bPrice);
                        })
                        .collect(Collectors.toList());
                return new PageImpl<>(sorted, PageRequest.of(page, size), result.getTotalElements());

            case "new":
            default:
                sort = Sort.by(Sort.Direction.DESC, "regdate");
                break;
        }

        Pageable pageable = PageRequest.of(page, size, sort);
        return productRepository.findAll(spec, pageable);
    }

    public Page<Product> searchProductsByType(String type, String search, int page, int size, String sortOption) {
        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.isEmpty()) {
                if ("name".equalsIgnoreCase(type)) {
                    predicates.add(cb.like(root.get("name"), "%" + search + "%"));
                } else if ("code".equalsIgnoreCase(type)) {
                    try {
                        long codeValue = Long.parseLong(search.trim());
                        predicates.add(cb.equal(root.get("no"), codeValue));
                    } catch (NumberFormatException e) {
                        predicates.add(cb.disjunction()); // 항상 false
                    }
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        switch (sortOption) {
            case "old":
                return productRepository.findAll(spec, PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "regdate")));

            case "phigh":
            case "plow":
                Page<Product> result = productRepository.findAll(spec, PageRequest.of(page, size));
                List<Product> sorted = result.getContent().stream()
                        .sorted((a, b) -> {
                            int aPrice = a.isSale() ? a.getSaleprice() : a.getPrice();
                            int bPrice = b.isSale() ? b.getSaleprice() : b.getPrice();
                            return sortOption.equals("phigh")
                                    ? Integer.compare(bPrice, aPrice) // 높은 가격 순
                                    : Integer.compare(aPrice, bPrice); // 낮은 가격 순
                        })
                        .collect(Collectors.toList());
                return new PageImpl<>(sorted, PageRequest.of(page, size), result.getTotalElements());

            case "new":
            default:
                return productRepository.findAll(spec, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "regdate")));
        }
    }
}
