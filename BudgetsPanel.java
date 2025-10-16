package frontend.ui;

import backend.model.Category;
import backend.service.DomainFacade;
import util.DateUtil;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class BudgetsPanel extends JPanel {
    private final DomainFacade domain;
    private final DefaultListModel<String> model = new DefaultListModel<>();
    private final JList<String> list = new JList<>(model);
    private String month = DateUtil.currentMonth();

    public BudgetsPanel(DomainFacade domain) {
        this.domain = domain;
        setLayout(new BorderLayout(10,10));
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField monthField = new JTextField(month, 7);
        JTextField catField = new JTextField(10);
        JTextField amtField = new JTextField(6);
        JButton setBtn = new JButton("Set/Update");
        JButton seedBtn = new JButton("Seed defaults ($300 each)");
        top.add(new JLabel("Month:")); top.add(monthField);
        top.add(new JLabel("Category:")); top.add(catField);
        top.add(new JLabel("Amount:")); top.add(amtField);
        top.add(setBtn); top.add(seedBtn);
        add(top, BorderLayout.NORTH);
        add(new JScrollPane(list), BorderLayout.CENTER);

        setBtn.addActionListener(e -> {
            try {
                String m = monthField.getText().trim();
                String c = catField.getText().trim();
                double a = Double.parseDouble(amtField.getText().trim());
                String res = domain.setBudget(c, a, m);
                if (res == null) reload(m); else JOptionPane.showMessageDialog(this, res);
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Invalid input: "+ex.getMessage()); }
        });
        seedBtn.addActionListener(e -> {
            String m = monthField.getText().trim();
            String msg = domain.ensureMonthlyBudgetsForAll(m, 300.0);
            JOptionPane.showMessageDialog(this, msg);
            reload(m);
        });

        reload(month);
    }

    public void reload(String m) {
        this.month = m;
        model.clear();
        Map<Category, Double> budgets = domain.monthlyBudgets(month);
        Map<Category, Double> spent = domain.categoryBreakdown(month);
        for (Category c : new LinkedHashMap<>(budgets).keySet()) {
            double b = budgets.get(c);
            double s = spent.getOrDefault(c, 0.0);
            model.addElement(c.getName() + " — Budget: " + b + " | Spent: " + s +
                    " | " + (b > 0 ? String.format("%.0f%%", (s/b)*100) : "No budget"));
        }
        if (model.isEmpty()) model.addElement("No budgets for " + month + ". Use controls above to add.");
    }
}
