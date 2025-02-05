package com.dsoftn.models;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.dsoftn.Interfaces.IModelEntity;
import com.dsoftn.services.SQLiteDB;
import com.dsoftn.utils.UError;

import com.dsoftn.CONSTANTS;
import com.dsoftn.OBJECTS;


public class DefVariant implements IModelEntity<DefVariant> {
    private int id = CONSTANTS.INVALID_ID;
    private String text = "";
    private int definitionID = CONSTANTS.INVALID_ID;
    private int matchCase = OBJECTS.SETTINGS.getvINTEGER("DefVariantDefaultMatchCase");

    // Constructors
    
    public DefVariant() {}

    // Interface IModelEntity methods

    @Override
    public boolean load(Integer id) {
        SQLiteDB db = OBJECTS.DATABASE;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = db.preparedStatement("SELECT * FROM definitions_variants WHERE id = ?", id);
            if (stmt == null) {
                UError.error("DefVariant.load: Failed to load DefVariant", "Statement is unexpectedly null");
                return false;
            }
            
            rs = db.select(stmt);
            if (rs == null) {
                UError.error("DefVariant.load: Failed to load DefVariant", "Result set is unexpectedly null");
                return false;
            }

            if (rs.next()) {
                return loadFromResultSet(rs);
            }
        } catch (Exception e) {
            UError.exception("DefVariant.load: Failed to load DefVariant", e);
            return false;
        }
        finally {
            if (rs != null) try { rs.close(); } catch (Exception e) { e.printStackTrace(); }
            if (stmt != null) try { stmt.close(); } catch (Exception e) { e.printStackTrace(); }
            db.taskCompleted();
        }
        return false;
    }

    public boolean loadFromResultSet(ResultSet rs) {
        try {
            this.id = rs.getInt("id");
            this.text = rs.getString("text");
            this.definitionID = rs.getInt("definition_id");
            this.matchCase = rs.getInt("match_case");

            return isValid();
        } catch (Exception e) {
            UError.exception("DefVariant.loadFromResultSet: Failed to load DefVariant from result set", e);
            return false;
        }
    }

    @Override
    public boolean isValid() {
        return this.text != null;
    }

    @Override
    public Integer getID() { return id; }

    @Override
    public boolean add() {
        // Check if DefVariant can be added
        if (!canBeAdded()) {
            return false;
        }

        SQLiteDB db = OBJECTS.DATABASE;
        PreparedStatement stmt = null;
        try {
            // Add to database
            stmt = db.preparedStatement(
                "INSERT INTO definitions_variants " + 
                "(text, definition_id, match_case) " + 
                "VALUES (?, ?, ?)",
                this.text,
                this.definitionID,
                this.matchCase);

            if (stmt == null) {
                UError.error("DefVariant.add: Failed to add DefVariant", "Statement is unexpectedly null");
                return false;
            }
            Integer result = db.insert(stmt);
            if (result == null) {
                UError.error("DefVariant.add: Failed to write DefVariant to database", "Adding DefVariant failed");
                return false;
            }

            this.id = result;
            
            // Add to repository
            if (!OBJECTS.DEFINITIONS_VARIANTS.add(this)) {
                UError.error("DefVariant.add: Failed to add DefVariant to repository", "Adding DefVariant to repository failed");
                return false;
            }

            return true;

        } catch (Exception e) {
            UError.exception("DefVariant.add: Failed to add DefVariant", e);
            return false;
        }
        finally {
            if (stmt != null) try { stmt.close(); } catch (Exception e) { e.printStackTrace(); }
            db.taskCompleted();
        }
    }

    @Override
    public boolean canBeAdded() {
        if (OBJECTS.DEFINITIONS_VARIANTS.isExists(this.id)) return false;
        return true;
    }

    @Override
    public boolean update() {
        // Not implemented for DefVariant
        return false;
    }

    @Override
    public boolean canBeUpdated() {
        // Not implemented for DefVariant
        return false;
    }

    @Override
    public boolean delete() {
        // Check if DefVariant can be deleted
        if (!canBeDeleted()) {
            return false;
        }

        SQLiteDB db = OBJECTS.DATABASE;
        PreparedStatement stmt = null;
        try {
            // Delete from database
            stmt = db.preparedStatement(
                "DELETE FROM definitions_variants " + 
                "WHERE id = ?",
                this.id);

            if (stmt == null) {
                UError.error("DefVariant.delete: Failed to delete DefVariant", "Statement is unexpectedly null");
                return false;
            }
            if (!db.delete(stmt)) {
                UError.error("DefVariant.delete: Failed to delete DefVariant from database", "Deleting DefVariant failed");
                return false;
            }

            // Delete from repository
            if (!OBJECTS.DEFINITIONS_VARIANTS.delete(this)) {
                UError.error("DefVariant.delete: Failed to delete DefVariant from repository", "Deleting DefVariant from repository failed");
                return false;
            }

            return true;

        } catch (Exception e) {
            UError.exception("DefVariant.delete: Failed to delete DefVariant", e);
            return false;
        }
        finally {
            if (stmt != null) try { stmt.close(); } catch (Exception e) { e.printStackTrace(); }
            db.taskCompleted();
        }
    }

    @Override
    public boolean canBeDeleted() {
        if (this.id == CONSTANTS.INVALID_ID) return false;
        if (!OBJECTS.DEFINITIONS_VARIANTS.isExists(this.id)) return false;
        return true;
    }

    @Override
    public DefVariant duplicate() {
        DefVariant newDefVariant = new DefVariant();
        newDefVariant.id = this.id;
        newDefVariant.text = this.text;
        newDefVariant.definitionID = this.definitionID;
        newDefVariant.matchCase = this.matchCase;
        
        return newDefVariant;
    }
    

    // Getters
   
    public String getText() { return text; }
    
    public int getDefinitionID() { return definitionID; }

    public int getMatchCaseInt() { return matchCase; }
    
    public boolean getMatchCase() { return matchCase == 1; }

    // Setters

    public void setID(int id) { this.id = id; }
    
    public void setText(String text) { this.text = text; }
    
    public void setDefinitionID(int definitionID) { this.definitionID = definitionID; }
    
    public void setMatchCase(boolean matchCase) { this.matchCase = matchCase ? 1 : 0; }

    public void setMatchCase(int matchCase) { this.matchCase = matchCase; }

}
