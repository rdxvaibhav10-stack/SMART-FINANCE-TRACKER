package frontend.ui;

import backend.model.Transaction;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

public class TransactionDialog extends JDialog {
    private final JComboBox<String> type = new JComboBox<>(new String[]{"expense","income"});
    private final JTextField amount = new JTextField();
    private final JTextField date = new JTextField(LocalDate.now().toString());
    private final JComboBox<String> category;
    private final JCheckBox auto = new JCheckBox("Auto categorize (by merchant/note)", true);
    private final JTextField merchant = new JTextField();
    private final JTextField note = new JTextField();
    private boolean ok = false;

    public TransactionDialog(Window owner, String[] categoryNames) {
        super(owner, "Add Transaction", ModalityType.APPLICATION_MODAL);
        this.category = new JComboBox<>(categoryNames);
        setLayout(new BorderLayout(10,10));
        JPanel form = new JPanel(new GridLayout(0,2,6,6));
        form.add(new JLabel("Type:")); form.add(type);
        form.add(new JLabel("Amount:")); form.add(amount);
        form.add(new JLabel("Date (YYYY-MM-DD):")); form.add(date);
        form.add(new JLabel("Category:")); form.add(category);
        form.add(new JLabel("Auto:")); form.add(auto);
        form.add(new JLabel("Merchant:")); form.add(merchant);
        form.add(new JLabel("Note:")); form.add(note);
        add(form, BorderLayout.CENTER);
        JButton addBtn = new JButton("Add");
        JButton cancelBtn = new JButton("Cancel");
        JPanel actions = new JPanel(); actions.add(addBtn); actions.add(cancelBtn);
        add(actions, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> { ok = true; setVisible(false); });
        cancelBtn.addActionListener(e -> setVisible(false));

        type.addActionListener(e -> updateState());
        auto.addActionListener(e -> updateState());
        updateState();

        setSize(420, 300); setLocationRelativeTo(owner);
    }

    private void updateState() {
        boolean isExpense = "expense".equalsIgnoreCase((String) type.getSelectedItem());
        auto.setEnabled(isExpense);
        boolean autoOn = isExpense && auto.isSelected();
        category.setEnabled(isExpense && !autoOn);
        merchant.setEnabled(true);
        note.setEnabled(true);
    }

    public boolean isOk() { return ok; }
    public boolean isAutoCategorize() { return auto.isEnabled() && auto.isSelected(); }

    public Transaction toTransaction() {
        Transaction t = new Transaction();
        t.setType((String) type.getSelectedItem());
        t.setAmount(Double.parseDouble(amount.getText().trim()));
        t.setOccurredAt(LocalDate.parse(date.getText().trim()));
        t.setMerchant(merchant.getText().trim());
        t.setNote(note.getText().trim());
        return t;
    }

    public String getSelectedCategoryName() {
        return (String) category.getSelectedItem();
    }
}
