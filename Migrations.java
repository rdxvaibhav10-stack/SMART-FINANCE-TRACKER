package backend.db;

import java.sql.Connection;
import java.sql.Statement;

public final class Migrations {
    public static void init() {
        try (Connection con = Db.get(); Statement st = con.createStatement()) {
            st.executeUpdate("PRAGMA foreign_keys = ON;");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS categories (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE NOT NULL)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS transactions (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "type TEXT NOT NULL," +
                    "amount REAL NOT NULL," +
                    "occurred_at TEXT NOT NULL," +
                    "category_id INTEGER," +
                    "merchant TEXT," +
                    "note TEXT," +
                    "FOREIGN KEY (category_id) REFERENCES categories(id))");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS budgets (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "category_id INTEGER NOT NULL," +
                    "period TEXT NOT NULL," +
                    "amount REAL NOT NULL," +
                    "start_date TEXT NOT NULL," +
                    "UNIQUE(category_id, period, start_date)," +
                    "FOREIGN KEY (category_id) REFERENCES categories(id))");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS rules (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "keyword TEXT NOT NULL," +
                    "category_id INTEGER NOT NULL," +
                    "FOREIGN KEY (category_id) REFERENCES categories(id))");
            st.executeUpdate("CREATE INDEX IF NOT EXISTS idx_rules_keyword ON rules(keyword)");
        } catch (Exception e) {
            throw new RuntimeException("DB init failed", e);
        }
    }
}
