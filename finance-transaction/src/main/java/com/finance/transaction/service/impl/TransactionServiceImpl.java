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

    @SuppressWarnings("unused")
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

    @Override
    public List<Transaction> getAllTransactions() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<Transaction> transactions = session.createQuery("from Transaction", Transaction.class).list();
        session.close();
        return transactions;
    }

    @Override
    public List<Object[]> getTopExpenses(int k) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<Object[]> results = session.createQuery(
            "select t.amount, t.description from Transaction t where t.type = 'WITHDRAWAL' order by t.amount desc", Object[].class)
            .setMaxResults(k)
            .list();
        session.close();
        return results;
    }

    @Override
    public List<Object[]> getTopCategories(int k) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<Object[]> results = session.createQuery(
            "select t.description, sum(t.amount) from Transaction t where t.type = 'WITHDRAWAL' group by t.description order by sum(t.amount) desc", Object[].class)
            .setMaxResults(k)
            .list();
        session.close();
        return results;
    }

    @Override
    public double getMonthlyAverage(int months) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<Object[]> results = session.createQuery(
            "select month(t.createdAt), year(t.createdAt), sum(t.amount) from Transaction t where t.type = 'WITHDRAWAL' group by year(t.createdAt), month(t.createdAt) order by year(t.createdAt) desc, month(t.createdAt) desc", Object[].class)
            .setMaxResults(months)
            .list();
        session.close();
        if (results.isEmpty()) return 0.0;
        double total = 0.0;
        for (Object[] row : results) {
            total += ((Number) row[2]).doubleValue();
        }
        return total / results.size();
    }

    @Override
    public String analyzeBudget(double budget) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Double totalSpending = (Double) session.createQuery(
            "select sum(t.amount) from Transaction t where t.type = 'WITHDRAWAL'")
            .uniqueResult();
        session.close();
        if (totalSpending == null) totalSpending = 0.0;
        if (totalSpending > budget) {
            return budget + "|" + totalSpending + "|OVER|" + (totalSpending - budget);
        } else {
            return budget + "|" + totalSpending + "|UNDER|" + (budget - totalSpending);
        }
    }

    @Override
    public boolean undoLastTransaction() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        org.hibernate.Transaction tx = session.beginTransaction();
        Transaction last = (Transaction) session.createQuery("from Transaction order by id desc")
            .setMaxResults(1)
            .uniqueResult();
        if (last == null) {
            tx.rollback();
            session.close();
            return false;
        }
        Account account = last.getAccount();
        if ("DEPOSIT".equals(last.getType())) {
            account.setBalance(account.getBalance().subtract(last.getAmount()));
        } else {
            account.setBalance(account.getBalance().add(last.getAmount()));
        }
        session.update(account);
        session.delete(last);
        tx.commit();
        session.close();
        return true;
    }

    @Override
    public List<String> detectFraud() {
        // Simple implementation: check for duplicate amounts in short time
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<Object[]> results = session.createQuery(
            "select t.amount, count(t) from Transaction t group by t.amount having count(t) > 1", Object[].class)
            .list();
        session.close();
        List<String> frauds = new java.util.ArrayList<>();
        for (Object[] row : results) {
            frauds.add("DUPLICATE|" + row[0] + "|" + row[1]);
        }
        return frauds;
    }

    @Override
    public List<String> getCategorySuggestions(String prefix) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<String> suggestions = session.createQuery(
            "select distinct t.description from Transaction t where t.description like :prefix", String.class)
            .setParameter("prefix", prefix + "%")
            .list();
        session.close();
        return suggestions;
    }
}
