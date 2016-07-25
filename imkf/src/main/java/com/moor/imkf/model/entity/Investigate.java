package com.moor.imkf.model.entity;

import com.moor.imkf.ormlite.field.DatabaseField;
import com.moor.imkf.ormlite.table.DatabaseTable;

/**
 * 评价实体类
 */
@DatabaseTable(tableName = "investigate")
public class Investigate {

    public Investigate() {}

    /**
     * 主键
     */
    @DatabaseField(generatedId = true)
    public int id;
    /**
     * 名称
     */
    @DatabaseField
    public String name;
    /**
     * 数值
     */
    @DatabaseField
    public String value;
}
