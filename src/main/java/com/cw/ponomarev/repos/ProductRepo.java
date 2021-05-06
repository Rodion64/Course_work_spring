package com.cw.ponomarev.repos;

import com.cw.ponomarev.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepo extends JpaRepository<Product, Long> {
    Product findByTitle(String title);
}
