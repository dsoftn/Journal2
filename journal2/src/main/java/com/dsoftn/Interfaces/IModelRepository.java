package com.dsoftn.Interfaces;

import java.util.List;

public interface IModelRepository<T> {

    public boolean load();

    public int count();

    public boolean isExists(Integer entityID);
    
    public T getEntity(Integer entityID);

    public List<T> getEntityAll();

    public boolean update(T entity);

    public boolean add(T entity);

    public boolean delete(T entity);

}
