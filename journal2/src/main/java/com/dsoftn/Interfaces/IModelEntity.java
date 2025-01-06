package com.dsoftn.Interfaces;

public interface IModelEntity {

    public Integer getID();

    public boolean load(Integer id);

    public boolean add();
    public boolean canBeAdded();

    public boolean update();
    public boolean canBeUpdated();

    public boolean delete();
    public boolean canBeDeleted();



    

}
