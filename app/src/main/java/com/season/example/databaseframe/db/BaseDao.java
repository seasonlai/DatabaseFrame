package com.season.example.databaseframe.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.season.example.databaseframe.db.annotion.DbField;
import com.season.example.databaseframe.db.annotion.DbTable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by season on 2017/1/9.
 */

public abstract class BaseDao<T> implements IBaseDao<T> {

    private SQLiteDatabase database;
    private boolean isInit = false;
    private Class<T> entityClass;
    private String tableName;

    /**
     * 类的字段与数据库的字段对应
     */
    private HashMap<String, Field> cacheMap;

    protected synchronized boolean init(Class<T> entity, SQLiteDatabase sqLiteDatabase) {
        if (!isInit) {
            entityClass = entity;
            database = sqLiteDatabase;
            if (entity.getAnnotation(DbTable.class) == null) {
                tableName = entity.getClass().getSimpleName();
            } else {
                tableName = entity.getAnnotation(DbTable.class).value();
            }
            if (!database.isOpen()) {
                return false;
            }
            if (!TextUtils.isEmpty(createTable())) {
                database.execSQL(createTable());
            }
            cacheMap = new HashMap<>();
            initCacheMap();
            isInit = true;
            return true;
        }
        return false;
    }

    private void initCacheMap() {
        String sql = "select * from " + this.tableName + " limit 1,0";
        Cursor cursor = null;
        cursor = database.rawQuery(sql, null);
        String[] columns = cursor.getColumnNames();
        cursor.close();
        Field[] columnFields = entityClass.getDeclaredFields();
        //开始对应关系
        for (String columnName : columns) {
            Field columnFiled = null;
            for (Field field : columnFields) {
                String fieldName;
                field.setAccessible(true);
                if (field.getAnnotation(DbField.class) != null) {
                    fieldName = field.getAnnotation(DbField.class).value();
                } else {
                    fieldName = field.getName();
                }
                /**
                 * 如果表的列名 等于了  成员变量的注解名字
                 */
                if (columnName.equals(fieldName)) {
                    columnFiled = field;
                    break;
                }
            }
            //找到了对应关系
            if (columnFiled != null) {
                cacheMap.put(columnName, columnFiled);
            }
        }
    }

    @Override
    public long insert(T entity) {
        Map<String, String> map = getValues(entity);
        ContentValues values = getContentValues(map);
        long result = database.insert(tableName, null, values);
        return result;
    }

    @Override
    public int update(T entity, T where) {
        Map<String, String> values = getValues(entity);
        ContentValues contentValues = getContentValues(values);
        Map<String, String> whereMap = getValues(where);
        Condition condition = new Condition(whereMap);
        return database.update(tableName, contentValues, condition.whereClause, condition.whereArgs);
    }


    @Override
    public int delete(T where) {
        Map<String, String> whereMap = getValues(where);
        Condition condition = new Condition(whereMap);
        return database.delete(tableName, condition.whereClause, condition.whereArgs);
    }

    @Override
    public List<T> query(T where) {
        return query(where, null, null, null);
    }

    @Override
    public List<T> query(T where, String orderBy, Integer startIndex, Integer limit) {

        String limitString = null;
        if (startIndex != null && limit != null) {
            limitString = startIndex + " , " + limit;
        }
        Map<String, String> whereMap = getValues(where);
        Condition condition = new Condition(whereMap);
        Cursor cursor = database.query(tableName, null, condition.whereClause,
                condition.whereArgs, null, null, orderBy, limitString);
        List<T> result = getResult(cursor, where);
        cursor.close();
        return result;
    }

    private List<T> getResult(Cursor cursor, T where) {
        ArrayList list = new ArrayList();
        Object item;
        while (cursor.moveToNext()) {
            Iterator<Map.Entry<String, Field>> iterator = cacheMap.entrySet().iterator();
            try {
                item = where.getClass().newInstance();
                while (iterator.hasNext()) {
                    Map.Entry<String, Field> entry = iterator.next();
                    String columnName = entry.getKey();
                    Field field = entry.getValue();
                    field.setAccessible(true);
                    field.getName();
                    int index = cursor.getColumnIndex(columnName);
                    Class<?> type = field.getType();
                    if (index != -1) {
                        if (type == String.class) {
                            field.set(item, cursor.getString(index));
                        } else if (type == Integer.class) {
                            field.setInt(item, cursor.getInt(index));
                        } else if (type == Double.class) {
                            field.setDouble(item, cursor.getDouble(index));
                        } else if (type == Long.class) {
                            field.setLong(item, cursor.getLong(index));
                        } else if (type == Short.class) {
                            field.setShort(item, cursor.getShort(index));
                        } else if (type == byte[].class) {
                            field.set(item, cursor.getBlob(index));
                        } else {
                            continue;
                        }
                    }
                }
                list.add(item);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    /**
     * 讲map 转换成ContentValues
     *
     * @param map
     * @return
     */
    private ContentValues getContentValues(Map<String, String> map) {
        ContentValues contentValues = new ContentValues();
        Iterator<String> iterator = map.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            String value = map.get(key);
            if (value != null) {
                contentValues.put(key, value);
            }
        }

        return contentValues;
    }

    private Map<String, String> getValues(T entity) {
        HashMap<String, String> result = new HashMap<>();
        Iterator<Field> fieldIterator = cacheMap.values().iterator();
        /**
         * 循环遍历 映射map的  Filed
         */
        while (fieldIterator.hasNext()) {
            /**
             *
             */
            Field columnToFiled = fieldIterator.next();
            String cacheKey;
            String cacheValue = null;
            try {
                Object val = columnToFiled.get(entity);
                if (null == val) {
                    continue;
                }
                cacheValue = val.toString();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            if (columnToFiled.getAnnotation(DbField.class) != null) {
                cacheKey = columnToFiled.getAnnotation(DbField.class).value();
            } else {
                cacheKey = columnToFiled.getName();
            }
            result.put(cacheKey, cacheValue);
        }

        return result;
    }

    class Condition {
        String whereClause;
        String[] whereArgs;

        Condition(Map<String, String> whereMap) {
            Set<String> keys = whereMap.keySet();
            StringBuilder builder = new StringBuilder();
            List<String> values = new ArrayList<>();
            builder.append(" 1=1 ");
            for (String key : keys) {
                String value = whereMap.get(key);
                if (TextUtils.isEmpty(value))
                    continue;
                builder.append(" and ");
                builder.append(key);
                builder.append("=?");
                values.add(value);
            }
            whereClause = builder.toString();
            whereArgs = values.toArray(new String[values.size()]);
        }
    }


    protected abstract String createTable();
}
