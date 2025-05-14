package com.example.luna.service;

import com.example.luna.entity.Product;
import com.example.luna.repository.ProductRepository;
import jakarta.persistence.criteria.Predicate;
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
