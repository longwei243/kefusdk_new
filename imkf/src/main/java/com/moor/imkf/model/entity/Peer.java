package com.moor.imkf.model.entity;

import java.io.Serializable;

/**
 * Created by longwei on 2016/3/8.
 * 技能组
 */
public class Peer implements Serializable{
    private String id;
    private String name;
    private String status;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
