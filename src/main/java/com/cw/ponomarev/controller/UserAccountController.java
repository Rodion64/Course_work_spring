package com.cw.ponomarev.controller;

import com.cw.ponomarev.back.UserService;
import com.cw.ponomarev.model.User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;


@Controller
@RequestMapping("/user")
public class UserAccountController {
    private final UserService service;

    public UserAccountController(UserService service) {
        this.service = service;
    }

    @GetMapping("/userAccount")
    public String getUserAccDet(Model model, HttpServletRequest request){
        User currentUser = service.getUserByName(SecurityContextHolder.getContext().getAuthentication().getName());

        Map<String, ?> map = RequestContextUtils.getInputFlashMap(request);

        if(map != null) {
            for (Map.Entry<String, ?> entry : map.entrySet()) {
                model.addAttribute(entry.getKey(), entry.getValue());
            }
        }

        model.addAttribute("phone", currentUser.getPhone() == null ? "" : currentUser.getPhone());
        model.addAttribute("name", currentUser.getName() == null ? "" : currentUser.getName());
        model.addAttribute("address", currentUser.getAddress() == null ? "" : currentUser.getAddress());
        model.addAttribute("realName", currentUser.getRealName() == null ? "" : currentUser.getRealName());
        model.addAttribute("surname", currentUser.getSurname() == null ? "" : currentUser.getSurname());

        return "userAccountDetails";
    }

    @PostMapping("/updateUser")
    public String updateUser(@ModelAttribute("phone") String phone,
                           @ModelAttribute("address") String address,
                             @ModelAttribute("realName") String realName,
                             @ModelAttribute("surname") String surname,
                             RedirectAttributes redirectAttributes) {
        boolean falseFlag = false;
        User user = service.getUserByName(SecurityContextHolder.getContext().getAuthentication().getName());


        if(phone.isEmpty()){
            redirectAttributes.addFlashAttribute("currentPhone", phone);
            redirectAttributes.addFlashAttribute("phoneErr", "Введите ваш номер телефона");
            falseFlag = true;
        }

        if(address.isEmpty()){
            redirectAttributes.addFlashAttribute("currentAddress", address);
            redirectAttributes.addFlashAttribute("addressErr", "Введите адрес");
            falseFlag = true;
        }

        if(realName.isEmpty()){
            redirectAttributes.addFlashAttribute("currentRealName", realName);
            redirectAttributes.addFlashAttribute("realNameErr", "Введите ваше имя");
            falseFlag = true;
        }

        if(surname.isEmpty()){
            redirectAttributes.addFlashAttribute("currentSurname", surname);
            redirectAttributes.addFlashAttribute("surnameErr", "Введите вашу фамилию");
            falseFlag = true;
        }

        if (falseFlag)
            return "redirect:/user/userAccount";

        user.setAddress(address);
        user.setPhone(phone);
        user.setRealName(realName);
        user.setSurname(surname);

        service.saveOrUpdate(user);

        return "redirect:/user/userAccount";
    }

    @PostMapping("/updatePassword")
    public String updatePassword(@ModelAttribute("oldPassword") String oldPassword,
                                 @ModelAttribute("passwordFirstTry") String passwordFirstTry,
                                 @ModelAttribute("passwordSecondTry") String passwordSecondTry,
                                 RedirectAttributes redirectAttributes){
        User user = service.getUserByName(SecurityContextHolder.getContext().getAuthentication().getName());

        boolean errFlag = false;

        if(!service.matches(oldPassword, user.getPassword())){
            redirectAttributes.addFlashAttribute("oldPasswordErr", "Введите старый пароль");
            return "redirect:/user/userAccount";
        }

        if(passwordFirstTry.length() < 8 || passwordSecondTry.length() < 8){
            redirectAttributes.addFlashAttribute("newPasswordErr", "Длина нового пароля должна быть не менее 8 знаков");
            errFlag = true;
        }
        if (!service.equalsPassword(passwordFirstTry, passwordSecondTry)){
            redirectAttributes.addFlashAttribute("newPasswordErr", "Пожалуйста, проверьте введенный пароль");
            errFlag = true;
        }
        if(service.equalsPassword(oldPassword, passwordFirstTry)){
            redirectAttributes.addFlashAttribute("oldPasswordErr", "Новый пароль должен отличаться от старого");
            errFlag = true;
        }

        if (errFlag)
            return "redirect:/user/userAccount";


        user.setPassword(service.encode(passwordFirstTry));
        service.saveOrUpdate(user);
        return "redirect:/login";
    }
}
