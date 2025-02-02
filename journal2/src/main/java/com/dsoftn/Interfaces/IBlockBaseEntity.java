package com.dsoftn.Interfaces;

import com.dsoftn.enums.models.BlockTypeEnum;
import com.dsoftn.models.Block;


public interface IBlockBaseEntity {

    public boolean saveBlockAndBase();

    public boolean deleteBlockAndBase();

    public BlockTypeEnum getBlockType();

    public Integer getBaseBlockID();

    public Block getBaseBlock();

    public void setBaseBlock(Block baseBlock);

    public String getTextForBaseBlock();

    public String getTextStyleForBaseBlock();

}
