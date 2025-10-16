package frontend.ui;

import backend.service.AlertService;
import backend.service.ChatbotService;
import backend.service.DomainFacade;
import util.DateUtil;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class MainFrame extends JFrame {
    private final DomainFacade domain;
    private final DashboardPanel dashboard = new DashboardPanel();
    private final TransactionsPanel transactions;
    private final BudgetsPanel budgets;
    private final ChatPanel chat;
    private final RulesPanel rulesPanel;

    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);

    public MainFrame(DomainFacade domain, ChatbotService bot, AlertService alerts) {
        super("Smart Finance Tracker");
        this.domain = domain;
        this.transactions = new TransactionsPanel(domain);
        this.budgets = new BudgetsPanel(domain);
        this.chat = new ChatPanel(domain, bot);
        this.rulesPanel = new RulesPanel(domain);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 680);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JList<String> nav = new JList<>(new String[]{"Dashboard","Transactions","Budgets","Rules","Chat"});
        nav.setSelectedIndex(0);
        nav.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) cards.show(content, nav.getSelectedValue());
        });
        add(new JScrollPane(nav), BorderLayout.WEST);

        content.add(dashboard, "Dashboard");
        content.add(transactions, "Transactions");
        content.add(budgets, "Budgets");
        content.add(rulesPanel, "Rules");
        content.add(chat, "Chat");
        add(content, BorderLayout.CENTER);

        refreshDashboard();

        Runnable check = () -> {
            var alertsList = domain.budgetAlerts(DateUtil.currentMonth());
            if (!alertsList.isEmpty()) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(this, String.join("\n", alertsList), "Budget Alerts", JOptionPane.WARNING_MESSAGE));
            }
        };
        alerts.scheduleDaily(check);
        check.run();
    }

    public void refreshDashboard() {
        String month = DateUtil.currentMonth();
        double inc = domain.monthIncome(month);
        double exp = domain.monthExpenses(month);
        dashboard.updateKpis(inc, exp);

        Map<String, Double> pie = new LinkedHashMap<>();
        domain.categoryBreakdown(month).forEach((cat, amt) -> pie.put(cat.getName(), amt));
        dashboard.setPieData(pie);

        dashboard.setBarData(domain.expensesByMonth(), domain.incomeByMonth());
    }
}
