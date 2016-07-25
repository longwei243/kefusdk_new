package com.moor.imkf.db.dao;

import com.moor.imkf.ormlite.dao.Dao;
import com.moor.imkf.IMChatManager;
import com.moor.imkf.db.DataBaseHelper;
import com.moor.imkf.model.entity.Investigate;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by longwei on 2015/9/16.
 */
public class InvestigateDao {

    private Dao<Investigate, Integer> investigateDao = null;
    private DataBaseHelper helper = DataBaseHelper.getHelper(IMChatManager.getInstance().getAppContext());
    private static InvestigateDao instance;

    private InvestigateDao() {
        try {
            investigateDao = helper.getInvestigateDao();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static InvestigateDao getInstance() {
        if (instance == null) {
            instance = new InvestigateDao();
        }
        return instance;
    }

    /**
     * 将评价存入数据库中
     * @param datas
     */
    public void insertInvestigateToDao(List<Investigate> datas) {
        try {
            for (int i=0; i<datas.size(); i++) {
                investigateDao.create(datas.get(i));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertOneInvestigateToDao(Investigate data) {
        try {
                investigateDao.create(data);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从数据库中取出评价列表
     * @return
     */
    public List<Investigate> getInvestigatesFromDao() {
        List<Investigate> list = new ArrayList<Investigate>();
        try {
            list = investigateDao.queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 删除所有数据
     */
    public void deleteAll() {
        try {
            investigateDao.delete(investigateDao.queryForAll());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
