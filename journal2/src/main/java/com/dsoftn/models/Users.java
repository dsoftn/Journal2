package com.dsoftn.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.dsoftn.CONSTANTS;
import com.dsoftn.Interfaces.IModelRepository;
import com.dsoftn.utils.UFile;

public class Users implements IModelRepository<User> {

    // Variables
    private Map<String, User> data = new LinkedHashMap<>();
    private boolean isLoaded = false;

    // IModelRepository methods

    /**
     * Load all users from data/users folder
     */
    @Override
    public boolean load() {
        List<String> folders = UFile.getFolderContent(CONSTANTS.FOLDER_DATA_USERS);
        if (folders == null) return false;

        data.clear();

        for (String folder : folders) {
            if (UFile.isDirectory(folder)) {
                User user = new User();
                if (user.load(folder)) {
                    data.put(user.getUsername(), user);
                }
            }
        }

        isLoaded = true;
        return true;
    }

    @Override
    public int count() {
        return data.size();
    }

    @Override
    public boolean isExists(Integer userName) {
        return false;
    }

    public boolean isExists(String userName) {
        return data.containsKey(userName);
    }

    @Override
    public User getEntity(Integer userName) {
        return null;
    }

    public User getEntity(String userName) {
        return data.get(userName);
    }

    @Override
    public List<User> getEntityAll() {
        List<User> list = new ArrayList<>(data.values());
        return list;
    }

    @Override
    public boolean update(User user) {
        if (user == null || user.getUsername() == null || user.getUsername().isEmpty()) return false;
        if (!data.containsKey(user.getUsername())) return false;
        data.put(user.getUsername(), user);
        return true;
    }

    @Override
    public boolean add(User user) {
        if (user == null || user.getUsername() == null || user.getUsername().isEmpty()) return false;
        if (data.containsKey(user.getUsername())) return false;
        data.put(user.getUsername(), user);
        return true;
    }

    @Override
    public boolean delete(User user) {
        if (user == null || user.getUsername() == null || user.getUsername().isEmpty()) return false;
        if (!data.containsKey(user.getUsername())) return false;
        data.remove(user.getUsername());
        return true;
    }

    @Override
    public boolean isModelLoaded() {
        return isLoaded;
    }
    
    // Public methods

    public User lastActiveUser() {
        if (data.isEmpty()) return null;

        LocalDateTime last = data.values().stream().map(User::getLastSessionStart).max(LocalDateTime::compareTo).get();

        for (User user : data.values()) {
            if (user.getLastSessionStart().equals(last)) return user;
        }

        return null;
    }



}

