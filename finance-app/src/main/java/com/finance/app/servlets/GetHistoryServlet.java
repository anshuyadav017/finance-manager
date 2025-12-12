package com.finance.app.servlets;

import jakarta.servlet.http.*;
import jakarta.servlet.ServletException;
import java.io.IOException;
import org.hibernate.Session;
import com.finance.persistence.util.HibernateUtil;
import com.finance.core.entity.Transaction;
import com.google.gson.Gson;
import java.util.List;

public class GetHistoryServlet extends HttpServlet {
    private final Gson gson = new Gson();
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        String accountIdStr = req.getParameter("accountId");
        
        if (accountIdStr == null) {
            resp.setStatus(400);
            resp.getWriter().write("{\"error\":\"accountId parameter required\"}");
            return;
        }
        
        long accountId;
        try {
            accountId = Long.parseLong(accountIdStr);
        } catch (NumberFormatException e) {
            resp.setStatus(400);
            resp.getWriter().write("{\"error\":\"Invalid accountId format\"}");
            return;
        }
        
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            List<Transaction> transactions = session.createQuery(
                "from Transaction t where t.account.id = :aid order by t.date desc", 
                Transaction.class)
                .setParameter("aid", accountId)
                .list();
            
            resp.setContentType("application/json");
            resp.getWriter().write(gson.toJson(transactions));
        } finally {
            session.close();
        }
    }
}
