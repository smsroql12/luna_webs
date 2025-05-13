package com.example.luna.service;

import com.example.luna.entity.Product;
import com.example.luna.repository.ProductRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Page<Product> searchProducts(String category, String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "no"));
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

        return productRepository.findAll(spec, pageable);
    }
}
