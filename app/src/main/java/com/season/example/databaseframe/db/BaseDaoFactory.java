package com.season.example.databaseframe.db;

import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.HashMap;

/**
 * Created by season on 2017/1/9.
 */

public class BaseDaoFactory {

    private String sqliteDatabasePath;

    private SQLiteDatabase sqliteDatabase;

    private static BaseDaoFactory baseDaoFactory = new BaseDaoFactory();

    private HashMap<String, Object> cacheDao;

    private static final String TAG = "BaseDaoFactory";

    private BaseDaoFactory() {
        sqliteDatabasePath = Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator + "user.db";
        cacheDao = new HashMap<>();
        openDatabase();
    }

    private void openDatabase() {
        this.sqliteDatabase = SQLiteDatabase.openOrCreateDatabase(sqliteDatabasePath, null);
    }

    public synchronized <T extends BaseDao<M>, M> T getDataHelper(Class<T> clazz, Class<M> entity) {
        String key = clazz.getName();
        if (cacheDao.containsKey(key)) {
            Log.d(TAG, "getDataHelper: 已缓存了Dao");
            return (T) cacheDao.get(key);
        }
        BaseDao baseDao = null;
        try {
            baseDao = clazz.newInstance();
            baseDao.init(entity, sqliteDatabase);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        cacheDao.put(key, baseDao);
        return (T) baseDao;
    }

    public static BaseDaoFactory getInstance() {
        return baseDaoFactory;
    }
}
