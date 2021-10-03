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

/**
 * Класс, реализующий логику работы пользователя с ролью admin.
 * Краткий перечень функций админа: добавление новой позиции товара, получение списка всех пользователей,
 * сортировка товаров по типу, удаление товара из БД по его id, изменение продукта, функции бана / разбана
 * пользователей, добавление новых рабочих в бд.
 * @author Денис Пономарев
 */
@Service
public class AdminService {
    @Value("${upload.path}")
    String uploadPath;

    /**
     * Репозиторий продуктов.
     * @see Product
     */
    private final ProductRepo repository;
    /**
     * Репозиторий пользователей.
     * @see User
     */
    private final UserRepo userRepo;
    /**
     * Криптографический класс для шифровки / расшифровки паролей пользователей.
     */
    private final BCryptPasswordEncoder encoder;

    /**
     * Конструктор, который автоматически внедряет зависимости класса.
     * @param repository - {@link #repository}
     * @param userRepo - {@link #userRepo}
     * @param encoder - {@link #encoder}
     */
    public AdminService(ProductRepo repository, UserRepo userRepo, BCryptPasswordEncoder encoder) {
        this.repository = repository;
        this.userRepo = userRepo;
        this.encoder = encoder;
    }

    /**
     * Метод добавления новых позиций товаров в базу данных.
     * @param product - продукт, который необходимо добавить в базу данных.
     * @param file - изображение формата png или jpg, которое должно быть прикрепленно к товару.
     * @param errors - в случае ошибки создания товара на вход приходят ошибки, которые возвращаются пользователю в виде предупреждений.
     * @param redirectAttributes - атрибуты, которые заполняются и в дальнейшем выдаются пользователю на странице.
     * @see Product
     * @return Главная страница администратора с новыми атрбутами (добавленная позиция товара)
     */
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

    /**
     * Метод, который добавляет на страницу пользователей список пользователей.
     * @param model - MVC класс, в который добавляются атрибуты для отображения на странице (атрибут "users" - список пользователей).
     * @return Страница, содержащая всех пользователей приложения.
     */
    public String userList(Model model){
        List<User> users = userRepo.findAll();
        model.addAttribute("users", users);

        Role [] roles = Role.values();
        model.addAttribute("userRoles", roles);
        return "userList";
    }

    /**
     * Метод, который фильтрует продукты по их типу.
     * @param model - MVC класс, в который добавляются атрибуты для отображения на странице.
     * @param type - тип товара с фронта.
     * @param redirectAttributes - атрибуты, которые заполняются и в дальнейшем выдаются пользователю на странице.
     * @see ProductType
     * @return Строка, которая перенаправляет заполненные redirectAttributes на страницу для отображения.
     */
    public String getCheckedListForm(Model model, ProductType type, RedirectAttributes redirectAttributes) {
        List<Product> neededProducts = repository.findAllByType(type);
        redirectAttributes.addFlashAttribute("list", neededProducts);
        return "redirect:/admin";
    }

    /**
     * Метод удаления товара из базы данных по его id, а также удаление изображения товара.
     * @param id - уникальный идентификатор товара, по которому происходит удаление.
     * @see Product
     * @return Строка, которая перенаправляет пользователя на главную страницу.
     */
    public String deleteProd(Long id){
        String imageUrl = uploadPath + "/" + repository.findById(id).get().getImageUrl();
        repository.deleteById(id);

        FileUtils.deleteQuietly(new File(imageUrl));
        return "redirect:/admin";
    }

    /**
     * Метод, заполняющий страницу изменения конкретного товара.
     * @param id - уникальный идентификатор товара.
     * @param model - MVC класс, в который добавляются атрибуты для отображения на странице
     *              (currentProduct - продукт, который передается по id, types - возможные типы продукта).
     * @param attributes - атрибуты с фронта.
     * @return Страница, содержащая информацию по изменяющемуся товару.
     * @see Product
     * @see ProductType
     */
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

    /**
     * Метод, реализующий проверку полей продукта, который будет изменен.
     * Товар не будет изменен, если его название или описание, или тип, или количество, или цена пустые.
     * @param product - продукт, приходящий с фронта, который необходимо проверить.
     * @param redirectAttributes - атрибуты, которые заполняются и в дальнейшем выдаются пользователю на странице.
     *                           (titleErr, descriptionErr, selectingTypeErr, numberErr, priceErr).
     * @param id - уникальный идентификатор товара, который нужно изменить в базе.
     * @return Два варианта исхода: если в пришедшем продукте содержатся ошибки в полях, в redirectAttributes добавляются
     * соответствующие атрибуты и возвращается та же страница, в противном случае - товар в БД обновляется и возвращается страница
     * админа.
     * @see Product
     */
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

    /**
     * Метод, возвращающий страницу для изменения изображения товара.
     * @param id - уникальный идентификатор товара, изображение которого надо изменить.
     * @param model - model - MVC класс, в который добавляются атрибуты для отображения на странице (product - продукт, который был найден в бд по id).
     * @param map - параметры, которые приходят с фронта.
     * @return Страница для изменения изображения товара.
     * @see Product
     */
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

    /**
     * Метод непосредственного изменения изображения товара.
     * @param id - уникальный идентификатор товара, изображение которого надо изменить.
     * @param img - новое изображение товара.
     * @param attributes - атрибуты, которые заполняются и в дальнейшем выдаются пользователю на странице (imgErr).
     * @return Два возможных исхода: если новое изображение не содержит названия, то в attributes добавляется атрибут,
     * который оповещает пользователя об ошибке, и возвращается страница изменения товара; в противном случае - продукт обновляется,
     * новый файл заменяет старый и возвращается главная страница админа.
     * @see Product
     */
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

    /**
     * Метод, реализующий логику бана / разбана пользователя (меняется activity пользователя в БД).
     * @param id - уникальный идентификатор пользователя, которого необходимо забанить / розбанить.
     * @return Страница со всеми пользователями системы (будет обновлен статус пользователя, которого забанили / разбанили).
     * @see User
     */
    public String changeUserActivity(Long id) {
        User user = userRepo.findById(id).get();
        user.setActive(!user.isActive());
        userRepo.save(user);
        return "redirect:/admin/userList";
    }

    /**
     * Метод добавления нового рабочего в базу данных.
     * @param user - пользователь, которого необходимо добавить в БД.
     * @param errors - - в случае ошибки создания пользователя на вход приходят ошибки, которые возвращаются пользователю в виде предупреждений.
     * @param redirectAttributes - атрибуты, которые заполняются и в дальнейшем выдаются пользователю на странице (currentName, currentPassword,
     *                           currentEmail, currentRealName, currentSurname, currentPhone, currentAddress).
     * @return Два исхода: если пришедший пользователь имя поля с ошибками, то в redirectAttributes записываются ошибки и пользователю
     * возвращается страница, уведомляющая его об ошибках; в противном случае - новые работник добавляется в БД и возвращается главная страница.
     * @see User
     * @see BCryptPasswordEncoder
     */
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
