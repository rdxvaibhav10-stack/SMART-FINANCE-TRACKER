package backend.dao;

import backend.db.Db;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class BudgetDAO {
    public void upsertMonthlyBudget(int categoryId, String monthYYYYMM, double amount) {
        String start = monthYYYYMM + "-01";
        String sql = "INSERT INTO budgets(category_id, period, amount, start_date) VALUES(?, 'monthly', ?, ?) " +
                     "ON CONFLICT(category_id, period, start_date) DO UPDATE SET amount=excluded.amount";
        try (Connection con = Db.get(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            ps.setDouble(2, amount);
            ps.setString(3, start);
            ps.executeUpdate();
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public Map<Integer, Double> monthlyBudgets(String monthYYYYMM) {
        Map<Integer, Double> res = new LinkedHashMap<>();
        String start = monthYYYYMM + "-01";
        String sql = "SELECT category_id, amount FROM budgets WHERE period='monthly' AND start_date=?";
        try (Connection con = Db.get(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, start);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) res.put(rs.getInt(1), rs.getDouble(2));
            }
        } catch (Exception e) { throw new RuntimeException(e); }
        return res;
    }
}
