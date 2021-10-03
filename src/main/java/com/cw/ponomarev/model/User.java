package com.cw.ponomarev.model;

import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Класс, который содержит поля пользователя.
 * @author Денис Пономарев
 */
@Entity
@Table(name = "usr")
@Data
public class User implements UserDetails {
    /**
     * Уникальный идентификатор, который генерируется автоматически.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Логин пользователя.
     */
    @NotBlank(message = "not empty")
    @NotNull(message = "not null")
    @Size(min = 5, message = "At least five characters")
    private String name;

    /**
     * Адрес электронной почты пользователя.
     */
    @Email(message = "incorrect email address")
    @NotBlank(message = "not empty")
    @NotNull(message = "not null")
    private String email;

    /**
     * Пароль пользователя.
     */
    @NotBlank(message = "not empty")
    @NotNull(message = "not null")
    @Size(min = 8, message = "Length of password should be from 8 to 24 characters")
    private String password;

    /**
     * Имя пользователя.
     */
    @Column(name = "real_name")
    private String realName;

    /**
     * Фамилия пользователя.
     */
    private String surname;

    /**
     * Номер телефона пользователя.
     */
    private String phone;

    /**
     * Адрес проживания пользователя.
     */
    private String address;

    /**
     * Поле, которое отвечает за то, забанен пользователь или нет (true - не забанен).
     */
    private boolean active;

    /**
     * Роль пользователя.
     * @see Role
     */
    @ElementCollection(targetClass = Role.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "user_role", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    private Set<Role> roles;

    /**
     * Заказы пользователя.
     * @see Order
     */
    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
    private List<Order> orders;

    /**
     * Метод получения роли пользователя.
     * @return Возвращает роль.
     * @see Role
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles;
    }

    /**
     * Метод, возвращающий логин пользователя.
     * @return Возвращает логин пользователя.
     */
    @Override
    public String getUsername() {
        return name;
    }

    @Override
    public boolean isAccountNonExpired() {
        return active;
    }

    @Override
    public boolean isAccountNonLocked() {
        return active;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return active;
    }

    /**
     * Метод проверки, забанен пользователь или нет.
     * @return {@link #active}
     */
    @Override
    public boolean isEnabled() {
        return active;
    }
}
