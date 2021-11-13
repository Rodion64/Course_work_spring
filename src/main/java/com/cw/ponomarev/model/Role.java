package com.cw.ponomarev.model;

import org.springframework.security.core.GrantedAuthority;

/**
 * Перечисление ролей, которые могут быть у пользователя системы.
 * @see User
 */
public enum Role implements GrantedAuthority {
    USER, ADMIN, WORKER, PR5;

    /**
     * Метод, возвращающий роль.
     * @return Возвращает роль в String представлении.
     */
    @Override
    public String getAuthority() {
        return name();
    }
}
