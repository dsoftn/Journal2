package com.dsoftn.models;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javafx.application.Platform;

import com.dsoftn.OBJECTS;
import com.dsoftn.Interfaces.IModelRepository;
import com.dsoftn.enums.models.ScopeEnum;
import com.dsoftn.enums.models.TaskStateEnum;
import com.dsoftn.events.TaskStateEvent;
import com.dsoftn.services.SQLiteDB;
import com.dsoftn.utils.UError;
import com.dsoftn.utils.UNumbers;
import com.dsoftn.utils.UString;

/*
 * TABLE definitions_variants
    id INTEGER PRIMARY KEY AUTOINCREMENT
    text TEXT NOT NULL - text of the variant
    definition_id INTEGER NOT NULL - id of the definition
    match_case INTEGER NOT NULL - 0 or 1
 */
public class DefVariants implements IModelRepository<DefVariant> {
    // Variables

    private Map<Integer, DefVariant> data = new LinkedHashMap<>(); // ID: variant
    private Map<Integer, List<DefVariant>> dataByDef = new LinkedHashMap<>(); // definitionID: list of variants
    private boolean isLoaded = false;

    // Constructor

    public DefVariants() {}

    // Interface IModelRepository<DefVariant> methods

    @Override
    public boolean load() {
        // Send start event
        Platform.runLater(() -> {
            OBJECTS.EVENT_HANDLER.fireEvent(
                new TaskStateEvent(
                    ScopeEnum.DEF_VARIANT,
                    TaskStateEnum.STARTED
                )
            );
        });

        boolean result = true;

        SQLiteDB db = OBJECTS.DATABASE;
        if (db.isConnected() == false) { loadFailed(); return false; }

        // Find number of rows
        Integer rowCount = db.getRowCount("definitions_variants");
        if (rowCount == null) {
            UError.error("DefVariants.load: Failed to load DefVariants", "Failed to get row count");
            loadFailed();
            return false;
        }

        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            stmt = db.preparedStatement("SELECT * FROM definitions_variants");
            if (stmt == null) {
                UError.error("DefVariants.load: Failed to load DefVariants", "Statement is unexpectedly null");
                loadFailed();
                return false;
            }
            rs = db.select(stmt);
            if (rs == null) {
                UError.error("DefVariants.load: Failed to load DefVariants", "Result set is unexpectedly null");
                loadFailed();
                return false;
            }

            int currentRow = 1;

            while (rs.next()) {
                DefVariant def_variant = new DefVariant();
                result = def_variant.loadFromResultSet(rs);
                if (result == false) {
                    UError.error("DefVariants.load: Failed to load DefVariant", "Loading DefVariant failed");
                    result = false;
                    continue;
                }
                
                // Add DefVariant
                add(def_variant);

                // Send progress event
                Integer progressPercent = UNumbers.getPercentIfHasNoRemainder(rowCount, currentRow);
                if (progressPercent != null) {
                    Platform.runLater(() -> {
                        OBJECTS.EVENT_HANDLER.fireEvent(
                            new TaskStateEvent(ScopeEnum.DEF_VARIANT, TaskStateEnum.EXECUTING, progressPercent)
                        );
                    });
                }
                currentRow++;
            }

        } catch (Exception e) {
            UError.exception("DefVariants.load: Failed to load DefVariants", e);
            loadFailed();
            return false;
        
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                    UError.exception("DefVariants.load: Failed to close result set", e);
                    loadFailed();
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Exception e) {
                    UError.exception("DefVariants.load: Failed to close statement", e);
                    loadFailed();
                }
            }
            db.taskCompleted();
        }

        // Rebuild dataByDef Map from data Map   - NOT NEEDED ???
        // dataByDef.clear();
        // for (DefVariant definition_variant : data.values()) {
        //     if (!dataByDef.containsKey(definition_variant.getDefinitionID())) {
        //         dataByDef.put(definition_variant.getDefinitionID(), new ArrayList<>());
        //     }
        //     dataByDef.get(definition_variant.getDefinitionID()).add(definition_variant);
        // }

        if (result == false) {
            loadFailed();
        }
        else {
            Platform.runLater(() -> { 
                OBJECTS.EVENT_HANDLER.fireEvent(new TaskStateEvent(ScopeEnum.DEF_VARIANT, TaskStateEnum.COMPLETED));
            });
            isLoaded = true;
        }

        return result;
    }

    private void loadFailed() {
        Platform.runLater(() -> {
            OBJECTS.EVENT_HANDLER.fireEvent(
                new TaskStateEvent(
                    ScopeEnum.DEF_VARIANT,
                    TaskStateEnum.FAILED
                )
            );
        });
    }

    @Override
    public int count() {
        return data.size();
    }

    @Override
    public boolean isExists(Integer entityID) {
        return data.containsKey(entityID);
    }

    @Override
    public DefVariant getEntity(Integer entityID) {
        return data.get(entityID);
    }

    @Override
    public List<DefVariant> getEntityAll() {
        List<DefVariant> list = new ArrayList<>(data.values());
        return list;
    }

    @Override
    public boolean add(DefVariant entity) {
        if (entity == null) return false;

        if (isExists(entity.getID())) return false;

        // Add DefVariant to data and dataByDef
        if (!dataByDef.containsKey(entity.getDefinitionID())) {
            dataByDef.put(entity.getDefinitionID(), new ArrayList<>());
        }

        data.put(entity.getID(), entity);
        dataByDef.get(entity.getDefinitionID()).add(entity);
        
        return true;
    }

    @Override
    public boolean update(DefVariant entity) {
        // Not implemented for DefVariant
        return false;
    }

    @Override
    public boolean delete(DefVariant entity) {
        if (entity == null) return false;

        if (!isExists(entity.getID())) return false;

        // Remove from data and dataByDef
        for (DefVariant defVariant : dataByDef.get(entity.getDefinitionID())) {
            if (defVariant.getID() == entity.getID()) {
                dataByDef.get(entity.getDefinitionID()).remove(defVariant);
                break;
            }
        }

        data.remove(entity.getID());
        
        return true;
    }

    @Override
    public boolean isModelLoaded() {
        return isLoaded;
    }

    // Public methods

    public List<String> getVariantsWordList(Integer definitionID) {
        List<String> result = new ArrayList<>(); // List of text of variants

        if (!dataByDef.containsKey(definitionID)) return result;

        result = dataByDef.get(definitionID).stream().map(DefVariant::getText).collect(Collectors.toList());

        return result;
    }

    public String getVariantsString(Integer definitionID) {
        return String.join("\n", getVariantsWordList(definitionID));
    }

    public boolean updateVariantsDefinitionAdd(Definition definition) {
        List<String> newWords = UString.splitAndStrip(definition.getVariants(), "\n");
        List<DefVariant> newVariants = new ArrayList<>();

        for (String word : newWords) {
            DefVariant defVariant = new DefVariant();
            defVariant.setDefinitionID(definition.getID());
            defVariant.setText(word);

            newVariants.add(defVariant);
        }

        if (!addVariants(newVariants)) {
            UError.error("DefVariants.updateVariantsDefinitionAdd: Failed to add variants", "Adding variants failed");
            return false;
        }

        return true;
    }

    public boolean updateVariantsDefinitionUpdate(Definition definition) {
        // Delete old variants
        if (!deleteAllVariants(definition)) {
            UError.error("DefVariants.updateVariantsDefinitionUpdate: Failed to update variants", "Deleting old variants failed");
            return false;
        }

        // Add new variants
        return updateVariantsDefinitionAdd(definition);
    }

    public boolean updateVariantsDefinitionDelete(Definition definition) {
        if (!dataByDef.containsKey(definition.getID())) return true;

        if (!deleteAllVariants(definition)) {
            UError.error("DefVariants.updateVariantsDefinitionDelete: Failed to delete variants", "Deleting variants failed");
            return false;
        }

        return true;
    }

    // Private methods

    private boolean deleteAllVariants(Definition definition) {
        if (!dataByDef.containsKey(definition.getID())) {
            return true;
        }

        SQLiteDB db = OBJECTS.DATABASE;
        PreparedStatement stmt = null;
        try {
            // Delete from database
            stmt = db.preparedStatement(
                "DELETE FROM definitions_variants " + 
                "WHERE definition_id = ?",
                definition.getID());

            if (stmt == null) {
                UError.error("DefVariant.deleteVariants: Failed to delete DefVariant", "Statement is unexpectedly null");
                return false;
            }
            if (!db.delete(stmt)) {
                UError.error("DefVariant.deleteVariants: Failed to delete DefVariant from database", "Deleting DefVariant failed");
                return false;
            }

        } catch (Exception e) {
            UError.exception("DefVariant.deleteVariants: Failed to delete DefVariant", e);
            return false;
        }
        finally {
            if (stmt != null) try { stmt.close(); } catch (Exception e) {}
            db.taskCompleted();
        }

        // Remove from data and dataByDef
        for (DefVariant defVariant : dataByDef.get(definition.getID())) {
            data.remove(defVariant.getID());
        }

        dataByDef.remove(definition.getID());

        return true;
    }

    private boolean addVariants(List<DefVariant> variants) {
        SQLiteDB db = OBJECTS.DATABASE;

        List<Integer> generatedKeys = db.insertMany(variants);

        db.taskCompleted();

        if (generatedKeys == null) {
            UError.error("DefVariant.addVariants: Failed to add DefVariant", "Database did not return generated keys");
            return false;
        }

        for (int i = 0; i < variants.size(); i++) {
            variants.get(i).setID(generatedKeys.get(i));
            if (!OBJECTS.DEFINITIONS_VARIANTS.add(variants.get(i))) {
                UError.error("DefVariant.addVariants: Failed to add DefVariant", "Adding DefVariant to repository failed");
                return false;
            }
        }

        return true;
    }

}
