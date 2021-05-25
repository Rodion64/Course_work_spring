package com.cw.ponomarev.controller;

import com.cw.ponomarev.back.OrderService;
import com.cw.ponomarev.back.UserService;
import com.cw.ponomarev.model.Order;
import com.cw.ponomarev.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Map;

@Controller
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService service;
    private final UserService userService;

    @GetMapping
    public String orderPage(Model model, RedirectAttributes attributes, HttpServletRequest request){
        User user = userService.getUserByName(SecurityContextHolder.getContext().getAuthentication().getName());

        Order order = new Order();

        Map<String, ?> map = RequestContextUtils.getInputFlashMap(request);
        if(map != null) {
            for (Map.Entry<String, ?> entry : map.entrySet()) {
                model.addAttribute(entry.getKey(), entry.getValue());
            }
        }

        if(user != null){
            if(user.getPhone() == null ||
                    user.getAddress() == null ||
                    user.getRealName() == null ||
                    user.getSurname() == null) {
                attributes.addFlashAttribute("inputInfoMessage", "Введите ваши личные данные для дальнейшей обработки заказа");
                return "redirect:/user/userAccount";
            }
            order.setName(user.getRealName());
            order.setSurname(user.getSurname());
            order.setAddress(user.getAddress());
            order.setEmail(user.getEmail());
            order.setPhone(user.getPhone());
        }
        model.addAttribute("order", order);
        return "orderForm";
    }

    @PostMapping
    public String placeOrder(@ModelAttribute @Valid Order order, Errors errors,
                             @CookieValue(value = "cart") String cart,
                             @CookieValue(value = "orders", required = false) String orders,
                             Model model,
                             HttpServletResponse response, RedirectAttributes attributes){
        User user = userService.getUserByName(SecurityContextHolder.getContext().getAuthentication().getName());
        return service.placeOrder(user, order, errors, cart, orders, model, response, attributes);
    }

    @GetMapping("/list")
    public String getOrderList( @CookieValue(value = "orders", required = false) String cookie,
                                Model model) {
        User user = userService.getUserByName(SecurityContextHolder.getContext().getAuthentication().getName());
        return service.getOrderList(user, cookie, model);
    }

    @PostMapping("/delete/{id}")
    public String deleteOrder(@PathVariable(value = "id") Long id,
                              @CookieValue(value = "orders", required = false) String cookie,
                              HttpServletResponse response){
        User user = userService.getUserByName(SecurityContextHolder.getContext().getAuthentication().getName());
        return service.deleteOrder(id, cookie, user, response);
    }
}
