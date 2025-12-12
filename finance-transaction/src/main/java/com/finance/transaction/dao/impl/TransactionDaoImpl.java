package com.finance.transaction.dao.impl;

import com.finance.core.entity.Transaction;
import com.finance.persistence.util.HibernateUtil;
import com.finance.transaction.dao.TransactionDao;
import org.hibernate.Session;

import java.util.List;

public class TransactionDaoImpl implements TransactionDao {

    @Override
    public Transaction save(Transaction tx) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        org.hibernate.Transaction hbTx = session.beginTransaction();
        session.save(tx);
        hbTx.commit();
        session.close();
        return tx;
    }

    @Override
    public List<Transaction> findByAccountNumber(String accountNumber) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<Transaction> list = session.createQuery(
                "select t from Transaction t join t.account a where a.accountNumber = :acc order by t.createdAt desc",
                Transaction.class)
                .setParameter("acc", accountNumber)
                .getResultList();
        session.close();
        return list;
    }
}
