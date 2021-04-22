package com.cw.ponomarev.repos;

import com.cw.ponomarev.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepo extends JpaRepository<User, Long> {
    User findUserByName(String name);

}
