package com.example.account.service;

import com.example.account.domain.model.Account;
import com.example.account.domain.repository.AccountRepository;
import com.example.account.dto.ResponseDTO;
import com.example.account.dto.UserDTO;
import com.example.account.utils.AccountNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountNumber accountNumber;

    public AccountService(AccountRepository accountRepository, AccountNumber accountNumber) {
        this.accountRepository = accountRepository;
        this.accountNumber = accountNumber;
    }

    public ResponseDTO createAccount(UserDTO userDTO) {
        log.info("Starting create account {}", userDTO);

        Account account = factoryAccount(userDTO);

        accountRepository.save(account);

        log.info("Account created successfully {}", account);

        return new ResponseDTO("Account created", List.of(account));
    }

    private Account factoryAccount(UserDTO userDTO) {
        Account account = new Account();
        account.setUserReference(userDTO.getIdReference());
        account.setNumberAccount(accountNumber.generate(userDTO.getIdReference()));
        account.setName(userDTO.getName());
        return account;
    }

}
