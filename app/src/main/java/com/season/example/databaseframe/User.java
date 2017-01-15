package com.season.example.databaseframe;

import com.season.example.databaseframe.db.annotion.DbField;
import com.season.example.databaseframe.db.annotion.DbTable;

/**
 * Created by season on 2017/1/9.
 */
@DbTable("tb_user")
public class User {

    @DbField("name")
    private String name;
    @DbField("password")
    private String password;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
