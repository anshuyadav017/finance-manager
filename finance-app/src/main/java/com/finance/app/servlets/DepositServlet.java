package com.finance.app.servlets;

import jakarta.servlet.http.*;
import jakarta.servlet.ServletException;
import java.io.IOException;
import org.hibernate.Session;
import com.finance.persistence.util.HibernateUtil;
import com.finance.core.entity.Account;
import com.finance.core.entity.Transaction;
import com.google.gson.Gson;
import java.util.Date;
import java.math.BigDecimal;

public class DepositServlet extends HttpServlet {
    private final Gson gson = new Gson();
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        resp.setContentType("application/json");
        
        String accStr = req.getParameter("acc");
        String amountStr = req.getParameter("amount");
        String desc = req.getParameter("desc");
        
        if (accStr == null || amountStr == null) {
            resp.setStatus(400);
            resp.getWriter().write("{\"error\":\"Account number and amount required\"}");
            return;
        }
        
        long accountId;
        BigDecimal amount;
        try {
            accountId = Long.parseLong(accStr);
            amount = new BigDecimal(amountStr);
        } catch (NumberFormatException e) {
            resp.setStatus(400);
            resp.getWriter().write("{\"error\":\"Invalid account number or amount\"}");
            return;
        }
        
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            resp.setStatus(400);
            resp.getWriter().write("{\"error\":\"Amount must be greater than zero\"}");
            return;
        }
        
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            session.beginTransaction();
            
            Account account = session.get(Account.class, accountId);
            if (account == null) {
                session.getTransaction().rollback();
                resp.setStatus(404);
                resp.getWriter().write("{\"error\":\"Account not found\"}");
                return;
            }
            
            // Create transaction record
            Transaction tx = new Transaction();
            tx.setAccount(account);
            tx.setAmount(amount);
            tx.setType("deposit");
            tx.setDescription(desc != null ? desc : "Deposit");
            // Use reflection to set timestamp field
            try {
                java.lang.reflect.Field field = Transaction.class.getDeclaredField("timestamp");
                field.setAccessible(true);
                field.set(tx, new Date());
            } catch (Exception e) {
                // If timestamp doesn't exist, try createdAt
                try {
                    java.lang.reflect.Field field = Transaction.class.getDeclaredField("createdAt");
                    field.setAccessible(true);
                    field.set(tx, new Date());
                } catch (Exception ex) {
                    System.err.println("Could not set date field: " + ex.getMessage());
                }
            }
            
            // Update account balance
            BigDecimal newBalance = (account.getBalance() == null ? BigDecimal.ZERO : account.getBalance()).add(amount);
            account.setBalance(newBalance);
            
            session.persist(tx);
            session.merge(account);
            session.getTransaction().commit();
            
            resp.setStatus(200);
            resp.getWriter().write(gson.toJson(new DepositResponse(true, "Deposit successful", newBalance.doubleValue(), tx.getId())));
        } catch (Exception e) {
            if (session.getTransaction().isActive()) {
                session.getTransaction().rollback();
            }
            resp.setStatus(500);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
            e.printStackTrace();
        } finally {
            session.close();
        }
    }
    
    static class DepositResponse {
        boolean success;
        String message;
        double newBalance;
        long transactionId;
        
        DepositResponse(boolean success, String message, double newBalance, long transactionId) {
            this.success = success;
            this.message = message;
            this.newBalance = newBalance;
            this.transactionId = transactionId;
        }
    }
}
