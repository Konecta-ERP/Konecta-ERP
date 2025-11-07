package com.konecta.financeservice.controller;

import com.konecta.financeservice.dto.AccountDTO;
import com.konecta.financeservice.dto.CreateAccountDTO;
import com.konecta.financeservice.dto.UpdateAccountDTO;
import com.konecta.financeservice.dto.response.ApiResponse;
import com.konecta.financeservice.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    @Autowired
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AccountDTO>> createAccount(@RequestBody CreateAccountDTO dto) {
        AccountDTO account = accountService.createAccount(dto);
        ApiResponse<AccountDTO> response = ApiResponse.success(
                account,
                HttpStatus.CREATED.value(),
                "Account created successfully",
                "Account created successfully with PK " + account.getAccountPK()
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AccountDTO>>> getAllAccounts() {
        List<AccountDTO> accounts = accountService.getAllAccounts();
        ApiResponse<List<AccountDTO>> response = ApiResponse.success(
                accounts,
                HttpStatus.OK.value(),
                "All accounts retrieved",
                accounts.size() + " account retrieved"
        );
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountDTO>> updateAccount(@PathVariable("id") Long id, @RequestBody UpdateAccountDTO dto) {
        AccountDTO updatedAccount = accountService.updateAccount(id, dto);
        ApiResponse<AccountDTO> response = ApiResponse.success(
                updatedAccount,
                HttpStatus.OK.value(),
                "Account updated successfully",
                "Account with id " + id + " updated successfully"
        );
        return ResponseEntity.ok(response);
    }

    @PutMapping("/deactivate/{id}")
    public ResponseEntity<ApiResponse<AccountDTO>> deactivateAccount(@PathVariable("id") Long id) {
        AccountDTO updatedAccount = accountService.deactivateAccount(id);
        ApiResponse<AccountDTO> response = ApiResponse.success(
                updatedAccount,
                HttpStatus.OK.value(),
                "Account deactivated successfully",
                "Account with id " + id + " deactivated successfully"
        );
        return ResponseEntity.ok(response);
    }


}
