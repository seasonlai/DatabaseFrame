package com.season.example.databaseframe.db;

import android.database.sqlite.SQLiteDatabase;

import java.util.List;

/**
 * Created by season on 2017/1/9.
 */

public interface IBaseDao<T> {

    long insert(T entity);

    int update(T entity, T where);

    int delete(T where);

    List<T> query(T where);

    List<T> query(T where, String orderBy, Integer startIndex, Integer limit);
}
