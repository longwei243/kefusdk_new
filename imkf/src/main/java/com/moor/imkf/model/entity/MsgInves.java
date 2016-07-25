package com.moor.imkf.model.entity;

import com.moor.imkf.ormlite.field.DatabaseField;
import com.moor.imkf.ormlite.table.DatabaseTable;

/**
 * Created by longwei on 2016/3/15.
 */
@DatabaseTable(tableName = "msginves")
public class MsgInves {

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
    @DatabaseField(canBeNull = true, foreign = true, foreignAutoRefresh = true)
    public FromToMessage msg;
}
