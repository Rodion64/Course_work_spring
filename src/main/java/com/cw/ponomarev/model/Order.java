package com.cw.ponomarev.model;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;

/**
 * Класс, который содержит поля заказа.
 * @author Денис Пономарев
 */
@Entity
@Data
@Table(name = "order_ponom")
public class Order {
    /**
     * Уникальный идентификатор, который генерируется автоматически.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Пользователь, к которому привязан заказ.
     * @see User
     */
    @ManyToOne
    private User user;

    /**
     * Список товаров в заказе.
     * @see Product
     */
    @OneToMany(mappedBy = "order", fetch = FetchType.EAGER)
    private List<Product> products;

    /**
     * Статус заказа
     * @see OrderStatus
     */
    @ElementCollection(targetClass = OrderStatus.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "order_status_pon", joinColumns = @JoinColumn(name = "order_id"))
    @Enumerated(EnumType.STRING)
    private Set<OrderStatus> status;

    /**
     * Имя пользователя, который сделал заказ.
     */
    @NotNull(message = "Обязательное поле для заполнения")
    @NotBlank(message = "Обязательное поле для заполнения")
    @NotEmpty(message = "Обязательное поле для заполнения")
    private String name = "";

    /**
     * Фамилия пользователя, который сделал заказ.
     */
    @NotNull(message = "Обязательное поле для заполнения")
    @NotBlank(message = "Обязательное поле для заполнения")
    @NotEmpty(message = "Обязательное поле для заполнения")
    private String surname = "";

    /**
     * Адрес пользователя, который сделал заказ.
     */
    @NotNull(message = "Обязательное поле для заполнения")
    @NotBlank(message = "Обязательное поле для заполнения")
    @NotEmpty(message = "Обязательное поле для заполнения")
    private String address = "";

    /**
     * Адрес почты пользователя, который сделал заказ.
     */
    @Email(message = "incorrect email address")
    @NotBlank(message = "not empty")
    @NotNull(message = "not null")
    private String email = "";

    /**
     * Телефон пользователя, который сделал заказ.
     */
    @NotNull(message = "Обязательное поле для заполнения")
    @NotBlank(message = "Обязательное поле для заполнения")
    @NotEmpty(message = "Обязательное поле для заполнения")
    private String phone = "";
}
