package com.moor.imkf.db.dao;

import com.moor.imkf.ormlite.dao.Dao;
import com.moor.imkf.IMChatManager;
import com.moor.imkf.db.DataBaseHelper;
import com.moor.imkf.model.entity.MsgInves;

import java.sql.SQLException;

/**
 * Created by longwei on 2015/9/16.
 */
public class MsgInvesDao {

    private Dao<MsgInves, Integer> investigateDao = null;
    private DataBaseHelper helper = DataBaseHelper.getHelper(IMChatManager.getInstance().getAppContext());
    private static MsgInvesDao instance;

    private MsgInvesDao() {
        try {
            investigateDao = helper.getMsgInvesDao();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static MsgInvesDao getInstance() {
        if (instance == null) {
            instance = new MsgInvesDao();
        }
        return instance;
    }

    public void insertOneMsgInvesToDao(MsgInves data) {
        try {
                investigateDao.create(data);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
