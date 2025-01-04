package com.dsoftn.services;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.List;

import com.dsoftn.OBJECTS;
import com.dsoftn.utils.UString;
import com.dsoftn.utils.UError;


public class SQLiteDB {
    // Variables

    private Connection conn = null;

    // Constructor

    public SQLiteDB() {
        this.conn = connect(null);
    }

    // Methods

    public boolean constructAllTables() {
        List<String> sqlCommands = UString.splitAndStrip(OBJECTS.SETTINGS.getvSTRING("DatabaseTables"), ";");

        for (String sql : sqlCommands) {
            if (!createTable(sql)) {
                return false;
            }
        }

        return true;
    }

    public boolean isConnected() {
        return conn != null;
    }
    
    public boolean createTable(String sql) {
        sql = fixSql(sql);

        try {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(sql);
            return true;
        } catch (SQLException e) {
            error("SQLiteDB.createTable: Failed to create table", e);
            return false;
        }
    }

    public boolean update(PreparedStatement stmt) {
        try {
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            error("SQLiteDB.update: Failed to update data", e);
            return false;
        }
    }

    public boolean insert(PreparedStatement stmt) {
        return update(stmt);
    }

    public boolean delete(PreparedStatement stmt) {
        return update(stmt);
    }

    public ResultSet select(PreparedStatement stmt) {
        try {
            return stmt.executeQuery();
        } catch (SQLException e) {
            error("SQLiteDB.select: Failed to select data", e);
            return null;
        }
    }
    
    public Connection connect(String dbPath) {
        if (dbPath == null || dbPath.isEmpty()) dbPath = OBJECTS.ACTIVE_USER.getDbPath();
        String dbUrl = "jdbc:sqlite:" + dbPath;

        try {
            if (conn != null && !conn.isClosed()) conn.close();

            Connection conn = DriverManager.getConnection(dbUrl);
            return conn;
        } catch (SQLException e) {
            error("SQLiteDB.connect: Failed to connect to database", e);
            return null;
        }
    }

    public void disconnect() {
        if (conn == null) return;

        try {
            conn.close();
        } catch (SQLException e) {
            error("SQLiteDB.disconnect: Failed to disconnect from database", e);
        }
    }

    public PreparedStatement preparedStatement(String sql, Object... params) {
        // Check if connection is open
        try {
            if (conn == null || conn.isClosed()) {
                throw new SQLException("Connection is not open.");
            }
        } catch (SQLException e) {
            error("SQLiteDB.prepareStatement: Connection is not open.", null);
            return null;
        }

        sql = fixSql(sql);

        // Prepare statement
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);

            for (int i = 0; i < params.length; i++) {
                Object param = params[i];

                if (param instanceof Integer) {
                    stmt.setInt(i + 1, (Integer) param);
                } else if (param instanceof String) {
                    stmt.setString(i + 1, (String) param);
                } else if (param instanceof Double) {
                    stmt.setDouble(i + 1, (Double) param);
                } else if (param instanceof Boolean) {
                    stmt.setBoolean(i + 1, (Boolean) param);
                } else if (param == null) {
                    stmt.setNull(i + 1, java.sql.Types.NULL);
                } else {
                    stmt.setObject(i + 1, param);
                }
            }

            return stmt;
        } catch (SQLException e) {
            error("SQLiteDB.prepareStatement: Failed to prepare statement", e);
            return null;
        }
    }

    private String fixSql(String sql) {
        if (!sql.endsWith(";")) sql += ";";
        return sql;
    }

    private void error(String message, SQLException e) {
        UError.exception(message, e);
    }


}
