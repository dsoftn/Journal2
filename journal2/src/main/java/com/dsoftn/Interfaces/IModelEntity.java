package com.dsoftn.Interfaces;

public interface IModelEntity {

    public String getID();

    public boolean load(String id);

    public boolean save();
    public boolean canBeSaved();

    public boolean isExists(String id);

    public boolean add();
    public boolean canBeAdded();

    public boolean update();
    public boolean canBeUpdated();

    public boolean delete();
    public boolean canBeDeleted();



    

}
