package frontend.ui;

import backend.model.Rule;
import backend.service.DomainFacade;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class RulesPanel extends JPanel {
    private final DomainFacade domain;
    private final RulesModel model = new RulesModel();
    private final JTable table = new JTable(model);
    private final JTextField keywordField = new JTextField(12);
    private final JComboBox<String> categoryCombo;

    public RulesPanel(DomainFacade domain) {
        this.domain = domain;
        this.categoryCombo = new JComboBox<>(domain.categoryNames());

        setLayout(new BorderLayout(10,10));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBtn = new JButton("Add");
        JButton delBtn = new JButton("Delete");
        JButton reloadBtn = new JButton("Reload");
        top.add(new JLabel("Keyword:")); top.add(keywordField);
        top.add(new JLabel("Category:")); top.add(categoryCombo);
        top.add(addBtn); top.add(delBtn); top.add(reloadBtn);
        add(top, BorderLayout.NORTH);

        add(new JScrollPane(table), BorderLayout.CENTER);

        addBtn.addActionListener(e -> onAdd());
        delBtn.addActionListener(e -> onDelete());
        reloadBtn.addActionListener(e -> reload());

        reload();
    }

    private void onAdd() {
        String kw = keywordField.getText().trim();
        String cat = (String) categoryCombo.getSelectedItem();
        String res = domain.addRule(kw, cat);
        if (res != null) JOptionPane.showMessageDialog(this, res, "Error", JOptionPane.ERROR_MESSAGE);
        keywordField.setText("");
        reload();
    }

    private void onDelete() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        int id = model.getRuleId(row);
        domain.deleteRule(id);
        reload();
    }

    private void reload() {
        List<Rule> rules = domain.listRules();
        model.setData(rules, domain);
    }

    private static class RulesModel extends AbstractTableModel {
        private final String[] cols = {"ID","Keyword","Category"};
        private final List<Rule> data = new ArrayList<>();
        private final List<String> catNames = new ArrayList<>();

        public void setData(List<Rule> rules, DomainFacade domain) {
            data.clear(); catNames.clear();
            for (Rule r : rules) {
                data.add(r);
                catNames.add(domain.categoryNameById(r.getCategoryId()));
            }
            fireTableDataChanged();
        }

        public int getRowCount() { return data.size(); }
        public int getColumnCount() { return cols.length; }
        public String getColumnName(int c) { return cols[c]; }
        public Object getValueAt(int r, int c) {
            Rule rule = data.get(r);
            switch (c) {
                case 0: return rule.getId();
                case 1: return rule.getKeyword();
                case 2: return catNames.get(r);
                default: return "";
            }
        }
        public int getRuleId(int row) { return data.get(row).getId(); }
    }
}
