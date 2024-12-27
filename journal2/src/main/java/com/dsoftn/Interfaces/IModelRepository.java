package com.dsoftn.Interfaces;

import java.util.List;

public interface IModelRepository<T> {

    public boolean load();

    public int count();

    public boolean isExists(String entityID);
    
    public T getEntity(String entityID);

    public List<T> getEntityAll();

    public boolean update(T entity);

    public boolean add(T entity);

    public boolean delete(T entity);

}
