package com.dsoftn.services;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

import java.util.List;

import com.dsoftn.OBJECTS;
import com.dsoftn.utils.UString;


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
        try {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(sql);
            return true;
        } catch (SQLException e) {
            error("SQLiteDB.createTable: Failed to create table", e);
            return false;
        }
    }

    public boolean insert(String sql) {
        try {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(sql);
            return true;
        } catch (SQLException e) {
            error("SQLiteDB.insert: Failed to insert data", e);
            return false;
        }
    }

    public boolean update(String sql) {
        try {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(sql);
            return true;
        } catch (SQLException e) {
            error("SQLiteDB.update: Failed to update data", e);
            return false;
        }
    }

    public boolean delete(String sql) {
        try {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(sql);
            return true;
        } catch (SQLException e) {
            error("SQLiteDB.delete: Failed to delete data", e);
            return false;
        }
    }

    public ResultSet select(String sql) {
        try {
            Statement stmt = conn.createStatement();
            return stmt.executeQuery(sql);
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

    public void disconnect(Connection conn) {
        try {
            conn.close();
        } catch (SQLException e) {
            error("SQLiteDB.disconnect: Failed to disconnect from database", e);
        }
    }

    private void error(String message, SQLException e) {
        System.out.println(message + ": " + e.getMessage() +  "\n\n");
        e.printStackTrace();
    }


}
