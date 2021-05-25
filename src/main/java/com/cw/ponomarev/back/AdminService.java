package com.cw.ponomarev.back;

import com.cw.ponomarev.model.Product;
import com.cw.ponomarev.model.ProductType;
import com.cw.ponomarev.model.Role;
import com.cw.ponomarev.model.User;
import com.cw.ponomarev.repos.ProductRepo;
import com.cw.ponomarev.repos.UserRepo;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
public class AdminService {
    @Value("${upload.path}")
    String uploadPath;

    private final ProductRepo repository;
    private final UserRepo userRepo;
    private final BCryptPasswordEncoder encoder;

    public AdminService(ProductRepo repository, UserRepo userRepo, BCryptPasswordEncoder encoder) {
        this.repository = repository;
        this.userRepo = userRepo;
        this.encoder = encoder;
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

        Role [] roles = Role.values();
        model.addAttribute("userRoles", roles);
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
        String imageUrl = uploadPath + "/" + repository.findById(id).get().getImageUrl();
        repository.deleteById(id);

        FileUtils.deleteQuietly(new File(imageUrl));
        return "redirect:/admin";
    }

    public String changeProductForm(Long id, Model model, Map<String, ?> attributes) {
        if(attributes != null) {
            for (Map.Entry<String, ?> entry : attributes.entrySet()) {
                model.addAttribute(entry.getKey(), entry.getValue());
            }
        }

        model.addAttribute("currentProduct", repository.findById(id).get());
        model.addAttribute("types", ProductType.values());
        return "change_product";
    }

    public String changeProduct(Product product, RedirectAttributes redirectAttributes, Long id){
        boolean flagOfErrors = false;

        Product dataBaseProduct = repository.findById(id).get();

        if(product.getTitle().equals("")){
            flagOfErrors = true;
            redirectAttributes.addFlashAttribute("titleErr", "Обязательное поле для заполнения");
        }
        if(product.getDescription().equals("")){
            flagOfErrors = true;
            redirectAttributes.addFlashAttribute("descriptionErr", "Обязательное поле для заполнения");
        }
        if(product.getType().isEmpty()){
            flagOfErrors = true;
            redirectAttributes.addFlashAttribute("selectingTypeErr", "Необходимо выбрать тип товара");
        }
        if(product.getNumber() == null){
            flagOfErrors = true;
            redirectAttributes.addFlashAttribute("numberErr", "Введите количество товара");
        }
        if(product.getPrice() == null){
            flagOfErrors = true;
            redirectAttributes.addFlashAttribute("priceErr", "Введите стоимость товара");
        }

        if (!flagOfErrors){
            dataBaseProduct.setForChange(product.getType(), product.getTitle(), product.getDescription(), product.getNumber(), product.getPrice());
            repository.save(dataBaseProduct);
            return "redirect:/admin";
        }

        return "redirect:/admin/changeProduct/" + id;
    }

    public String changeImageForm(Long id, Model model, Map<String, ?> map){
        if(map != null) {
            for (Map.Entry<String, ?> entry : map.entrySet()) {
                model.addAttribute(entry.getKey(), entry.getValue());
            }
        }
        Product productBD = repository.findById(id).get();
        model.addAttribute("product", productBD);
        return "change_image";
    }

    public String changeImage(Long id, MultipartFile img, RedirectAttributes attributes) {
        boolean flagErrors = false;
        Product changeProduct = repository.findById(id).get();
        String pastUrl = uploadPath + "/" + changeProduct.getImageUrl();

        if(!img.getOriginalFilename().equals("")) {
            String uuid = UUID.randomUUID().toString();
            String nameOfFile = uuid + img.getOriginalFilename();
            String filePath = uploadPath + "/" + nameOfFile;

            changeProduct.setImageUrl(nameOfFile);
            try {
                img.transferTo(new File(filePath));
            } catch (IOException e) {
                e.printStackTrace();
            }

            repository.save(changeProduct);
            FileUtils.deleteQuietly(new File(pastUrl));
        } else {
            flagErrors = true;
            attributes.addFlashAttribute("imgErr", "Файл должен иметь название и не должен быть пустым");
        }
        if (flagErrors)
            return "redirect:/admin/changeImage/" + id;

        return "redirect:/admin";
    }

    public String changeUserActivity(Long id) {
        User user = userRepo.findById(id).get();
        user.setActive(!user.isActive());
        userRepo.save(user);
        return "redirect:/admin/userList";
    }

    public String addNewWorker(User user, Errors errors, RedirectAttributes redirectAttributes) {
        if(errors.hasErrors()){
            redirectAttributes.addFlashAttribute("currentName", user.getName());
            redirectAttributes.addFlashAttribute("currentPassword", user.getPassword());
            redirectAttributes.addFlashAttribute("currentEmail", user.getEmail());
            redirectAttributes.addFlashAttribute("currentRealName", user.getRealName());
            redirectAttributes.addFlashAttribute("currentSurname", user.getSurname());
            redirectAttributes.addFlashAttribute("currentPhone", user.getPhone());
            redirectAttributes.addFlashAttribute("currentAddress", user.getAddress());

            List<FieldError> list = errors.getFieldErrors();
            for (FieldError f : list) {
                redirectAttributes.addFlashAttribute(f.getField(), f.getDefaultMessage());
            }
        } else {
            user.setRoles(Collections.singleton(Role.WORKER));
            user.setActive(true);
            user.setPassword(encoder.encode(user.getPassword()));
            userRepo.save(user);
        }
        return "redirect:/admin";
    }
}
