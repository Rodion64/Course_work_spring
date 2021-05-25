package com.cw.ponomarev.back;

import com.cw.ponomarev.model.Order;
import com.cw.ponomarev.model.OrderStatus;
import com.cw.ponomarev.repos.OrderRepo;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.List;

@Service
public class WorkerService {
    private final OrderRepo orderRepo;
    private final MailService mailService;

    public WorkerService(OrderRepo orderRepo, MailService mailService) {
        this.orderRepo = orderRepo;
        this.mailService = mailService;
    }

    public String getPage(Model model) {
        List<Order> orderList = orderRepo.findAll();
        model.addAttribute("orders", orderList);

        OrderStatus[] statuses = OrderStatus.values();
        model.addAttribute("statuses", statuses);
        return "workerForm";
    }

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
