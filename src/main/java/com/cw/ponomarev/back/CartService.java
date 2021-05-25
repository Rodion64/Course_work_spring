package com.cw.ponomarev.back;

import com.cw.ponomarev.dto.CartProduct;
import com.cw.ponomarev.model.Product;
import com.cw.ponomarev.repos.ProductRepo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.PostConstruct;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@Service
public class CartService {
    @Value("${cookies.name}")
    private String cartName;
    private final ProductRepo productRepo;  //change this

    public CartService(ProductRepo productRepo) {
        this.productRepo = productRepo;
    }

    public String add(String cart, Long id, HttpServletResponse response){
        Cookie cookie = new Cookie(cartName,
                (cart != null ? cart : "") + id + "_");
        cookie.setPath("/");
        cookie.setMaxAge(2147483647);
        response.addCookie(cookie);
        return "redirect:/";
    }

    private List<String> getListOfIDs(String cookie){
        return Arrays.asList(cookie.split("_"));
    }

    public Map<String, List<Product>> getProductsMap(String cookie){
        List<String> listOfIDs = new ArrayList<>(getListOfIDs(cookie));

        Map<Long, Integer> mapIDs = new HashMap<>();
        for (int i = 0; i < listOfIDs.size(); i++) {
            Long temp = Long.parseLong(listOfIDs.get(i));
            if (!mapIDs.containsKey(temp)) {
                mapIDs.put(temp, 1);
            } else {
                mapIDs.put(temp, mapIDs.get(temp) + 1);
            }
        }
        List<Product> errorProducts = new ArrayList<>();
        List<Product> resultProducts = new ArrayList<>();

        Map<String, List<Product>> resultMap = new LinkedHashMap<>();
        for (Map.Entry<Long, Integer> entry : mapIDs.entrySet()) {
            Product product = productRepo.findById(entry.getKey()).get();

            if(product != null) {
                if (product.getNumber() < entry.getValue()) {
                    errorProducts.add(product);
                } else {
                    for (int i = 0; i < entry.getValue(); i++) {
                        resultProducts.add(product);
                    }
                }
            }
        }
        resultMap.put("res", resultProducts);
        resultMap.put("err", errorProducts);
        return resultMap;
    }

    public Integer getSize(String cart, HttpServletResponse response){
        if(cart != null && !cart.isBlank()) {
            boolean changes = false;

            List<String> listOfIDs = getListOfIDs(cart);
            Map<Long, Integer> mapIDs = new HashMap<>();

            for (String stringID : listOfIDs) {
                Long temp = Long.parseLong(stringID);
                if (!mapIDs.containsKey(temp)) {
                    try {
                        if (productRepo.findById(temp) == null)
                            changes = true;
                        else
                            mapIDs.put(temp, 1);
                    } catch (Exception e){
                        changes = true;
                    }

                } else {
                    if (productRepo.findById(temp).get().getNumber() > mapIDs.get(temp))
                        mapIDs.put(temp, mapIDs.get(temp) + 1);
                    else
                        changes = true;
                }
            }
            if(changes){
                StringBuilder stringBuilder = new StringBuilder();
                int count = 0;
                for(Map.Entry<Long, Integer> entry : mapIDs.entrySet()){
                    for(int i = 0; i < entry.getValue(); i++){
                        stringBuilder.append(entry.getKey()).append("_");
                        count++;
                    }
                }
                Cookie cookie = new Cookie(cartName, stringBuilder.toString());
                cookie.setPath("/");
                cookie.setMaxAge(2147483647);
                response.addCookie(cookie);
                return count;
            }
            return listOfIDs.size();
        }
        return 0;
    }

    public String getPage(String cart, Model model, HttpServletResponse response) {
        if(cart == null || cart.isBlank()){
            model.addAttribute("visibility", false);
            model.addAttribute("emptyCart", "Корзина пуста. Добавьте товары и возвращайтесь)");
        } else{
            model.addAttribute("visibility", true);
            List<String> listOfIDs = getListOfIDs(cart);
            Map<Long, Long> mapIDs = new HashMap<>();
            boolean changes = false;

            for (String stringID : listOfIDs) {
                Long temp = Long.parseLong(stringID);
                if (!mapIDs.containsKey(temp)) {
                    mapIDs.put(temp, 1L);
                } else {
                    mapIDs.put(temp, mapIDs.get(temp) + 1);
                }
            }

            List<CartProduct> toUser = new ArrayList<>();
            boolean changeCookieFlag = false;
            for(Map.Entry<Long, Long> entry : mapIDs.entrySet()){
                Optional<Product> optionalBD = productRepo.findById(entry.getKey());
                if(!optionalBD.isPresent()){
                    changeCookieFlag = true;
                    changing(entry.getKey(), 0L, cart, response);
                    continue;
                }
                Product productBD = productRepo.findById(entry.getKey()).get();
                Long price = 0L;

                CartProduct tempProduct = new CartProduct();
                tempProduct.setId(entry.getKey());
                tempProduct.setType(productBD.getType());
                tempProduct.setTitle(productBD.getTitle());
                tempProduct.setDescription(productBD.getDescription());
                tempProduct.setImageUrl(productBD.getImageUrl());
                tempProduct.setNumberInBD(productBD.getNumber());
                tempProduct.setNumberInCart(entry.getValue());

                for(int i = 0; i < entry.getValue(); i++){
                    price += productBD.getPrice();
                }
                tempProduct.setPrice(price);
                toUser.add(tempProduct);
            }
            if(changeCookieFlag)
                model.addAttribute("emptyProdErr",
                        "Некоторые категории товаров были удалены из вашей корзины, " +
                                "поскольку данные товары не содержатся больше в магазине." +
                                " Приносим свои извинения)");
            model.addAttribute("products", toUser);
        }

        return "cart";
    }


    public String changeNumberProduct(Long id, Long newNumber, String cookie, HttpServletResponse response) {
        changing(id, newNumber, cookie, response);
        return "redirect:/cart";
    }

    private void changing(Long id, Long newNumber, String cookie, HttpServletResponse response){
        List<String> stringIDs = getListOfIDs(cookie);
        String strID = Long.toString(id);
        StringBuilder stringBuilder = new StringBuilder();
        for(String str: stringIDs) {
            if(!str.equals(strID))
                stringBuilder.append(str).append("_");
        }
        for(int i = 0; i < newNumber; i++)
            stringBuilder.append(id).append("_");

        Cookie newCookie = new Cookie(cartName, stringBuilder.toString());
        newCookie.setPath("/");
        newCookie.setMaxAge(2147483647);
        response.addCookie(newCookie);
    }

    public void clear(HttpServletResponse response){
        Cookie cookie = new Cookie(cartName, "");
        cookie.setPath("/");
        cookie.setMaxAge(2147483647);
        response.addCookie(cookie);
    }
}
