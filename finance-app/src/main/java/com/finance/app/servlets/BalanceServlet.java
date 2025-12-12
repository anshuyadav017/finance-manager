package com.finance.app.servlets;

import jakarta.servlet.http.*;
import jakarta.servlet.ServletException;
import java.io.IOException;
import org.hibernate.Session;
import com.finance.persistence.util.HibernateUtil;
import com.finance.core.entity.Account;
import com.google.gson.Gson;
import java.math.BigDecimal;

public class BalanceServlet extends HttpServlet {
    private final Gson gson = new Gson();
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        resp.setContentType("application/json");
        
        String accStr = req.getParameter("acc");
        if (accStr == null) {
            resp.setStatus(400);
            resp.getWriter().write("{\"error\":\"Account number required\"}");
            return;
        }
        
        long accountId;
        try {
            accountId = Long.parseLong(accStr);
        } catch (NumberFormatException e) {
            resp.setStatus(400);
            resp.getWriter().write("{\"error\":\"Invalid account number\"}");
            return;
        }
        
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            Account account = session.get(Account.class, accountId);
            if (account == null) {
                resp.setStatus(404);
                resp.getWriter().write("{\"error\":\"Account not found\"}");
                return;
            }
            
            BigDecimal balance = account.getBalance() == null ? BigDecimal.ZERO : account.getBalance();
            resp.setStatus(200);
            resp.getWriter().write(String.valueOf(balance.doubleValue()));
        } catch (Exception e) {
            resp.setStatus(500);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
            e.printStackTrace();
        } finally {
            session.close();
        }
    }
}
