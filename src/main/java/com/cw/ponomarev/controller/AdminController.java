package com.cw.ponomarev.controller;

import com.cw.ponomarev.back.AdminService;
import com.cw.ponomarev.model.Product;
import com.cw.ponomarev.model.ProductType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;


@Controller
@RequestMapping("/admin")
public class AdminController {
    private final AdminService service;

    public AdminController(AdminService service) {
        this.service = service;
    }

    @GetMapping("")
    public String adminPage(Model model, HttpServletRequest request) {
        ProductType[] productType = ProductType.values();
        model.addAttribute("prods", productType);
        model.addAttribute("prodss", productType);

        Map<String, ?> map = RequestContextUtils.getInputFlashMap(request);
        if(map != null) {
            for (Map.Entry<String, ?> entry : map.entrySet()) {
                model.addAttribute(entry.getKey(), entry.getValue());
            }
        }

        return "adminPage";
    }

    @PostMapping("/addNewPosition")
    public String addNewPosition(@ModelAttribute @Valid Product product, Errors errors, RedirectAttributes redirectAttributes, @RequestParam(name = "image") MultipartFile file){
         return  service.addNewPos(product, file, errors, redirectAttributes);
    }

    @GetMapping("/userList")
    public String getUserList(Model model){
        return service.userList(model);
    }

    @GetMapping("/checkClickedButton/{prod}")
    public String checkButton(Model model, @PathVariable(name = "prod") ProductType type, RedirectAttributes redirectAttributes){
        return service.getCheckedListForm(model, type, redirectAttributes);
    }

    @PostMapping("/deleteProduct/{id}")
    public String deleteProduct(@PathVariable(name = "id") Long id){
        return service.deleteProd(id);
    }

    @GetMapping("/changeProduct/{id}")
    public String changeProduct(@PathVariable(name = "id") Long id, Model model){
        return service.changeProductForm(id, model);
    }

    //Квуфудадидабифиждбц
    @PostMapping("/changeProduct/{id}")
    public String changeProduct(@ModelAttribute Product product, RedirectAttributes redirectAttributes, @PathVariable(name = "id") Long id){
        System.out.println(product);
        return service.changeProduct(product, redirectAttributes, id);
    }

    @GetMapping("/changeImage/{id}")
    public String changeImageForm(@PathVariable(name = "id") Long id, Model model){
        return service.changeImageForm(id, model);
    }
}
