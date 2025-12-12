package com.finance.app;

import com.google.gson.Gson;
import com.finance.account.service.AccountService;
import com.finance.account.service.impl.AccountServiceImpl;
import com.finance.core.entity.Account;
import com.finance.core.entity.Transaction;
import com.finance.transaction.service.TransactionService;
import com.finance.transaction.service.impl.TransactionServiceImpl;
import com.finance.persistence.util.HibernateUtil;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public class WebServer {
    private static final Gson gson = new Gson();
    private final AccountService accountService = new AccountServiceImpl();
    private final TransactionService txService = new TransactionServiceImpl();

    public void start(int port) throws Exception {
        Server server = new Server(port);
        ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        handler.setContextPath("/");
        server.setHandler(handler);

        // Simple index page
        handler.addServlet(IndexServlet.class, "/");

        // API servlets
        handler.addServlet(CreateAccountServlet.class, "/api/create");
        handler.addServlet(DepositServlet.class, "/api/deposit");
        handler.addServlet(WithdrawServlet.class, "/api/withdraw");
        handler.addServlet(HistoryServlet.class, "/api/history");

        server.start();
        System.out.println("Server started at http://localhost:" + port);
        server.join();
    }

    // IndexServlet serves a simple HTML with forms (see below)
    public static class IndexServlet extends HttpServlet {
        @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setContentType("text/html");
            resp.getWriter().write("<html><head><meta charset='utf-8'><title>Finance Manager</title></head><body>" +
                    "<h1>Finance Manager</h1>" +
                    "<p>Create account form (POST to /api/create with holderName,email,phone)</p>" +
                    "<p>Deposit form (POST to /api/deposit with acc,amount,desc)</p>" +
                    "<p>Withdraw form (POST to /api/withdraw with acc,amount,desc)</p>" +
                    "<p>View history (GET /api/history?acc=ACC...)</p>" +
                    "</body></html>");
        }
    }

    public static class CreateAccountServlet extends HttpServlet {
        private final AccountService accountService = new AccountServiceImpl();
        @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            String name = req.getParameter("holderName");
            String email = req.getParameter("email");
            String phone = req.getParameter("phone");
            Account a = accountService.createAccount(name, email, phone);
            resp.setContentType("application/json");
            resp.getWriter().write(gson.toJson(a));
        }
    }

    public static class DepositServlet extends HttpServlet {
        private final TransactionService txService = new TransactionServiceImpl();
        @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            String acc = req.getParameter("acc");
            String amount = req.getParameter("amount");
            String desc = req.getParameter("desc");
            txService.deposit(acc, new BigDecimal(amount), desc);
            resp.getWriter().write("{\"status\":\"ok\"}");
        }
    }

    public static class WithdrawServlet extends HttpServlet {
        private final TransactionService txService = new TransactionServiceImpl();
        @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            String acc = req.getParameter("acc");
            String amount = req.getParameter("amount");
            String desc = req.getParameter("desc");
            try {
                txService.withdraw(acc, new BigDecimal(amount), desc);
                resp.getWriter().write("{\"status\":\"ok\"}");
            } catch (RuntimeException e) {
                resp.setStatus(400);
                resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
            }
        }
    }

    public static class HistoryServlet extends HttpServlet {
        private final TransactionService txService = new TransactionServiceImpl();
        @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            String acc = req.getParameter("acc");
            List<Transaction> list = txService.getHistory(acc);
            resp.setContentType("application/json");
            resp.getWriter().write(gson.toJson(list));
        }
    }

    // Add a main to run server quickly:
    public static void main(String[] args) throws Exception {
        WebServer s = new WebServer();
        s.start(8080);
        // On shutdown:
        Runtime.getRuntime().addShutdownHook(new Thread(() -> HibernateUtil.shutdown()));
    }
}
