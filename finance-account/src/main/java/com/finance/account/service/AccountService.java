package com.finance.account.service;

import com.finance.core.entity.Account;

public interface AccountService {
    Account createAccount(String holderName, String email, String phone);
    Account getByAccountNumber(String accountNumber);
}
