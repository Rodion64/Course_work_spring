package com.cw.ponomarev.model;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Entity
@Table(name = "products_pon")
@Data
@ToString
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ElementCollection(targetClass = ProductType.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "product_type", joinColumns = @JoinColumn(name = "product_id"))
    @Enumerated(EnumType.STRING)
    private Set<ProductType> type;

    @NotNull(message = "Обязательное поле для заполнения")
    @NotBlank(message = "Обязательное поле для заполнения")
    @NotEmpty(message = "Обязательное поле для заполнения")
    private String title;

    @NotNull(message = "Обязательное поле для заполнения")
    @NotBlank(message = "Обязательное поле для заполнения")
    @NotEmpty(message = "Обязательное поле для заполнения")
    private String description;

    @Min(value = 0, message = "Количество товара должно быть >= 0")
    @NotNull(message = "Количество товара не должно быть пустым или меньше 0")
    private Long number;

    @Min(value = 0, message = "Цена не может быть отрицательной")
    @NotNull(message = "Цена товара не должна быть пустой или меньше 0")
    private Long price;

    @Column(name = "image_url")
    private String imageUrl;

    @ManyToOne
    private Order order;

    public void setForChange(Set<ProductType> type, String title, String description, Long number, Long price){
        this.type = type;
        this.title = title;
        this.description = description;
        this.number = number;
        this.price = price;
    }
}
