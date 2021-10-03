package com.cw.ponomarev.back;

import com.cw.ponomarev.model.User;
import com.cw.ponomarev.repos.UserRepo;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Класс, который содержит методы для корректной работы Spring Security.
 * @author Денис Пономарев
 */
@Service
public class UserDetService implements UserDetailsService {
    /**
     * Репозиторий, содержащий все записи пользователей в БД.
     * @see User
     */
    private final UserRepo repo;

    /**
     * Конструктор, который автоматически встраивает зависимость от {@link UserRepo}
     * @param repo - {@link #repo}
     */
    public UserDetService(UserRepo repo) {
        this.repo = repo;
    }

    /**
     * Метод, который возвращает пользователя по его имени.
     * @param name - имя пользователя.
     * @return Пользователь с переданным именем.
     * @throws UsernameNotFoundException - если не был найден пользователь.
     */
    @Override
    public UserDetails loadUserByUsername(String name) throws UsernameNotFoundException {
        return repo.findUserByName(name);
    }
}
