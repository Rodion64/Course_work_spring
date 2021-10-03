package com.cw.ponomarev.back;

import com.cw.ponomarev.model.Order;
import com.cw.ponomarev.model.OrderStatus;
import com.cw.ponomarev.model.Product;
import com.cw.ponomarev.model.User;
import com.cw.ponomarev.repos.OrderRepo;
import com.cw.ponomarev.repos.ProductRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Класс, который содержит логику работы оформления заказа.
 * @author Денис Пономарев
 */
@Service
@RequiredArgsConstructor
public class OrderService {
    /**
     * @see CartService
     */
    private final CartService cartService;
    /**
     * Репозиторий, который содержит все записи заказов.
     * @see Order
     * @see OrderStatus
     */
    private final OrderRepo orderRepo;
    /**
     * Репозиторий, который содержит все записи продуктов.
     * @see Product
     */
    private final ProductRepo productRepo;
    /**
     * @see MailService
     */
    private final MailService mailService;

    /**
     * Название поля, которое хранится в cookie.
     */
    @Value("${cookie.orders.name}")
    private String orderName;

    /**
     * Метод оформления заказа.
     * @param user - пользователь, который оформляет заказ.
     * @param order - заказ.
     * @param errors - ошибки, которые приходят с фронта в случае, если что-то не так с заказом.
     * @param cart - строка cookie, которая хранит id товаров в корзине, разделенных '_'.
     * @param orderCookie - строка, которая хранит товары заказа.
     * @param model - MVC класс, в который добавляются атрибуты для отображения на странице
     * @param response - ответ, который возвращается пользователю (строка в cookie).
     * @param attributes - атрибуты, которые заполняются и в дальнейшем выдаются пользователю на странице.
     * @return Несколько вариантов: если в БД имеется надлежащее кол-во товаров, то заказ дополняется полями пользователя и
     * статуса, заказ сохраняется в БД и пользователю на mail отправляется уведомление, корзина очищается. Сервис возвращает главную страницу.
     * Другой вариант - в БД не хватает товаров, поэтому в attributes добавляется атрибут 'numberErr' и возвращается страница оформления заказа.
     * @see User
     * @see Order
     * @see CartService
     */
    public String placeOrder(User user, Order order, Errors errors, String cart, String orderCookie, Model model, HttpServletResponse response, RedirectAttributes attributes) {
        if(errors.hasErrors()){
            List<FieldError> list = errors.getFieldErrors();
            for (FieldError f : list) {
                attributes.addFlashAttribute(f.getField(), f.getDefaultMessage());
            }
            return "redirect:/order";
        }

        Map<String, List<Product>> resultMap = cartService.getProductsMap(cart);
        if (!resultMap.get("err").isEmpty()){
            attributes.addFlashAttribute("numberErr", "Количество данных товаров на складе изменилось. " +
                    "Измените количество позиций или замените их на альтернативные.");
            attributes.addFlashAttribute("list", new HashSet<>(resultMap.get("err")));
            return "redirect:/order";
        }

        order.setUser(user);
//        order.setProducts(resultMap.get("res"));
        order.setStatus(Collections.singleton(OrderStatus.PROCESSING));
        orderRepo.save(order);

        for(Product product : resultMap.get("res")){
            product.setOrder(order);
            productRepo.save(product);
        }

        sendMessage(user, order);

        Map<Integer, Product> changeProductMap = new HashMap<>();

        for (Product product: new HashSet<>(resultMap.get("res"))){
            changeProductMap.put(Collections.frequency(resultMap.get("res"), product), product);
        }

        for(Map.Entry<Integer, Product> entry: changeProductMap.entrySet()){
            Product productBD = productRepo.findById(entry.getValue().getId()).get();
            productBD.setNumber(productBD.getNumber() - entry.getKey());
            productRepo.save(productBD);
        }

        cartService.clear(response);
        Cookie cookie = new Cookie(orderName,
                (orderCookie != null ? orderCookie : "") + order.getId() + "_");
        cookie.setPath("/");
        cookie.setMaxAge(2147483647);
        response.addCookie(cookie);

        return "redirect:/";
    }

    private void sendMessage(User user, Order order){
        List<Product> products = orderRepo.findById(order.getId()).get().getProducts();
        String message = String.format("Спасибо, что выбрали именно нас!\n" +
                "Номер вашего заказа : " + order.getId() + '.' +
                "\nСумма вашего заказа : " + getFullyOrderCost(products) + '.' +
                "\nТовары : " + getTitlesForMail(products) +
                "\nСкоро с вами свяжется наш оператор, ожидайте звонка.");
        String email = "";
        if(user != null){
            if(user.getEmail() != null || !user.getEmail().isEmpty())
                email = user.getEmail();
        } else {
            email = order.getEmail();
        }
        mailService.send(email, "Новый заказ", message);
    }

    private String getTitlesForMail(List<Product> products){
        String resultStr = "";

        for (Product product : products){
            resultStr += product.getTitle() + "\n";
        }
        return resultStr;
    }


    private Long getFullyOrderCost(List<Product> products){
        Long sum = products.stream().mapToLong(Product::getPrice).sum();
        return sum;
    }

    /**
     * Метод, который возвращает страницу с заказами. Логика зависит от того, авторизован пользователь или нет (для авторизованного
     * пользователя заказы берутся из БД, для неавторизованного - из cookie-файла).
     * @param user - пользователь, заказы которого необходимо посмотреть.
     * @param cookie - строка, которая содержит заказы неавторизованного пользователя.
     * @param model - MVC класс, в который добавляются атрибуты для отображения на странице (orders).
     * @return Страница со всеми заказами.
     * @see User
     * @see Order
     */
    public String getOrderList(User user, String cookie, Model model) {
        List<Order> orders = new ArrayList<>();
        if (user != null){
            orders = user.getOrders();
        } else {
            if(cookie != null || !cookie.isEmpty()){
                List<String> orderIDs = getOrderIDs(cookie);
                for(String id: orderIDs){
                    if(!id.isEmpty() || !id.isBlank()){
                        orders.add(orderRepo.findById(Long.parseLong(id)).get());
                    }
                }
            }
        }
        model.addAttribute("orders", orders);
        return "userOrderList";
    }

    private List<String> getOrderIDs(String cookie){
        return Arrays.asList(cookie.split("_"));
    }

    /**
     * Метод, который удаляет заказ по его уникальному идентификатору, логика делится в зависимости от того,
     * авторизован пользователь (удаляется из БД) или нет (удаляется из cookie).
     * @param id - уникальный идентификатор заказа, который надо удалить.
     * @param cookie - строка, которая содержит заказы неавторизованного пользователя.
     * @param user - пользователь системы
     * @param response - ответ, который возвращается пользователю (строка в cookie).
     * @return Страница, содержащая заказы пользователя системы.
     */
    public String deleteOrder(Long id, String cookie, User user, HttpServletResponse response) {
        if (user != null){
            orderRepo.deleteById(id);
        } else {
            List<String> fixedOrderIDs = getOrderIDs(cookie);
            List<String> orderIDs = new ArrayList<>(fixedOrderIDs);

//            orderIDs.remove(String.valueOf(id));
            for(String stringID : fixedOrderIDs){
                if(stringID.equals(Long.toString(id))){
                    orderIDs.remove(stringID);
                }
            }

            String newCookieStr = "";

            for (String stringId: orderIDs){
                newCookieStr += stringId + '_';
            }
            Cookie newCookie = new Cookie(orderName,
                    (newCookieStr));
            newCookie.setPath("/");
            newCookie.setMaxAge(2147483647);
            response.addCookie(newCookie);
        }

        return "redirect:/order/list";
    }
}
