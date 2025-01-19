package com.dsoftn.services;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.List;
import java.util.ArrayList;

import com.dsoftn.OBJECTS;
import com.dsoftn.utils.UString;
import com.dsoftn.utils.UError;
import com.dsoftn.models.DefVariant;


public class SQLiteDB {
    // Variables

    private Connection conn = null;

    // Constructor

    public SQLiteDB() {
        this.conn = connect(null);
    }

    public SQLiteDB(String dbPath) {
        this.conn = connect(dbPath);
    }

    // Methods

    public boolean constructAllTables() {
        List<String> sqlCommands = UString.splitAndStrip(OBJECTS.SETTINGS.getvSTRING("DatabaseTables"), ";");

        for (String sql : sqlCommands) {
            if (sql.isEmpty()) {
                continue;
            }
            
            if (!createTable(sql)) {
                return false;
            }
        }

        return true;
    }

    public boolean isConnected() {
        // Check if connection is open
        try {
            if (conn == null || conn.isClosed()) {
                return false;
            }
        } catch (SQLException e) {
            return false;
        }

        return true;
    }
    
    public boolean createTable(String sql) {
        // Check sql
        if (sql == null || sql.isEmpty()) {
            UError.error("SQLiteDB.createTable: Failed to create table", "SQL is null");
            return false;
        }
        
        // Check if connection is open
        if (checkConnection() == false) {
            UError.error("SQLiteDB.createTable: Failed to create table", "Database is not connected", "SQL: " + sql);
            return false;
        }

        sql = fixSql(sql);

        try {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(sql);
            return true;
        } catch (SQLException e) {
            UError.exception("SQLiteDB.createTable: Failed to create table", e, "SQL: " + sql);
            return false;
        }
    }

    public boolean update(PreparedStatement stmt) {
        // Check if stmt is null
        if (stmt == null) {
            UError.error("SQLiteDB.update: Failed to update data", "Statement is null");
            return false;
        }

        // Check if connection is open
        if (checkConnection() == false) {
            UError.error("SQLiteDB.update: Failed to update data", "Database is not connected", "SQL: " + stmt.toString());
            return false;
        }

        try {
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            UError.exception("SQLiteDB.update: Failed to update data", e, "SQL: " + stmt.toString());
            return false;
        }
    }

    /**
     * Returns new id or null if failed
     */
    public Integer insert(PreparedStatement stmt) {
        // Check if stmt is null
        if (stmt == null) {
            UError.error("SQLiteDB.insert: Failed to insert data", "Statement is null");
            return null;
        }

        // Check if connection is open
        if (checkConnection() == false) {
            UError.error("SQLiteDB.insert: Failed to insert data", "Database is not connected", "SQL: " + stmt.toString());
            return null;
        }

        // Insert data
        try {
            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                UError.error("SQLiteDB.insert: Insert operation did not affect any rows", "Affected rows is 0", "SQL: " + stmt.toString());
                return null;
            }

            // Get generated id
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                else {
                    UError.error("SQLiteDB.insert: No generated ID returned", "Result set is unexpectedly null" ,"SQL: " + stmt.toString());
                    return null;
                }
            }
            
        } catch (SQLException e) {
            UError.exception("SQLiteDB.insert: Failed to insert data", e, "SQL: " + stmt.toString());
            return null;
        }
    }

    public List<Integer> insertMany(List<DefVariant> variants) {
        String sql = "INSERT INTO definitions_variants (text, definition_id, match_case) VALUES (?, ?, ?)";
        List<Integer> generatedIds = new ArrayList<>();

        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            conn.setAutoCommit(false);

            for (DefVariant variant : variants) {
                pstmt.setString(1, variant.getText());
                pstmt.setInt(2, variant.getDefinitionID());
                pstmt.setInt(3, variant.getMatchCaseInt());
                pstmt.executeUpdate();

                // Get generated id
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        generatedIds.add(generatedKeys.getInt(1));
                    }
                    else {
                        try {
                            conn.rollback();
                            UError.error("SQLiteDB.insertMany: No generated ID returned", "Result set is unexpectedly null" ,"SQL: " + sql);
                            return null;
                        } catch (SQLException ex) {
                            UError.exception("SQLiteDB.insertMany: No generated ID returned. Database rollback failed. Failed to insert data", ex, "SQL: " + sql);
                            return null;
                        }
                    }
                }
                catch (SQLException e) {
                    try {
                        conn.rollback();
                        UError.exception("SQLiteDB.insertMany: Failed to insert data", e, "SQL: " + sql);
                        return null;
                    } catch (SQLException ex) {
                        UError.exception("SQLiteDB.insertMany: Database rollback failed. Failed to insert data", ex, "SQL: " + sql);
                        return null;
                    }
                }
    
            }

            conn.commit();
            
        } catch (SQLException e) {
            try {
                conn.rollback();
                UError.exception("SQLiteDB.insertMany: Failed to insert data", e, "SQL: " + sql);
                return null;
            } catch (SQLException ex) {
                UError.exception("SQLiteDB.insertMany: Database rollback failed. Failed to insert data", ex, "SQL: " + sql);
                return null;
            }
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                UError.exception("SQLiteDB.insertMany: Failed to set auto commit to true", e, "SQL: " + sql);
            }
        }

        return generatedIds;
    }

    public boolean delete(PreparedStatement stmt) {
        // Check if stmt is null
        if (stmt == null) {
            UError.error("SQLiteDB.delete: Failed to delete data", "Statement is null");
            return false;
        }

        // Check if connection is open
        if (checkConnection() == false) {
            UError.error("SQLiteDB.delete: Failed to delete data", "Database is not connected", "SQL: " + stmt.toString());
            return false;
        }

        try {
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            UError.exception("SQLiteDB.delete: Failed to delete data", e, "SQL: " + stmt.toString());
            return false;
        }
    }

    public ResultSet select(PreparedStatement stmt) {
        // Check if stmt is null
        if (stmt == null) {
            UError.error("SQLiteDB.select: Failed to select data", "Statement is null");
            return null;
        }

        // Check if connection is open
        if (checkConnection() == false) {
            UError.error("SQLiteDB.select: Failed to select data", "Database is not connected", "SQL: " + stmt.toString());
            return null;
        }

        try {
            return stmt.executeQuery();
        } catch (SQLException e) {
            UError.exception("SQLiteDB.select: Failed to select data", e, "SQL: " + stmt.toString());
            return null;
        }
    }

    public Integer getRowCount(String tableName) {
        String sql = "SELECT COUNT(*) AS row_count FROM " + tableName;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("row_count");
            }
            else {
                UError.error("SQLiteDB.getRowCount: Failed to get row count", "Result set is unexpectedly null", "SQL: " + sql);
                return null;
            }
        } catch (SQLException e) {
            UError.exception("SQLiteDB.getRowCount: Failed to get row count", e, "SQL: " + sql);
            return null;
        }
    }

    public Connection connect(String dbPath) {
        if (dbPath == null || dbPath.isEmpty()) dbPath = OBJECTS.ACTIVE_USER.getDbPath();
        String dbUrl = "jdbc:sqlite:" + dbPath;

        try {
            if (conn != null && !conn.isClosed()) disconnect();

            conn = DriverManager.getConnection(dbUrl);
            return conn;
        } catch (SQLException e) {
            UError.exception("SQLiteDB.connect: Failed to connect to database", e, "Database path: " + dbPath);
            return null;
        }
    }

    public void disconnect() {
        if (conn == null) return;

        try {
            conn.close();
        } catch (SQLException e) {
            UError.exception("SQLiteDB.disconnect: Failed to disconnect from database", e);
        }
    }

    /**
     * If SQL command is INSERT INTO, returns PreparedStatement with RETURN_GENERATED_KEYS
     */
    public PreparedStatement preparedStatement(String sql, Object... params) {
        // Check if connection is open
        if (checkConnection() == false) {
            UError.error("SQLiteDB.prepareStatement: Failed to prepare statement", "Database is not connected", "SQL: " + sql);
            return null;
        }

        sql = fixSql(sql);

        // Check if SQl is INSERT INTO
        boolean returnGeneratedKeys = sql.trim().toUpperCase().startsWith("INSERT INTO");

        // Prepare statement
        try {
            PreparedStatement stmt = returnGeneratedKeys
            ? conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
            : conn.prepareStatement(sql);

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
            UError.exception("SQLiteDB.prepareStatement: Failed to prepare statement", e, "SQL: " + sql);
            return null;
        }
    }

    private String fixSql(String sql) {
        if (!sql.endsWith(";")) sql += ";";
        return sql;
    }

    private boolean checkConnection() {
        // Check if connection is open
        try {
            if (conn == null || conn.isClosed()) {
                UError.error("SQLiteDB.checkConnection: Failed", "Database is not connected");
                return false;
            }
        } catch (SQLException e) {
            UError.exception("SQLiteDB.checkConnection: Failed.", e, "Database is not connected");
            return false;
        }

        return true;
    }

}
