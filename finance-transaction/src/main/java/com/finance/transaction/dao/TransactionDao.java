package com.finance.transaction.dao;

import com.finance.core.entity.Transaction;

import java.util.List;

public interface TransactionDao {
    Transaction save(Transaction tx);
    List<Transaction> findByAccountNumber(String accountNumber);
}
