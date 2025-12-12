package com.finance.transaction.service.impl;

import com.finance.account.dao.AccountDao;
import com.finance.account.dao.impl.AccountDaoImpl;
import com.finance.core.entity.Account;
import com.finance.core.entity.Transaction;
import com.finance.persistence.util.HibernateUtil;
import com.finance.transaction.dao.TransactionDao;
import com.finance.transaction.dao.impl.TransactionDaoImpl;
import com.finance.transaction.service.TransactionService;
import org.hibernate.Session;
import org.hibernate.LockMode;

import java.math.BigDecimal;
import java.util.List;

public class TransactionServiceImpl implements TransactionService {

    private final AccountDao accountDao = new AccountDaoImpl();
    private final TransactionDao transactionDao = new TransactionDaoImpl();

    @Override
    public void deposit(String accountNumber, BigDecimal amount, String description) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        Session session = HibernateUtil.getSessionFactory().openSession();
        org.hibernate.Transaction tx = session.beginTransaction();

        Account account = session.createQuery("from Account a where a.accountNumber = :acc", Account.class)
                .setParameter("acc", accountNumber)
                .setLockMode("a", LockMode.PESSIMISTIC_WRITE) // note: some hibernate versions accept setLockMode(int)
                .uniqueResult();

        if (account == null) {
            tx.rollback();
            session.close();
            throw new RuntimeException("Account not found");
        }

        account.setBalance(account.getBalance().add(amount));
        session.update(account);

        Transaction log = new Transaction(account, "DEPOSIT", amount, account.getBalance(), description);
        session.save(log);

        tx.commit();
        session.close();
    }

    @Override
    public void withdraw(String accountNumber, BigDecimal amount, String description) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        Session session = HibernateUtil.getSessionFactory().openSession();
        org.hibernate.Transaction tx = session.beginTransaction();

        Account account = session.createQuery("from Account a where a.accountNumber = :acc", Account.class)
                .setParameter("acc", accountNumber)
                .setLockMode("a", LockMode.PESSIMISTIC_WRITE)
                .uniqueResult();

        if (account == null) {
            tx.rollback();
            session.close();
            throw new RuntimeException("Account not found");
        }

        if (account.getBalance().compareTo(amount) < 0) {
            tx.rollback();
            session.close();
            throw new RuntimeException("Insufficient funds");
        }

        account.setBalance(account.getBalance().subtract(amount));
        session.update(account);

        Transaction log = new Transaction(account, "WITHDRAWAL", amount, account.getBalance(), description);
        session.save(log);

        tx.commit();
        session.close();
    }

    @Override
    public List<Transaction> getHistory(String accountNumber) {
        return transactionDao.findByAccountNumber(accountNumber);
    }
}
