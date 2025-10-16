package app;

import backend.dao.BudgetDAO;
import backend.dao.CategoryDAO;
import backend.dao.RuleDAO;
import backend.dao.TransactionDAO;
import backend.db.Migrations;
import backend.service.*;
import frontend.ui.MainFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}

            // Create tables and connect
            Migrations.init();

            // Demo popup to prove DB connection
            try (var con = backend.db.Db.get();
                 var st = con.createStatement();
                 var rs = st.executeQuery("SELECT 1")) {
                JOptionPane.showMessageDialog(null, "Database connected (SQLite)");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "DB connection failed: " + ex.getMessage());
            }

            // DAOs and services
            CategoryDAO cdao = new CategoryDAO();
            TransactionDAO tdao = new TransactionDAO();
            BudgetDAO bdao = new BudgetDAO();
            RuleDAO rdao = new RuleDAO();

            PredictionService pred = new PredictionService();
            AutoCategorizationService auto = new AutoCategorizationService(rdao, tdao);

            DomainFacade domain = new DomainFacade(tdao, cdao, bdao, pred, rdao, auto);
            domain.ensureSeed();

            ChatbotService bot = new ChatbotService();
            AlertService alerts = new AlertService();

            MainFrame frame = new MainFrame(domain, bot, alerts);
            frame.setVisible(true);
        });
    }
}
