package com.cw.ponomarev.model;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Entity
@Table(name = "products_pon")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ElementCollection(targetClass = ProductType.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "product_type", joinColumns = @JoinColumn(name = "product_id"))
    @Enumerated(EnumType.STRING)
    private Set<ProductType> type;

    @NotNull(message = "Should be not null")
    @NotBlank(message = "Should be not blank")
    private String title;

    @NotNull(message = "Should be not null")
    @NotBlank(message = "Should be not blank")
    private String description;

    private String availability;

    @Min(value = 0, message = "Количество товара должно быть >= 0")
    private Long number;

    @Min(value = 0, message = "Цена не может быть отрицательной")
    private Long price;

    @Column(name = "image_url")
    private String imageUrl;
}
