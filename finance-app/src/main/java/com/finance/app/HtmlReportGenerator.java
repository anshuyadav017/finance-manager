package com.finance.app;

import com.finance.transaction.service.TransactionService;
import com.finance.transaction.service.impl.TransactionServiceImpl;
import com.finance.core.entity.Transaction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;

public class HtmlReportGenerator {

    private final TransactionService txService = new TransactionServiceImpl();

    /**
     * Generate a basic HTML report for an account and write to outputFile.
     */
    public void generate(String accountNumber, File outputFile) throws Exception {
        List<Transaction> txs = txService.getHistory(accountNumber);

        try (BufferedWriter w = new BufferedWriter(new FileWriter(outputFile))) {
            w.write("<!doctype html><html><head><meta charset='utf-8'><title>Finance Report</title>");
            w.write("<style>body{font-family:Arial;margin:20px}table{border-collapse:collapse;width:100%}th,td{border:1px solid #ddd;padding:8px}th{background:#f4f4f4}</style>");
            w.write("</head><body>");
            w.write("<h1>Account: " + accountNumber + "</h1>");
            w.write("<h3>Transaction History</h3>");
            w.write("<table><thead><tr><th>Date</th><th>Type</th><th>Amount</th><th>Balance After</th><th>Description</th></tr></thead><tbody>");
            for (Transaction t : txs) {
                w.write("<tr>");
                w.write("<td>" + t.getCreatedAt() + "</td>");
                w.write("<td>" + t.getType() + "</td>");
                w.write("<td>" + t.getAmount() + "</td>");
                w.write("<td>" + t.getBalanceAfter() + "</td>");
                w.write("<td>" + (t.getDescription() == null ? "" : t.getDescription()) + "</td>");
                w.write("</tr>");
            }
            w.write("</tbody></table>");
            w.write("</body></html>");
        }
    }
}
