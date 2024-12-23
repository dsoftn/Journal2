package com.dsoftn.models;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.dsoftn.Interfaces.IModelRepository;

public class Users implements IModelRepository<User> {

    // Variables
    private Map<String, User> data = new LinkedHashMap<>();

    // IModelRepository methods

    @Override
    public boolean load() {
        // TODO Load data
        return true;
    }

    @Override
    public int count() {
        return data.size();
    }

    @Override
    public boolean isExists(String entityID) {
        return data.containsKey(entityID);
    }

    @Override
    public User getEntity(String entityID) {
        return data.get(entityID);
    }





}

