package com.finance.account.service.impl;

import com.finance.account.dao.AccountDao;
import com.finance.account.dao.impl.AccountDaoImpl;
import com.finance.account.service.AccountService;
import com.finance.core.entity.Account;

import java.util.UUID;

public class AccountServiceImpl implements AccountService {
    private final AccountDao accountDao = new AccountDaoImpl();

    @Override
    public Account createAccount(String holderName, String email, String phone) {
        String accNum = "ACC" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Account account = new Account(accNum, holderName, email, phone);
        return accountDao.save(account);
    }

    @Override
    public Account getByAccountNumber(String accountNumber) {
        return accountDao.findByAccountNumber(accountNumber);
    }
}
