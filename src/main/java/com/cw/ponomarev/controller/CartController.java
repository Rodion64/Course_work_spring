package com.cw.ponomarev.controller;

import com.cw.ponomarev.back.CartService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.websocket.server.PathParam;

@Controller
@RequestMapping("/cart")
public class CartController {
    private final CartService service;

    public CartController(CartService service) {
        this.service = service;
    }

    @GetMapping
    public String page(@CookieValue(value = "cart", required = false) String cookie, Model model, HttpServletResponse response){
        return service.getPage(cookie, model, response);
    }

    @PostMapping("/changeNumberInCart/{id}")
    public String changeNumber(@PathVariable(name = "id") Long id, @PathParam(value = "newNumber") Long newNumber,
                               @CookieValue(name = "cart") String cookie, HttpServletResponse response){
        return service.changeNumberProduct(id, newNumber, cookie, response);
    }

    @PostMapping("/deletePosition/{id}")
    public String delete(@PathVariable(name = "id") Long id,
                               @CookieValue(name = "cart") String cookie,
                               HttpServletResponse response){
        return service.changeNumberProduct(id, 0L, cookie, response);
    }
}
