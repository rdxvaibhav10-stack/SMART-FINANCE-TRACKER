package backend.dao;

import backend.db.Db;
import backend.model.Rule;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RuleDAO {
    public List<Rule> findAll() {
        List<Rule> list = new ArrayList<>();
        String sql = "SELECT id, keyword, category_id FROM rules ORDER BY keyword";
        try (Connection con = Db.get(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(new Rule(rs.getInt(1), rs.getString(2), rs.getInt(3)));
        } catch (Exception e) { throw new RuntimeException(e); }
        return list;
    }

    public int insert(String keyword, int categoryId) {
        String sql = "INSERT INTO rules(keyword, category_id) VALUES(?,?)";
        try (Connection con = Db.get(); PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, keyword.toLowerCase().trim());
            ps.setInt(2, categoryId);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) return rs.getInt(1); }
        } catch (Exception e) { throw new RuntimeException(e); }
        return -1;
    }

    public void delete(int id) {
        try (Connection con = Db.get(); PreparedStatement ps = con.prepareStatement("DELETE FROM rules WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
