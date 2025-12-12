package com.finance.account.dao.impl;

import com.finance.account.dao.AccountDao;
import com.finance.core.entity.Account;
import com.finance.persistence.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class AccountDaoImpl implements AccountDao {

    @Override
    public Account save(Account account) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();
        session.save(account);
        tx.commit();
        session.close();
        return account;
    }

    @Override
    public Account findByAccountNumber(String accountNumber) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Account acc = session.createQuery("from Account a where a.accountNumber = :acc", Account.class)
                .setParameter("acc", accountNumber)
                .uniqueResult();
        session.close();
        return acc;
    }

    @Override
    public Account findById(Long id) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Account acc = session.get(Account.class, id);
        session.close();
        return acc;
    }

    @Override
    public void update(Account account) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();
        session.update(account);
        tx.commit();
        session.close();
    }
}
