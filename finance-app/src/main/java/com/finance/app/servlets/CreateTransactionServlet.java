package com.finance.app.servlets;

import jakarta.servlet.http.*;
import jakarta.servlet.ServletException;
import java.io.IOException;
import org.hibernate.Session;
import com.finance.persistence.util.HibernateUtil;
import com.finance.core.entity.Transaction;
import com.finance.core.entity.Account;
import com.google.gson.Gson;
import java.util.Date;

public class CreateTransactionServlet extends HttpServlet {
    private final Gson gson = new Gson();
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        String accountIdStr = req.getParameter("accountId");
        String amountStr = req.getParameter("amount");
        String type = req.getParameter("type");
        String description = req.getParameter("description");
        
        if (accountIdStr == null || amountStr == null) {
            resp.setStatus(400);
            resp.getWriter().write("{\"error\":\"accountId and amount are required\"}");
            return;
        }
        
        long accountId;
        double amount;
        try {
            accountId = Long.parseLong(accountIdStr);
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            resp.setStatus(400);
            resp.getWriter().write("{\"error\":\"Invalid number format\"}");
            return;
        }
        
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            session.beginTransaction();
            
            Account acc = session.get(Account.class, accountId);
            if (acc == null) {
                session.getTransaction().rollback();
                resp.setStatus(404);
                resp.getWriter().write("{\"error\":\"Account not found\"}");
                return;
            }
            
            Transaction tx = new Transaction();
            tx.setAccount(acc);
            tx.setAmount(amount);
            tx.setType(type == null ? "OTHER" : type);
            tx.setDescription(description);
            tx.setDate(new Date());
            
            session.persist(tx);
            
            // Update account balance
            double balance = acc.getBalance() == null ? 0.0 : acc.getBalance();
            if ("WITHDRAWAL".equalsIgnoreCase(type)) {
                balance -= amount;
            } else {
                balance += amount;
            }
            acc.setBalance(balance);
            session.merge(acc);
            
            session.getTransaction().commit();
            
            resp.setContentType("application/json");
            resp.getWriter().write(gson.toJson(tx));
        } catch (Exception e) {
            session.getTransaction().rollback();
            resp.setStatus(500);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        } finally {
            session.close();
        }
    }
}
