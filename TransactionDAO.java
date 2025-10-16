package backend.dao;

import backend.db.Db;
import backend.model.Transaction;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class TransactionDAO {
    public void insert(Transaction t) {
        String sql = "INSERT INTO transactions(type, amount, occurred_at, category_id, merchant, note) VALUES(?,?,?,?,?,?)";
        try (Connection con = Db.get(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, t.getType());
            ps.setDouble(2, t.getAmount());
            ps.setString(3, t.getOccurredAt().toString());
            if (t.getCategoryId() == null) ps.setNull(4, Types.INTEGER); else ps.setInt(4, t.getCategoryId());
            ps.setString(5, t.getMerchant());
            ps.setString(6, t.getNote());
            ps.executeUpdate();
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public List<Transaction> listByMonth(String yyyyMM) {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT id, type, amount, occurred_at, category_id, merchant, note " +
                     "FROM transactions WHERE substr(occurred_at,1,7)=? ORDER BY occurred_at DESC, id DESC";
        try (Connection con = Db.get(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, yyyyMM);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Transaction t = new Transaction();
                    t.setId(rs.getInt(1));
                    t.setType(rs.getString(2));
                    t.setAmount(rs.getDouble(3));
                    t.setOccurredAt(LocalDate.parse(rs.getString(4)));
                    int cid = rs.getInt(5);
                    t.setCategoryId(rs.wasNull() ? null : cid);
                    t.setMerchant(rs.getString(6));
                    t.setNote(rs.getString(7));
                    list.add(t);
                }
            }
        } catch (Exception e) { throw new RuntimeException(e); }
        return list;
    }

    public Map<String, Double> monthTotals() {
        Map<String, Double> res = new LinkedHashMap<>();
        String sql = "SELECT substr(occurred_at,1,7) AS m, SUM(CASE WHEN type='expense' THEN amount ELSE 0 END) AS expenses " +
                     "FROM transactions GROUP BY m ORDER BY m";
        try (Connection con = Db.get(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) res.put(rs.getString(1), rs.getDouble(2));
        } catch (Exception e) { throw new RuntimeException(e); }
        return res;
    }

    public Map<String, Double> monthTotalsIncome() {
        Map<String, Double> res = new LinkedHashMap<>();
        String sql = "SELECT substr(occurred_at,1,7) AS m, SUM(CASE WHEN type='income' THEN amount ELSE 0 END) AS income " +
                     "FROM transactions GROUP BY m ORDER BY m";
        try (Connection con = Db.get(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) res.put(rs.getString(1), rs.getDouble(2));
        } catch (Exception e) { throw new RuntimeException(e); }
        return res;
    }

    public Map<Integer, Double> categoryTotalsForMonth(String yyyyMM) {
        Map<Integer, Double> res = new LinkedHashMap<>();
        String sql = "SELECT category_id, SUM(amount) FROM transactions " +
                     "WHERE type='expense' AND substr(occurred_at,1,7)=? AND category_id IS NOT NULL " +
                     "GROUP BY category_id ORDER BY SUM(amount) DESC";
        try (Connection con = Db.get(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, yyyyMM);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) res.put(rs.getInt(1), rs.getDouble(2));
            }
        } catch (Exception e) { throw new RuntimeException(e); }
        return res;
    }

    public double[] lastNMonthsTotalsForCategory(int categoryId, int n) {
        String sql = "SELECT substr(occurred_at,1,7) AS m, SUM(amount) total " +
                     "FROM transactions WHERE type='expense' AND category_id=? " +
                     "GROUP BY m ORDER BY m DESC LIMIT ?";
        List<Double> values = new ArrayList<>();
        try (Connection con = Db.get(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            ps.setInt(2, n);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) values.add(rs.getDouble(2));
            }
        } catch (Exception e) { throw new RuntimeException(e); }
        Collections.reverse(values);
        return values.stream().mapToDouble(Double::doubleValue).toArray();
    }

    public double monthSum(String yyyyMM, String type) {
        String sql = "SELECT COALESCE(SUM(amount),0) FROM transactions WHERE type=? AND substr(occurred_at,1,7)=?";
        try (Connection con = Db.get(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, type);
            ps.setString(2, yyyyMM);
            try (ResultSet rs = ps.executeQuery()) { return rs.getDouble(1); }
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    // Labeled expenses for training (merchant + note)
    public List<TrainingRow> labeledExpenses() {
        String sql = "SELECT category_id, COALESCE(merchant,'') || ' ' || COALESCE(note,'') AS text " +
                     "FROM transactions WHERE type='expense' AND category_id IS NOT NULL";
        List<TrainingRow> out = new ArrayList<>();
        try (Connection con = Db.get(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                TrainingRow r = new TrainingRow();
                r.categoryId = rs.getInt(1);
                r.text = rs.getString(2);
                out.add(r);
            }
        } catch (Exception e) { throw new RuntimeException(e); }
        return out;
    }

    public static class TrainingRow {
        public int categoryId;
        public String text;
    }
}
