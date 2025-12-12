package com.finance.app;

import com.finance.account.service.AccountService;
import com.finance.account.service.impl.AccountServiceImpl;
import com.finance.core.entity.Account;
import com.finance.core.entity.Transaction;
import com.finance.transaction.service.TransactionService;
import com.finance.transaction.service.impl.TransactionServiceImpl;
import com.finance.persistence.util.HibernateUtil;

import java.math.BigDecimal;
import java.util.List;

public class App {
    public static void main(String[] args) {
        AccountService accountService = new AccountServiceImpl();
        TransactionService txService = new TransactionServiceImpl();

        // create account
        Account acct = accountService.createAccount("Anshu Yadav", "ashuy@example.com", "9876543210");
        System.out.println("Created account number: " + acct.getAccountNumber());

        // deposit
        txService.deposit(acct.getAccountNumber(), new BigDecimal("1500.00"), "Initial deposit");
        System.out.println("Deposited 1500.00");

        // withdraw
        try {
            txService.withdraw(acct.getAccountNumber(), new BigDecimal("300.00"), "ATM withdrawal");
            System.out.println("Withdrew 300.00");
        } catch (RuntimeException e) {
            System.err.println("Withdraw failed: " + e.getMessage());
        }

        // print balance
        Account updated = accountService.getByAccountNumber(acct.getAccountNumber());
        System.out.println("Available balance: " + updated.getBalance());

        // print history
        List<Transaction> history = txService.getHistory(acct.getAccountNumber());
        System.out.println("Transaction history:");
        for (Transaction t : history) {
            System.out.printf("%s | %s | %s | balance after: %s%n",
                    t.getCreatedAt(), t.getType(), t.getAmount(), t.getBalanceAfter());
        }

        HibernateUtil.shutdown();
    }
}
