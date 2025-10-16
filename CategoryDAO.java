package backend.dao;

import backend.db.Db;
import backend.model.Category;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {

    public void ensureDefaults() {
        String[] names = {"Income","Food","Transport","Utilities","Rent","Entertainment","Shopping","Health","Education","Travel","Groceries","Other"};
        for (String n : names) if (findByName(n) == null) insert(n);
    }

    public int insert(String name) {
        try (Connection con = Db.get();
             PreparedStatement ps = con.prepareStatement("INSERT INTO categories(name) VALUES(?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) return rs.getInt(1); }
        } catch (Exception e) { throw new RuntimeException(e); }
        return -1;
    }

    public Category findByName(String name) {
        try (Connection con = Db.get();
             PreparedStatement ps = con.prepareStatement("SELECT id,name FROM categories WHERE LOWER(name)=LOWER(?)")) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return new Category(rs.getInt(1), rs.getString(2));
            }
        } catch (Exception e) { throw new RuntimeException(e); }
        return null;
    }

    public Category findById(int id) {
        try (Connection con = Db.get();
             PreparedStatement ps = con.prepareStatement("SELECT id,name FROM categories WHERE id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return new Category(rs.getInt(1), rs.getString(2)); }
        } catch (Exception e) { throw new RuntimeException(e); }
        return null;
    }

    public List<Category> findAll() {
        List<Category> list = new ArrayList<>();
        try (Connection con = Db.get();
             PreparedStatement ps = con.prepareStatement("SELECT id,name FROM categories ORDER BY name");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(new Category(rs.getInt(1), rs.getString(2)));
        } catch (Exception e) { throw new RuntimeException(e); }
        return list;
    }
}
