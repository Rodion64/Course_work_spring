package com.cw.ponomarev.model;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "products")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ProductType type;

    @NotNull(message = "Should be not null")
    @NotBlank(message = "Should be not blank")
    private String title;

    private String description;

    private Long number;

    @NotNull(message = "Should has some price")
    @NotBlank(message = "Should be not blank")
    private Long price;

}
