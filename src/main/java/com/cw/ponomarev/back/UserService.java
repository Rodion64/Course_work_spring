package com.cw.ponomarev.back;

import com.cw.ponomarev.model.Role;
import com.cw.ponomarev.model.User;
import com.cw.ponomarev.repos.UserRepo;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.util.Collections;
import java.util.List;

/**
 * Класс, который содержит логику работы с {@link User}.
 * @author Денис Пономарев
 */
@Service
public class UserService {
    /**
     * Репозиторий, который содержит все записи пользователей системы.
     * @see User
     */
    private final UserRepo repository;
    /**
     * Криптографический класс для шифровки / расшифровки паролей пользователей.
     */
    private final BCryptPasswordEncoder encoder;
    /**
     * @see UserDetService
     */
    private final UserDetService details;

    /**
     * Конструктор, который автоматически встраивает зависимости.
     * @param repository - {@link #repository}
     * @param encoder - {@link #encoder}
     * @param details - {@link #details}
     */
    public UserService(UserRepo repository, BCryptPasswordEncoder encoder, UserDetService details) {
        this.repository = repository;
        this.encoder = encoder;
        this.details = details;
    }

    /**
     * Метод, который добавляет в БД нового пользователя.
     * @param user - пользователь, которого необходимо добавить в БД.
     * @param errors - в случае ошибки создания пользователю в model запишутся ошибки, которые отобразятся на странице
     * @param role - роль создаваемого пользователя ({@link Role}).
     * @param model - MVC класс, в который добавляются атрибуты для отображения на странице (currentName, currentPassword,
     *              currentEmail, errorAcc)
     * @return Если errors пустое, то в БД добавляется новый пользователь и происходит переход на страницу логина.
     * В противном случае заполняется model и возвращается страница регистрации.
     * @see User
     * @see Role
     */
    public String addUser(@ModelAttribute @Valid User user, Errors errors, @RequestParam Role role, Model model) {
        if (errors.hasErrors()) {
            model.addAttribute("currentName",user.getName());
            model.addAttribute("currentPassword",user.getPassword());
            model.addAttribute("currentEmail",user.getEmail());

            List<FieldError> list = errors.getFieldErrors();
            for (FieldError f : list) {
                model.addAttribute(f.getField(), f.getDefaultMessage());
            }

            if (details.loadUserByUsername(user.getName()) != null) {
                model.addAttribute("errorAcc", "Пользователь с таким именем уже существует.");
            }
            return "/registration";
        }

        user.setRoles(Collections.singleton(role));
        user.setPassword(encode(user.getPassword()));
        user.setActive(true);
        repository.save(user);

        return "redirect:/login";
    }

    /**
     * Метод, который сохраняет пользователя в БД или обновляет данные по пользователю, если тот уже содержится в БД.
     * @param user - пользователь, поля которого надо сохранить или обновить.
     * @see User
     * {@link #repository}
     */
    public void saveOrUpdate(User user){
        repository.save(user);
    }

    /**
     * Метод, который возвращает пользователя по его имени.
     * @param name - имя пользователя.
     * @return Пользователь с заданным именем.
     * @see User
     * {@link #repository}
     */
    public User getUserByName(String name){
        return repository.findUserByName(name);
    }


    /**
     * Метод, который сравнивает пароли.
     * @param pas1 - первый пароль.
     * @param pas2 - второй пароль.
     * @return Boolean значение: true, если пароли совпали.
     * {@link #encoder}
     */
    public boolean equalsPassword(String pas1, String pas2){
        return encoder.matches(pas1, pas2);
    }


    /**
     * Метод, который кодирует пароль.
     * @param pass - исходный пароль.
     * @return Закодированный пароль.
     * @see #encoder
     */
    public String encode(String pass){
        return encoder.encode(pass);
    }
    /**
     * Метод, который сравнивает пароли.
     * @param password - первый пароль.
     * @param currentPassword - второй пароль.
     * @return Boolean значение: true, если пароли совпали.
     * @see #encoder
     */
    public boolean matches(String password, String currentPassword){
        return encoder.matches(password, currentPassword);
    }

}
