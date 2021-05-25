package com.cw.ponomarev.controller;

import com.cw.ponomarev.back.CartService;
import com.cw.ponomarev.back.UserService;
import com.cw.ponomarev.model.Product;
import com.cw.ponomarev.model.ProductType;
import com.cw.ponomarev.model.Role;
import com.cw.ponomarev.model.User;
import com.cw.ponomarev.repos.ProductRepo;
import org.springframework.security.core.Authentication;
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
import java.util.List;
import java.util.Map;

@Controller
public class SimpleController {
    private final UserService service;
    private final ProductRepo productRepo;  //change this
    private final CartService cartService;

    public SimpleController(UserService service, ProductRepo productRepo, CartService cartService) {
        this.service = service;
        this.productRepo = productRepo;
        this.cartService = cartService;
    }


    @GetMapping("/registration")
    public String registrationForm() {
        return "registration";
    }

    @GetMapping("/registrationAcc")
    public String addUser(@ModelAttribute @Valid User user,
                          Errors errors,
                          @RequestParam Role role,
                          Model model) {
        return service.addUser(user, errors, role, model);
    }

    @GetMapping
    public String afterLogin(@CookieValue(value = "cart", required = false) String cart,
                             HttpServletRequest request,
                             HttpServletResponse response,
                             Model model){
        Authentication authUser = SecurityContextHolder.getContext().getAuthentication();
        model.addAttribute("cartSize", cartService.getSize(cart, response));

        if(RequestContextUtils.getInputFlashMap(request) != null){
            Map<String, ?> map = RequestContextUtils.getInputFlashMap(request);
            for (Map.Entry<String, ?> entry : map.entrySet()) {
                model.addAttribute(entry.getKey(), entry.getValue());
            }
        } else {
            model.addAttribute("list", productRepo.findAllByType(ProductType.PROCESSOR));
        }
        ProductType[] productType = ProductType.values();
        model.addAttribute("types", productType);

        model.addAttribute("isAuthorized", authUser.getPrincipal()!="anonymousUser");
        model.addAttribute("isAdmin", authUser.getAuthorities().contains(Role.ADMIN));

        return "index";
    }

    @GetMapping("/checkClickedButton/{type}")
    public String checkType(Model model, @PathVariable(name = "type") ProductType type, RedirectAttributes redirectAttributes){
        List<Product> neededProducts = productRepo.findAllByType(type);
        redirectAttributes.addFlashAttribute("list", neededProducts);
        return "redirect:/";
    }

    @PostMapping("/addToCart/{id}")
    public String addToCart(@CookieValue(value = "cart", required = false) String cart,
                            @PathVariable("id") Long id,
                            HttpServletResponse response){
        return cartService.add(cart, id, response);
    }
}
