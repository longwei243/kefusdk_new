package com.moor.imkf.db.dao;

import com.moor.imkf.ormlite.dao.Dao;
import com.moor.imkf.IMChatManager;
import com.moor.imkf.db.DataBaseHelper;
import com.moor.imkf.model.entity.Info;
import com.moor.imkf.utils.LogUtil;

import java.sql.SQLException;

/**
 * Created by longwei on 2015/9/16.
 */
public class InfoDao {

    private Dao<Info, Integer> infoDao = null;
    private DataBaseHelper helper = DataBaseHelper.getHelper(IMChatManager.getInstance().getAppContext());
    private static InfoDao instance;

    private InfoDao() {
        try {
            infoDao = helper.getInfoDao();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static InfoDao getInstance() {
        if (instance == null) {
            instance = new InfoDao();
        }
        return instance;
    }

    /**
     * 将信息存入数据库中
     * @param info
     */
    public void insertInfoToDao(Info info) {
        try {
            infoDao.delete(infoDao.queryForAll());
            infoDao.create(info);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存connectionId
     * @param connectionId
     */
    public void saveConnectionId(String connectionId) {
        try {
            Info info = infoDao.queryForAll().get(0);
            if(info != null) {
                info.connectionId = connectionId;
                infoDao.update(info);
                LogUtil.d("InfoDao", "connectionId存入了数据库中");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从数据库中获取connectionId
     * @return
     */
    public String getConnectionId() {
        String connectionId = "";
        try {
            Info info = infoDao.queryForAll().get(0);
            if(info != null) {
                connectionId = info.connectionId;
                return connectionId;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 从数据库中获取imServiceNo
     * @return
     */
    public String getImServiceNo() {
        String imServiceNo = "";
        try {
            Info info = infoDao.queryForAll().get(0);
            if(info != null) {
                imServiceNo = info.imServiceNo;
                return imServiceNo;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 从数据库中获取accessId
     * @return
     */
    public String getAccessId() {
        String accessId = "";
        try {
            Info info = infoDao.queryForAll().get(0);
            if(info != null) {
                accessId = info.accessId;
                return accessId;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 从数据库中获取userId
     * @return
     */
    public String getUserId() {
        String userId = "";
        try {
            Info info = infoDao.queryForAll().get(0);
            if(info != null) {
                userId = info.userId;
                return userId;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 从数据库中获取loginName
     * @return
     */
    public String getLoginName() {
        String loginName = "";
        try {
            Info info = infoDao.queryForAll().get(0);
            if(info != null) {
                loginName = info.loginName;
                return loginName;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 删除所有数据
     */
    public void deleteAll() {
        try {
            infoDao.delete(infoDao.queryForAll());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
