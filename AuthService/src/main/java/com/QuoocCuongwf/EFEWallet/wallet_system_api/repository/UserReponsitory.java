package com.QuoocCuongwf.EFEWallet.wallet_system_api.repository;

import com.QuoocCuongwf.EFEWallet.wallet_system_api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserReponsitory extends JpaRepository<User,Long> {
     User findUserByEmail(String email);
}
