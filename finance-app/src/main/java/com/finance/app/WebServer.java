package com.finance.app;

import com.google.gson.Gson;
import com.finance.account.service.AccountService;
import com.finance.account.service.impl.AccountServiceImpl;
import com.finance.core.entity.Account;
import com.finance.core.entity.Transaction;
import com.finance.transaction.service.TransactionService;
import com.finance.transaction.service.impl.TransactionServiceImpl;
import com.finance.persistence.util.HibernateUtil;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.ee10.servlet.DefaultServlet;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.handler.HandlerList;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public class WebServer {
    private static final Gson gson = new Gson();

    public static void main(String[] args) throws Exception {
        int port = 8080;
        Server server = new Server(port);
        
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        
        // Register API servlets
        context.addServlet(CreateServlet.class, "/api/create");
        context.addServlet(DepositServlet.class, "/api/deposit");
        context.addServlet(WithdrawServlet.class, "/api/withdraw");
        context.addServlet(BalanceServlet.class, "/api/balance");
        context.addServlet(HistoryServlet.class, "/api/history");
        context.addServlet(TopExpensesServlet.class, "/api/top-expenses");
        context.addServlet(TopCategoriesServlet.class, "/api/top-categories");
        context.addServlet(MonthlyAverageServlet.class, "/api/monthly-average");
        context.addServlet(BudgetAnalysisServlet.class, "/api/budget-analysis");
        context.addServlet(UndoServlet.class, "/api/undo");
        context.addServlet(FraudDetectionServlet.class, "/api/fraud");
        context.addServlet(CategorySuggestionsServlet.class, "/api/suggestions");
        context.addServlet(AllTransactionsServlet.class, "/api/all-transactions");

        // Serve static files
        ResourceHandler staticHandler = new ResourceHandler();
        staticHandler.setDirAllowed(false);
        
        HandlerList handlers = new HandlerList();
        handlers.addHandler(staticHandler);
        handlers.addHandler(context);
        
        server.setHandler(handlers);
        
        System.out.println("Server starting on http://localhost:" + port);
        server.start();
        server.join();
    }

    public static class CreateServlet extends HttpServlet {
        private final AccountService accountService = new AccountServiceImpl();

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            String name = req.getParameter("holderName");
            String email = req.getParameter("email");
            String phone = req.getParameter("phone");
            if (name == null || name.trim().isEmpty()) {
                resp.setStatus(400);
                resp.getWriter().write("{\"error\":\"holderName required\"}");
                return;
            }
            Account a = accountService.createAccount(name, email, phone);
            resp.setContentType("application/json");
            resp.getWriter().write(gson.toJson(a));
        }
    }

    public static class DepositServlet extends HttpServlet {
        private final TransactionService txService = new TransactionServiceImpl();

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            String acc = req.getParameter("acc");
            String amt = req.getParameter("amount");
            String desc = req.getParameter("desc");
            try {
                txService.deposit(acc, new BigDecimal(amt), desc);
                resp.setContentType("application/json");
                resp.getWriter().write("{\"status\":\"ok\"}");
            } catch (Exception e) {
                resp.setStatus(400);
                resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
            }
        }
    }

    public static class WithdrawServlet extends HttpServlet {
        private final TransactionService txService = new TransactionServiceImpl();

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            String acc = req.getParameter("acc");
            String amt = req.getParameter("amount");
            String desc = req.getParameter("desc");
            try {
                txService.withdraw(acc, new BigDecimal(amt), desc);
                resp.setContentType("application/json");
                resp.getWriter().write("{\"status\":\"ok\"}");
            } catch (Exception e) {
                resp.setStatus(400);
                resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
            }
        }
    }

    public static class BalanceServlet extends HttpServlet {
        private final AccountService accountService = new AccountServiceImpl();

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            String acc = req.getParameter("acc");
            Account a = accountService.getByAccountNumber(acc);
            if (a == null) {
                resp.setStatus(404);
                resp.getWriter().write("{\"error\":\"account not found\"}");
                return;
            }
            resp.setContentType("application/json");
            resp.getWriter().write(gson.toJson(a));
        }
    }

    public static class HistoryServlet extends HttpServlet {
        private final TransactionService txService = new TransactionServiceImpl();

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            String acc = req.getParameter("acc");
            List<Transaction> list = txService.getHistory(acc);
            resp.setContentType("application/json");
            resp.getWriter().write(gson.toJson(list));
        }
    }

    public static class TopExpensesServlet extends HttpServlet {
        private final TransactionService txService = new TransactionServiceImpl();

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            int k = Integer.parseInt(req.getParameter("k") != null ? req.getParameter("k") : "5");
            List<Object[]> list = txService.getTopExpenses(k);
            resp.setContentType("application/json");
            resp.getWriter().write(gson.toJson(list));
        }
    }

    public static class TopCategoriesServlet extends HttpServlet {
        private final TransactionService txService = new TransactionServiceImpl();

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            int k = Integer.parseInt(req.getParameter("k") != null ? req.getParameter("k") : "3");
            List<Object[]> list = txService.getTopCategories(k);
            resp.setContentType("application/json");
            resp.getWriter().write(gson.toJson(list));
        }
    }

    public static class MonthlyAverageServlet extends HttpServlet {
        private final TransactionService txService = new TransactionServiceImpl();

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            int months = Integer.parseInt(req.getParameter("months") != null ? req.getParameter("months") : "3");
            double avg = txService.getMonthlyAverage(months);
            resp.setContentType("application/json");
            resp.getWriter().write("{\"average\":" + avg + "}");
        }
    }

    public static class BudgetAnalysisServlet extends HttpServlet {
        private final TransactionService txService = new TransactionServiceImpl();

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            double budget = Double.parseDouble(req.getParameter("budget"));
            String analysis = txService.analyzeBudget(budget);
            resp.setContentType("application/json");
            resp.getWriter().write("{\"analysis\":\"" + analysis + "\"}");
        }
    }

    public static class UndoServlet extends HttpServlet {
        private final TransactionService txService = new TransactionServiceImpl();

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            boolean success = txService.undoLastTransaction();
            resp.setContentType("application/json");
            resp.getWriter().write("{\"success\":" + success + "}");
        }
    }

    public static class FraudDetectionServlet extends HttpServlet {
        private final TransactionService txService = new TransactionServiceImpl();

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            List<String> frauds = txService.detectFraud();
            resp.setContentType("application/json");
            resp.getWriter().write(gson.toJson(frauds));
        }
    }

    public static class CategorySuggestionsServlet extends HttpServlet {
        private final TransactionService txService = new TransactionServiceImpl();

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            String prefix = req.getParameter("prefix");
            List<String> suggestions = txService.getCategorySuggestions(prefix);
            resp.setContentType("application/json");
            resp.getWriter().write(gson.toJson(suggestions));
        }
    }

    public static class AllTransactionsServlet extends HttpServlet {
        private final TransactionService txService = new TransactionServiceImpl();

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            List<Transaction> list = txService.getAllTransactions();
            resp.setContentType("application/json");
            resp.getWriter().write(gson.toJson(list));
        }
    }
}
