package com.finance.app.servlets;

import jakarta.servlet.http.*;
import jakarta.servlet.ServletException;
import java.io.IOException;
import org.hibernate.Session;
import com.finance.persistence.util.HibernateUtil;
import com.finance.core.entity.Account;
import com.google.gson.Gson;
import java.math.BigDecimal;

public class CreateServlet extends HttpServlet {
    private final Gson gson = new Gson();
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        resp.setContentType("application/json");
        
        String name = req.getParameter("name");
        String email = req.getParameter("email");
        String phone = req.getParameter("phone");
        
        if (name == null || name.trim().isEmpty()) {
            resp.setStatus(400);
            resp.getWriter().write("{\"error\":\"Name is required\"}");
            return;
        }
        
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            session.beginTransaction();
            
            Account account = new Account();
            account.setHolderName(name);
            account.setEmail(email);
            account.setPhone(phone);
            account.setBalance(BigDecimal.ZERO);
            
            session.persist(account);
            session.getTransaction().commit();
            
            resp.setStatus(200);
            resp.getWriter().write(gson.toJson(account));
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
}
