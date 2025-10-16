package frontend.ui;

import backend.model.Transaction;
import backend.service.DomainFacade;
import util.DateUtil;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.List;

public class TransactionsPanel extends JPanel {
    private final DomainFacade domain;
    private final JTable table = new JTable();
    private final TxModel model = new TxModel();
    private String currentMonth = DateUtil.currentMonth();

    public TransactionsPanel(DomainFacade domain) {
        this.domain = domain;
        setLayout(new BorderLayout(10,10));
        table.setModel(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField monthField = new JTextField(currentMonth, 7);
        JButton reload = new JButton("Load");
        JButton add = new JButton("Add");
        top.add(new JLabel("Month (YYYY-MM):"));
        top.add(monthField); top.add(reload); top.add(add);
        add(top, BorderLayout.NORTH);

        reload.addActionListener(e -> { currentMonth = monthField.getText().trim(); reload(); });
        add.addActionListener(e -> onAdd());

        reload();
    }

    private void onAdd() {
        TransactionDialog dlg = new TransactionDialog(SwingUtilities.getWindowAncestor(this), domain.categoryNames());
        dlg.setVisible(true);
        if (!dlg.isOk()) return;

        try {
            Transaction t = dlg.toTransaction();
            if ("income".equalsIgnoreCase(t.getType())) {
                t.setCategoryId(null);
                domain.addTransaction(t);
                JOptionPane.showMessageDialog(this, "Income added.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                if (dlg.isAutoCategorize()) {
                    String msg = domain.addTransactionAuto(t);
                    JOptionPane.showMessageDialog(this, msg, "Auto-categorized", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    String catName = dlg.getSelectedCategoryName();
                    Integer id = domain.categories().stream()
                            .filter(c -> c.getName().equals(catName)).findFirst().map(c -> c.getId()).orElse(null);
                    t.setCategoryId(id);
                    domain.addTransaction(t);
                    JOptionPane.showMessageDialog(this, "Expense added to " + catName + ".", "Success", JOptionPane.INFORMATION_MESSAGE);
                }
            }
            reload();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid input: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void reload() {
        List<Transaction> list = domain.transactionsForMonth(currentMonth);
        model.setData(list);
    }

    private static class TxModel extends AbstractTableModel {
        private List<Transaction> data = java.util.Collections.emptyList();
        private final String[] cols = {"Date","Type","Amount","CategoryId","Merchant","Note"};

        public void setData(List<Transaction> d) { this.data = d; fireTableDataChanged(); }
        public int getRowCount() { return data.size(); }
        public int getColumnCount() { return cols.length; }
        public String getColumnName(int c) { return cols[c]; }

        public Object getValueAt(int r, int c) {
            Transaction t = data.get(r);
            switch (c) {
                case 0: return t.getOccurredAt();
                case 1: return t.getType();
                case 2: return String.format("%.2f", t.getAmount());
                case 3: return t.getCategoryId();
                case 4: return t.getMerchant();
                case 5: return t.getNote();
                default: return "";
            }
        }
    }
}
