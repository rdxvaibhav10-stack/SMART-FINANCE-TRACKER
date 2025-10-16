package frontend.ui;

import frontend.ui.charts.BarChartPanel;
import frontend.ui.charts.PieChartPanel;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class DashboardPanel extends JPanel {
    private final JLabel incomeLbl = new JLabel("$0.00", SwingConstants.CENTER);
    private final JLabel expenseLbl = new JLabel("$0.00", SwingConstants.CENTER);
    private final JLabel savingsLbl = new JLabel("$0.00", SwingConstants.CENTER);
    private final PieChartPanel pie = new PieChartPanel();
    private final BarChartPanel bars = new BarChartPanel();

    public DashboardPanel() {
        setLayout(new BorderLayout(10,10));
        JPanel kpis = new JPanel(new GridLayout(1,3,8,8));
        kpis.add(card("Income", incomeLbl));
        kpis.add(card("Expenses", expenseLbl));
        kpis.add(card("Savings", savingsLbl));
        add(kpis, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, wrap("Category Breakdown", pie), wrap("Monthly Trend", bars));
        split.setResizeWeight(0.5);
        add(split, BorderLayout.CENTER);
    }

    private JPanel card(String title, JLabel value) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        value.setFont(value.getFont().deriveFont(Font.BOLD, 18f));
        p.add(value, BorderLayout.CENTER);
        return p;
    }
    private JPanel wrap(String title, JComponent c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(c, BorderLayout.CENTER);
        return p;
    }

    public void updateKpis(double income, double expenses) {
        incomeLbl.setText(String.format("$%.2f", income));
        expenseLbl.setText(String.format("$%.2f", expenses));
        savingsLbl.setText(String.format("$%.2f", income - expenses));
    }

    public void setPieData(Map<String, Double> data) { pie.setData(data); }
    public void setBarData(Map<String, Double> exp, Map<String, Double> inc) { bars.setData(exp, inc); }

    public void clear() {
        updateKpis(0,0);
        pie.setData(new LinkedHashMap<>());
        bars.setData(new LinkedHashMap<>(), new LinkedHashMap<>());
    }
}
