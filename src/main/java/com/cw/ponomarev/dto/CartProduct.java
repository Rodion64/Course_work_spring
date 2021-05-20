package com.cw.ponomarev.dto;

import com.cw.ponomarev.model.ProductType;
import lombok.Data;

import java.util.Set;

@Data
public class CartProduct {
    private Long id;
    private Set<ProductType> type;
    private String title;
    private String description;
    private Long price;
    private Long numberInCart;
    private Long numberInBD;
    private String imageUrl;
}
