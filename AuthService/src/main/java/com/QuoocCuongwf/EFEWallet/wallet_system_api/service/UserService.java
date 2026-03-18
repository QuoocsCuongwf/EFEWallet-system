package com.QuoocCuongwf.EFEWallet.wallet_system_api.service;

import com.QuoocCuongwf.EFEWallet.wallet_system_api.entity.User;
import com.QuoocCuongwf.EFEWallet.wallet_system_api.repository.UserReponsitory;
import com.QuoocCuongwf.EFEWallet.wallet_system_api.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserReponsitory userReponsitory;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userReponsitory.findUserByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException(email);
        }
        return new CustomUserDetails(user);
    }

}
