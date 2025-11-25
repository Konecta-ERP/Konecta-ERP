package com.konecta.financeservice.repository;

import com.konecta.financeservice.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {
    /**
     * Retrieves the primary keys (accountPK) of all accounts that are currently ACTIVE.
     * This is used to populate the GL report when no specific accounts are filtered.
     *
     * @return List of Long representing all active account primary keys.
     */
    @Query("SELECT a.accountPK FROM Account a WHERE a.status = com.konecta.financeservice.model.enums.AccountStatus.ACTIVE")
    List<Long> findAllActiveAccountPKs();
}
