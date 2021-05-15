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
            redirectAttributes.addFlashAttribute("selectingTypeErr", "Необходимо выбрать тип товара");
        }
        if (!flagOfErrors){
            dataBaseProduct.setForChange(product.getType(), product.getTitle(), product.getDescription(), product.getNumber(), product.getPrice());
            repository.save(dataBaseProduct);
            return "redirect:/admin";
        }

        return "redirect:/admin/changeProduct/" + id;
    }

    public String changeImageForm(Long id, Model model){
        Product productBD = repository.findById(id).get();
        model.addAttribute("product", productBD);
        return "change_image";
    }

    public String changeImage(Long id, MultipartFile img, RedirectAttributes attributes) {
        Product changeProduct = repository.findById(id).get();

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
        }

        attributes.addFlashAttribute("imgErr", "Ошибка в прочтении файла, попробуйте снова");
        return "redirect:/admin/changeImage/" + id;
    }
}
