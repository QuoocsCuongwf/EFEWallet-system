package com.QuoocCuongwf.EFEWallet.AuthService.service;

import com.QuoocCuongwf.EFEWallet.AuthService.entity.User;
import com.QuoocCuongwf.EFEWallet.AuthService.repository.UserReponsitory;
import com.QuoocCuongwf.EFEWallet.AuthService.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserReponsitory userReponsitory;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userReponsitory.findUserByEmail(email).orElseThrow(()->new RuntimeException());

        return new CustomUserDetails(user);
    }
    public UserDetails loadUserById(UUID id) throws UsernameNotFoundException  {
        User user = userReponsitory.findById(id).orElseThrow(()->new RuntimeException());
        return new CustomUserDetails(user);
    }
}
