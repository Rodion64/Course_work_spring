package com.cw.ponomarev.controller;

import com.cw.ponomarev.model.Product;
import com.cw.ponomarev.model.ProductType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("/admin")
public class AdminController {

    @GetMapping("")
    public String adminPage(Model model) {
        ProductType[] productType = ProductType.values();
        model.addAttribute("prods", productType);
        model.addAttribute("product", new Product());

        return "adminPage";
    }

}
