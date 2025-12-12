package com.finance.account.dao;

import com.finance.core.entity.Account;

public interface AccountDao {
    Account save(Account account);
    Account findByAccountNumber(String accountNumber);
    Account findById(Long id);
    void update(Account account);
}
