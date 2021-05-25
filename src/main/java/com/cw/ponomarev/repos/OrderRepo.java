package com.cw.ponomarev.repos;

import com.cw.ponomarev.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepo extends JpaRepository<Order, Long> {
}
