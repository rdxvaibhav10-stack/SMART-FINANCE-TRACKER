package backend.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Db {
    private static final String URL = "jdbc:sqlite:finance.db";
    static {
        try { Class.forName("org.sqlite.JDBC"); } catch (Exception ignored) {}
    }
    public static Connection get() throws SQLException {
        Connection c = DriverManager.getConnection(URL);
        c.setAutoCommit(true);
        return c;
    }
}
