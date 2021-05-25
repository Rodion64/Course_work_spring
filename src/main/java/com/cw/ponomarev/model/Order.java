package com.cw.ponomarev.model;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;

@Entity
@Data
@Table(name = "order_ponom")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    @OneToMany(mappedBy = "order", fetch = FetchType.EAGER)
    private List<Product> products;

    @ElementCollection(targetClass = OrderStatus.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "order_status_pon", joinColumns = @JoinColumn(name = "order_id"))
    @Enumerated(EnumType.STRING)
    private Set<OrderStatus> status;

    @NotNull(message = "Обязательное поле для заполнения")
    @NotBlank(message = "Обязательное поле для заполнения")
    @NotEmpty(message = "Обязательное поле для заполнения")
    private String name = "";

    @NotNull(message = "Обязательное поле для заполнения")
    @NotBlank(message = "Обязательное поле для заполнения")
    @NotEmpty(message = "Обязательное поле для заполнения")
    private String surname = "";

    @NotNull(message = "Обязательное поле для заполнения")
    @NotBlank(message = "Обязательное поле для заполнения")
    @NotEmpty(message = "Обязательное поле для заполнения")
    private String address = "";

    @Email(message = "incorrect email address")
    @NotBlank(message = "not empty")
    @NotNull(message = "not null")
    private String email = "";

    @NotNull(message = "Обязательное поле для заполнения")
    @NotBlank(message = "Обязательное поле для заполнения")
    @NotEmpty(message = "Обязательное поле для заполнения")
    private String phone = "";
}
