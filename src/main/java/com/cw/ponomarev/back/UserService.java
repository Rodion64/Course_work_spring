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

@Service
public class UserService {
    private final UserRepo repository;
    private final BCryptPasswordEncoder encoder;
    private final UserDetService details;

    public UserService(UserRepo repository, BCryptPasswordEncoder encoder, UserDetService details) {
        this.repository = repository;
        this.encoder = encoder;
        this.details = details;
    }

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

    public void saveOrUpdate(User user){
        repository.save(user);
    }

    public User getUserByName(String name){
        return repository.findUserByName(name);
    }

    public boolean equalsPassword(String pas1, String pas2){
        return encoder.matches(pas1, pas2);
    }

    public String encode(String pass){
        return encoder.encode(pass);
    }

    public boolean matches(String password, String currentPassword){
        return encoder.matches(password, currentPassword);
    }

}
