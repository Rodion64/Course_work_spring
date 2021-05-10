package com.cw.ponomarev.back;

import com.cw.ponomarev.model.Product;
import com.cw.ponomarev.model.ProductType;
import com.cw.ponomarev.model.User;
import com.cw.ponomarev.repos.ProductRepo;
import com.cw.ponomarev.repos.UserRepo;
import org.dom4j.rule.Mode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AdminService {
    @Value("${upload.path}")
    String uploadPath;

    private final ProductRepo repository;
    private final UserRepo userRepo;

    public AdminService(ProductRepo repository, UserRepo userRepo) {
        this.repository = repository;
        this.userRepo = userRepo;
    }

    public String addNewPos(Product product, MultipartFile file, Errors errors, RedirectAttributes redirectAttributes){
        boolean flagOfErrors = false;

        if(errors.hasErrors()){
            flagOfErrors = true;

            redirectAttributes.addFlashAttribute("currentTitle", product.getTitle());
            redirectAttributes.addFlashAttribute("currentDescription", product.getDescription());
            redirectAttributes.addFlashAttribute("currentNumber", product.getNumber());
            redirectAttributes.addFlashAttribute("currentPrice", product.getPrice());

            List<FieldError> list = errors.getFieldErrors();
            for (FieldError f : list) {
                redirectAttributes.addFlashAttribute(f.getField(), f.getDefaultMessage());
            }
        }

        if(repository.findByTitle(product.getTitle()) != null) {
            flagOfErrors = true;
            redirectAttributes.addFlashAttribute("exists", "Товар с таким названием уже существует");
        }

        if(product.getNumber() != null) {
            if (product.getNumber() == 0 && product.getAvailability().equals("Есть на складе")) {
                flagOfErrors = true;
                redirectAttributes.addFlashAttribute("availabilityErr", "Товар не может быть на складе с указанным количеством");
            }
        }


        if(!file.getOriginalFilename().equals("")) {
            String uuid = UUID.randomUUID().toString();
            String nameOfFile = uuid + file.getOriginalFilename();
            String filePath = uploadPath + "/" + nameOfFile;

            product.setImageUrl(nameOfFile);
            try {
                file.transferTo(new File(filePath));
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            flagOfErrors = true;
            redirectAttributes.addFlashAttribute("fileErr", "Файл должен иметь название и не должен быть пустым");
        }

        if (!flagOfErrors)
            repository.save(product);

        return "redirect:/admin";
    }

    public String userList(Model model){
        List<User> users = userRepo.findAll();
        model.addAttribute("users", users);
        return "userList";
    }

    public List<Product> getProducts(){
        return repository.findAll();
    }

    public String getCheckedListForm(Model model, ProductType type, RedirectAttributes redirectAttributes) {
        List<Product> neededProducts = repository.findAllByType(type);
        redirectAttributes.addFlashAttribute("list", neededProducts);
        return "redirect:/admin";
    }

    public String deleteProd(Long id){
        repository.deleteById(id);
        return "redirect:/admin";
    }

    public String changeProductForm(Long id, Model model) {
        model.addAttribute("currentProduct", repository.findById(id).get());
        model.addAttribute("types", ProductType.values());
        return "change_product";
    }

    public String changeProduct(Product product, Errors errors, RedirectAttributes redirectAttributes){
        boolean flagOfErrors = false;

        if(errors.hasErrors()){
            flagOfErrors = true;

            redirectAttributes.addFlashAttribute("currentTitle", product.getTitle());
            redirectAttributes.addFlashAttribute("currentDescription", product.getDescription());
            redirectAttributes.addFlashAttribute("currentNumber", product.getNumber());
            redirectAttributes.addFlashAttribute("currentPrice", product.getPrice());

            List<FieldError> list = errors.getFieldErrors();
            for (FieldError f : list) {
                redirectAttributes.addFlashAttribute(f.getField(), f.getDefaultMessage());
            }
        }

        if(repository.findByTitle(product.getTitle()) != null) {
            flagOfErrors = true;
            redirectAttributes.addFlashAttribute("exists", "Товар с таким названием уже существует");
        }

        if(product.getNumber() != null) {
            if (product.getNumber() == 0 && product.getAvailability().equals("Есть на складе")) {
                flagOfErrors = true;
                redirectAttributes.addFlashAttribute("availabilityErr", "Товар не может быть на складе с указанным количеством");
            }
        }


        if(!file.getOriginalFilename().equals("")) {
            String uuid = UUID.randomUUID().toString();
            String nameOfFile = uuid + file.getOriginalFilename();
            String filePath = uploadPath + "/" + nameOfFile;

            product.setImageUrl(nameOfFile);
            try {
                file.transferTo(new File(filePath));
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            flagOfErrors = true;
            redirectAttributes.addFlashAttribute("fileErr", "Файл должен иметь название и не должен быть пустым");
        }

        if (!flagOfErrors)
            repository.save(product);

        return "redirect:/admin";
    }
}
