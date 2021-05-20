package com.cw.ponomarev.back;

import com.cw.ponomarev.dto.CartProduct;
import com.cw.ponomarev.model.Product;
import com.cw.ponomarev.repos.ProductRepo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

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

    public Integer getSize(String cart, HttpServletResponse response){
        if(cart != null && !cart.isBlank()) {
            boolean changes = false;

            List<String> listOfIDs = getListOfIDs(cart);
            Map<Long, Integer> mapIDs = new HashMap<>();

            for (String stringID : listOfIDs) {
                Long temp = Long.parseLong(stringID);
                if (!mapIDs.containsKey(temp)) {
                    if (productRepo.findById(temp).get() == null)
                        changes = true;
                    else
                        mapIDs.put(temp, 1);
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

    public String getPage(String cart, Model model) {
        if(cart == null || cart.isBlank()){
            model.addAttribute("emptyCart", "Корзина пуста. Добавьте товары и возвращайтесь)");
        } else{
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
            for(Map.Entry<Long, Long> entry : mapIDs.entrySet()){
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
            model.addAttribute("products", toUser);
        }

        return "cart";
    }


    public String changeNumberProduct(Long id, Long newNumber, String cookie, HttpServletResponse response) {
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
        return "redirect:/cart";
    }
}
