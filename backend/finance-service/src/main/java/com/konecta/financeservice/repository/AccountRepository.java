package com.konecta.financeservice.repository;

import com.konecta.financeservice.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
//    Account findByAccountId(String );
}
