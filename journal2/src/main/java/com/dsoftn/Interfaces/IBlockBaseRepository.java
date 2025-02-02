package com.dsoftn.Interfaces;

import com.dsoftn.models.Block;



public interface IBlockBaseRepository<T> {

    public boolean isBaseExists(Integer baseBlockID);

    public boolean isBaseExists(Block baseBlock);

    public T getEntityFromBase(Integer baseBlockID);

    public T getEntityFromBase(Block baseBlock);

}
