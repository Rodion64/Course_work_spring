package com.cw.ponomarev.back;

import com.cw.ponomarev.model.Order;
import com.cw.ponomarev.model.OrderStatus;
import com.cw.ponomarev.repos.OrderRepo;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.List;

/**
 * Класс, который содержит логику работы рабочего.
 * @author Денис Пономарев
 */
@Service
public class WorkerService {
    /**
     * Репозиторий, который содержит все заказы.
     * @see Order
     */
    private final OrderRepo orderRepo;
    /**
     * @see MailService
     */
    private final MailService mailService;

    /**
     * Конструктор, который автоматически внедряет зависимости.
     * @param orderRepo - {@link #orderRepo}
     * @param mailService - {@link #mailService}
     */
    public WorkerService(OrderRepo orderRepo, MailService mailService) {
        this.orderRepo = orderRepo;
        this.mailService = mailService;
    }

    /**
     * Метод, который возвращает главную страницу с заказами и статусами.
     * @param model - MVC класс, в который добавляются атрибуты для отображения на странице (orders, statuses).
     * @return Страница с заказами и статусами.
     * @see Order
     * @see OrderStatus
     */
    public String getPage(Model model) {
        List<Order> orderList = orderRepo.findAll();
        model.addAttribute("orders", orderList);

        OrderStatus[] statuses = OrderStatus.values();
        model.addAttribute("statuses", statuses);
        return "workerForm";
    }

    /**
     * Метод, который меняет статус заказа и уведомляет об этом пользователя.
     * @param order - заказ, в котором хранится новый статус.
     * @param id - уникальный идентификатор заказа, статус которого надо изменить.
     * @return Главная страница с измененным заказом.
     * @see Order
     * @see #mailService
     */
    public String changeStatus(Order order, Long id) {
        Order orderBD = orderRepo.findById(id).get();
        orderBD.setStatus(order.getStatus());
        orderRepo.save(orderBD);

        String emailTo = orderBD.getEmail();
        String messageTo = "Статус вашего заказа(" + orderBD.getId() + ") был изменен на " +
                orderBD.getStatus() + ". Спасибо за ваше ожидание.";

        mailService.send(emailTo, "Обновление статуса заказа", messageTo);
        return "redirect:/worker";
    }
}
