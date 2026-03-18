package com.QuoocCuongwf.EFEWallet.AuthService.reponsitory;

import com.QuoocCuongwf.EFEWallet.AuthService.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserReponsitory extends JpaRepository<User,Long> {
     User findUserByEmail(String email);
}
