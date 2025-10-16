package backend.model;

import java.time.LocalDate;

public class Transaction {
    private int id;
    private String type;           // expense | income
    private double amount;
    private LocalDate occurredAt;
    private Integer categoryId;
    private String merchant;
    private String note;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public LocalDate getOccurredAt() { return occurredAt; }
    public void setOccurredAt(LocalDate occurredAt) { this.occurredAt = occurredAt; }
    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
    public String getMerchant() { return merchant; }
    public void setMerchant(String merchant) { this.merchant = merchant; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
