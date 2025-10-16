package backend.service;

import backend.dao.BudgetDAO;
import backend.dao.CategoryDAO;
import backend.dao.RuleDAO;
import backend.dao.TransactionDAO;
import backend.model.Category;
import backend.model.Rule;
import backend.model.Transaction;
import util.DateUtil;

import java.time.LocalDate;
import java.util.*;

public class DomainFacade {
    private final TransactionDAO transactionDAO;
    private final CategoryDAO categoryDAO;
    private final BudgetDAO budgetDAO;
    private final PredictionService predictionService;
    private final RuleDAO ruleDAO;
    private final AutoCategorizationService auto;

    public DomainFacade(TransactionDAO t, CategoryDAO c, BudgetDAO b, PredictionService p,
                        RuleDAO r, AutoCategorizationService auto) {
        this.transactionDAO = t; this.categoryDAO = c; this.budgetDAO = b; this.predictionService = p;
        this.ruleDAO = r; this.auto = auto;
    }

    public void ensureSeed() { categoryDAO.ensureDefaults(); }

    public List<Category> categories() { return categoryDAO.findAll(); }
    public String[] categoryNames() { return categories().stream().map(Category::getName).toArray(String[]::new); }
    public String categoryNameById(int id) { Category c = categoryDAO.findById(id); return c != null ? c.getName() : ("#" + id); }

    public String thisMonthSummary(String month) {
        double income = transactionDAO.monthSum(month, "income");
        double expenses = transactionDAO.monthSum(month, "expense");
        double savings = income - expenses;
        return String.format("Month %s — Income: %.2f, Expenses: %.2f, Savings: %.2f", month, income, expenses, savings);
    }

    public Map<Category, Double> categoryBreakdown(String month) {
        Map<Integer, Double> raw = transactionDAO.categoryTotalsForMonth(month);
        Map<Category, Double> map = new LinkedHashMap<>();
        for (Map.Entry<Integer, Double> e : raw.entrySet()) {
            Category c = categoryDAO.findById(e.getKey());
            if (c != null) map.put(c, e.getValue());
        }
        return map;
    }

    public String setBudget(String categoryName, double amount, String month) {
        Category c = categoryDAO.findByName(categoryName);
        if (c == null) return "Unknown category: " + categoryName;
        budgetDAO.upsertMonthlyBudget(c.getId(), month, amount);
        return null;
    }

    public Map<Category, Double> monthlyBudgets(String month) {
        Map<Integer, Double> raw = budgetDAO.monthlyBudgets(month);
        Map<Category, Double> map = new LinkedHashMap<>();
        for (Map.Entry<Integer, Double> e : raw.entrySet()) {
            Category c = categoryDAO.findById(e.getKey());
            if (c != null) map.put(c, e.getValue());
        }
        return map;
    }

    public String predictNextMonth(String categoryName) {
        Category c = categoryDAO.findByName(categoryName);
        if (c == null) return "Unknown category: " + categoryName;
        double[] series = transactionDAO.lastNMonthsTotalsForCategory(c.getId(), 12);
        double pred = predictionService.predictNext(series);
        return String.format("Predicted '%s' next month: %.2f", c.getName(), pred);
    }

    public void addTransaction(Transaction t) {
        transactionDAO.insert(t);
        if ("expense".equalsIgnoreCase(t.getType()) && t.getCategoryId() != null) {
            String text = safeText(t.getMerchant()) + " " + safeText(t.getNote());
            auto.observe(t.getCategoryId(), text);
        }
    }

    public String addTransactionAuto(Transaction t) {
        if (!"expense".equalsIgnoreCase(t.getType())) {
            transactionDAO.insert(t);
            return "Income recorded.";
        }
        AutoCategorizationService.Result res = auto.categorize(t.getMerchant(), t.getNote());
        Integer cid = res.categoryId;
        if (cid == null) cid = otherCategoryId();
        t.setCategoryId(cid);
        transactionDAO.insert(t);
        String cname = categoryNameById(cid);
        String why = res.viaRule ? ("rule '" + res.matchedKeyword + "'") : String.format("confidence %.2f", res.confidence);
        if (!res.viaRule && res.categoryId == null) why = "fallback to Other";
        auto.observe(cid, safeText(t.getMerchant()) + " " + safeText(t.getNote()));
        return "Auto-categorized to " + cname + " (" + why + ").";
    }

    private String safeText(String s) { return s == null ? "" : s; }

    private Integer otherCategoryId() {
        Category other = categoryDAO.findByName("Other");
        if (other != null) return other.getId();
        List<Category> cats = categoryDAO.findAll();
        return cats.isEmpty() ? null : cats.get(0).getId();
    }

    public String quickAddExpense(double amount, String categoryName) {
        Category c = categoryDAO.findByName(categoryName);
        if (c == null) return "Unknown category: " + categoryName;
        Transaction t = new Transaction();
        t.setType("expense");
        t.setAmount(amount);
        t.setOccurredAt(LocalDate.now());
        t.setCategoryId(c.getId());
        t.setMerchant("");
        t.setNote("chat-added");
        addTransaction(t);
        return "Expense added to " + c.getName() + ".";
    }

    public String quickAddExpenseAuto(double amount, String text) {
        Transaction t = new Transaction();
        t.setType("expense");
        t.setAmount(amount);
        t.setOccurredAt(LocalDate.now());
        t.setMerchant(text);
        t.setNote("chat-added");
        return addTransactionAuto(t);
    }

    public double monthIncome(String month) { return transactionDAO.monthSum(month, "income"); }
    public double monthExpenses(String month) { return transactionDAO.monthSum(month, "expense"); }
    public Map<String, Double> expensesByMonth() { return transactionDAO.monthTotals(); }
    public Map<String, Double> incomeByMonth() { return transactionDAO.monthTotalsIncome(); }
    public List<Transaction> transactionsForMonth(String month) { return transactionDAO.listByMonth(month); }

    public java.util.List<String> budgetAlerts(String month) {
        Map<Category, Double> budgets = monthlyBudgets(month);
        Map<Category, Double> spent = categoryBreakdown(month);
        java.util.List<String> alerts = new java.util.ArrayList<>();
        for (Category c : budgets.keySet()) {
            double b = budgets.get(c);
            double s = spent.getOrDefault(c, 0.0);
            if (b > 0 && s >= b) alerts.add("Budget reached for " + c.getName() + " (" + s + "/" + b + ")");
            else if (b > 0 && s > 0.9*b) alerts.add("Over 90% of " + c.getName() + " budget (" + s + "/" + b + ")");
        }
        return alerts;
    }

    public java.util.List<Rule> listRules() { return ruleDAO.findAll(); }
    public String addRule(String keyword, String categoryName) {
        if (keyword == null || keyword.trim().isEmpty()) return "Keyword cannot be empty.";
        Category c = categoryDAO.findByName(categoryName);
        if (c == null) return "Unknown category: " + categoryName;
        ruleDAO.insert(keyword.trim().toLowerCase(), c.getId());
        auto.reloadRules();
        return null;
    }
    public void deleteRule(int id) { ruleDAO.delete(id); auto.reloadRules(); }

    public String ensureMonthlyBudgetsForAll(String month, double defaultAmount) {
        for (Category c : categories()) {
            if ("Income".equalsIgnoreCase(c.getName())) continue;
            budgetDAO.upsertMonthlyBudget(c.getId(), month, defaultAmount);
        }
        return "Budgets upserted.";
    }
}
