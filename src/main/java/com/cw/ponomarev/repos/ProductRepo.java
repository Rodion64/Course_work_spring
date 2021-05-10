package com.cw.ponomarev.repos;

import com.cw.ponomarev.model.Product;
import com.cw.ponomarev.model.ProductType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepo extends JpaRepository<Product, Long> {
    Product findByTitle(String title);
    List<Product> findAllByType(ProductType type);
}
