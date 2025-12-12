package com.finance.transaction.service;

import java.math.BigDecimal;
import java.util.List;
import com.finance.core.entity.Transaction;

public interface TransactionService {
    void deposit(String accountNumber, BigDecimal amount, String description);
    void withdraw(String accountNumber, BigDecimal amount, String description);
    List<Transaction> getHistory(String accountNumber);
}
