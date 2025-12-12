package com.finance.transaction.service;

import java.math.BigDecimal;
import java.util.List;
import com.finance.core.entity.Transaction;

public interface TransactionService {
    void deposit(String accountNumber, BigDecimal amount, String description);
    void withdraw(String accountNumber, BigDecimal amount, String description);
    List<Transaction> getHistory(String accountNumber);
    List<Transaction> getAllTransactions();
    List<Object[]> getTopExpenses(int k);
    List<Object[]> getTopCategories(int k);
    double getMonthlyAverage(int months);
    String analyzeBudget(double budget);
    boolean undoLastTransaction();
    List<String> detectFraud();
    List<String> getCategorySuggestions(String prefix);
}
