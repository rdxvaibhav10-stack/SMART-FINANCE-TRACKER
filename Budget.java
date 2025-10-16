package backend.model;

public class Budget {
    private int id;
    private int categoryId;
    private String period;     // monthly
    private double amount;
    private String startDate;  // YYYY-MM-01

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
}
