package backend.model;

public class Rule {
    private int id;
    private String keyword;
    private int categoryId;

    public Rule() {}
    public Rule(int id, String keyword, int categoryId) {
        this.id = id; this.keyword = keyword; this.categoryId = categoryId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
}
