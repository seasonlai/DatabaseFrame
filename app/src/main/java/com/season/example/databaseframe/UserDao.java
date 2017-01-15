package com.season.example.databaseframe;

import com.season.example.databaseframe.db.BaseDao;

/**
 * Created by season on 2017/1/11.
 */

public class UserDao extends BaseDao<User> {
    @Override
    protected String createTable() {
        return "create table if not exists tb_user(name TEXT,password TEXT)";
    }
}
