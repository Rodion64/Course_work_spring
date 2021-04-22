package com.cw.ponomarev.back;

import com.cw.ponomarev.model.User;
import com.cw.ponomarev.repos.UserRepo;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.lang.reflect.Array;
import java.util.ArrayList;


@Service
public class UserDetService implements UserDetailsService {
    private final UserRepo repo;


    public UserDetService(UserRepo repo) {
        this.repo = repo;
    }

    @Override
    public UserDetails loadUserByUsername(String name) throws UsernameNotFoundException {
        return repo.findUserByName(name);
    }
}
