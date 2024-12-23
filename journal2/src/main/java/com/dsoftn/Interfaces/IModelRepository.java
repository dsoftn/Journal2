package com.dsoftn.Interfaces;

public interface IModelRepository<T> {

    public boolean load();

    public int count();

    public boolean isExists(String entityID);
    
    public T getEntity(String entityID);

}
