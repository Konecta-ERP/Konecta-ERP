package com.konecta.financeservice.service;

import com.konecta.financeservice.dto.AccountDTO;
import com.konecta.financeservice.dto.CreateAccountDTO;
import com.konecta.financeservice.dto.UpdateAccountDTO;
import com.konecta.financeservice.entity.Account;
import com.konecta.financeservice.model.enums.AccountStatus;
import com.konecta.financeservice.repository.AccountRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    @Autowired
    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public AccountDTO createAccount(CreateAccountDTO dto) {
        Account account = new Account();
        account.setAccountId(dto.getAccountId());
        account.setAccountName(dto.getAccountName());
        account.setAccountType(dto.getAccountType());
        if (dto.getPlMapping() != null) {
            account.setPlMapping(dto.getPlMapping());
        }
        account.setDescription(dto.getDescription());
        Account savedAccount = accountRepository.save(account);
        return convertToDTO(savedAccount);
    }

    public List<AccountDTO> getAllAccounts() {
        return accountRepository.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Transactional
    public AccountDTO updateAccount(Long id, UpdateAccountDTO dto) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Account not found with id " + id));

        if (account.isHasTransactions()) {
            if (dto.getAccountId() != null || dto.getAccountType() != null) {
                throw new IllegalArgumentException("Cannot update the id or type of an account that already has transactions");
            }
        } else {
            if (dto.getAccountId() != null) account.setAccountId(dto.getAccountId());
            if (dto.getAccountType() != null) account.setAccountType(dto.getAccountType());
        }

        if (dto.getAccountName() != null) account.setAccountName(dto.getAccountName());
        if (dto.getPlMapping() != null) account.setPlMapping(dto.getPlMapping());
        if (dto.getDescription() != null) account.setDescription(dto.getDescription());

        Account updatedAccount = accountRepository.save(account);
        return convertToDTO(updatedAccount);
    }

    @Transactional
    public AccountDTO deactivateAccount(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Account not found with id " + id));
        if (account.getStatus() == AccountStatus.INACTIVE) {
            throw new IllegalStateException("Account with id " + id + " is already inactive");
        }
        account.setStatus(AccountStatus.INACTIVE);
        Account updatedAccount = accountRepository.save(account);
        return convertToDTO(updatedAccount);
    }

    private AccountDTO convertToDTO(Account account) {
        AccountDTO dto = new AccountDTO();
        dto.setAccountPK(account.getAccountPK());
        dto.setAccountId(account.getAccountId());
        dto.setAccountName(account.getAccountName());
        dto.setAccountType(account.getAccountType());
        dto.setPlMapping(account.getPlMapping());
        dto.setDescription(account.getDescription());
        dto.setStatus(account.getStatus());
        dto.setHasTransactions(account.isHasTransactions());
        dto.setCreatedAt(account.getCreatedAt());
        dto.setUpdatedAt(account.getUpdatedAt());
        return dto;
    }

}
