package com.cw.ponomarev.back;

import com.cw.ponomarev.model.Product;
import com.cw.ponomarev.repos.ProductRepo;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import javax.validation.Valid;
import java.util.List;

@Service
public class AdminService {
    private final ProductRepo repository;

    public AdminService(ProductRepo repository) {
        this.repository = repository;
    }

    public String addNewPos(@ModelAttribute @Valid Product product, Errors errors, RedirectAttributes redirectAttributes){

        if(errors.hasErrors()){
            redirectAttributes.addFlashAttribute("currentTitle", product.getTitle());
            redirectAttributes.addFlashAttribute("currentDescription", product.getDescription());
            redirectAttributes.addFlashAttribute("currentNumber", product.getNumber());
            redirectAttributes.addFlashAttribute("currentPrice", product.getPrice());

            List<FieldError> list = errors.getFieldErrors();
            for (FieldError f : list) {
                redirectAttributes.addFlashAttribute(f.getField(), f.getDefaultMessage());
            }

            return "redirect:/admin";
        }

        return "redirect:/admin";
    }
}
