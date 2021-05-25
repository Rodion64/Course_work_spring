package com.cw.ponomarev.controller;

import com.cw.ponomarev.back.WorkerService;
import com.cw.ponomarev.model.Order;
import com.cw.ponomarev.model.OrderStatus;
import com.cw.ponomarev.model.Product;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/worker")
public class WorkerController {
    private final WorkerService service;

    public WorkerController(WorkerService service) {
        this.service = service;
    }

    @GetMapping
    public String getPage(Model model){
        return service.getPage(model);
    }

    @PostMapping("/changeStatus/{id}")
    public String changeStatus(@ModelAttribute Order order,
                               @PathVariable(name = "id") Long id){
        return service.changeStatus(order, id);
    }
}
