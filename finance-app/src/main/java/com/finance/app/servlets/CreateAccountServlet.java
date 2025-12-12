package com.finance.app.servlets;

import jakarta.servlet.http.*;
import jakarta.servlet.ServletException;
import java.io.IOException;
import org.hibernate.Session;
import com.finance.persistence.util.HibernateUtil;
import com.finance.core.entity.Account;
import com.google.gson.Gson;

public class CreateAccountServlet extends HttpServlet {
    private final Gson gson = new Gson();
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        String holderName = req.getParameter("holderName");
        String email = req.getParameter("email");
        String phone = req.getParameter("phone");
        
        if (holderName == null || holderName.trim().isEmpty()) {
            resp.setStatus(400);
            resp.getWriter().write("{\"error\":\"holderName is required\"}");
            return;
        }
        
        Account acc = new Account();
        acc.setHolderName(holderName);
        acc.setEmail(email);
        acc.setPhone(phone);
        acc.setBalance(0.0);
        
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            session.beginTransaction();
            session.persist(acc);
            session.getTransaction().commit();
            
            resp.setContentType("application/json");
            resp.getWriter().write(gson.toJson(acc));
        } catch (Exception e) {
            session.getTransaction().rollback();
            resp.setStatus(500);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        } finally {
            session.close();
        }
    }
}
