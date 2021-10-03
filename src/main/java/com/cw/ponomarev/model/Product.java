package com.cw.ponomarev.model;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Set;

/**
 * Класс, который содержит поля товара.
 * @author Денис Пономарев
 */
@Entity
@Table(name = "products_pon")
@Data
@ToString
public class Product {
    /**
     * Уникальный идентификатор, который генерируется автоматически.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Тип товара.
     * @see ProductType
     */
    @ElementCollection(targetClass = ProductType.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "product_type", joinColumns = @JoinColumn(name = "product_id"))
    @Enumerated(EnumType.STRING)
    private Set<ProductType> type;

    /**
     * Название товара.
     */
    @NotNull(message = "Обязательное поле для заполнения")
    @NotBlank(message = "Обязательное поле для заполнения")
    @NotEmpty(message = "Обязательное поле для заполнения")
    private String title;

    /**
     * Описание товара.
     */
    @NotNull(message = "Обязательное поле для заполнения")
    @NotBlank(message = "Обязательное поле для заполнения")
    @NotEmpty(message = "Обязательное поле для заполнения")
    private String description;

    /**
     * Количество товара на складе (в БД).
     */
    @Min(value = 0, message = "Количество товара должно быть >= 0")
    @NotNull(message = "Количество товара не должно быть пустым или меньше 0")
    private Long number;

    /**
     * Цена товара.
     */
    @Min(value = 0, message = "Цена не может быть отрицательной")
    @NotNull(message = "Цена товара не должна быть пустой или меньше 0")
    private Long price;

    /**
     * URL, по которому хранится изображение товара.
     */
    @Column(name = "image_url")
    private String imageUrl;

    /**
     * Заказ, к которому прикреплен товар.
     */
    @ManyToOne
    private Order order;

    /**
     * Метод, который изменяет тип товара, название, описание кол-во и цену
     * @param type - {@link #type}
     * @param title - {@link #title}
     * @param description - {@link #description}
     * @param number - {@link #number}
     * @param price - {@link #price}
     */
    public void setForChange(Set<ProductType> type, String title, String description, Long number, Long price){
        this.type = type;
        this.title = title;
        this.description = description;
        this.number = number;
        this.price = price;
    }
}
