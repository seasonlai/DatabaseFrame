package com.season.example.databaseframe;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.season.example.databaseframe.db.BaseDaoFactory;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "season";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void insert(View view) {
        User user = new User();
        user.setName("lisi");
        user.setPassword("123456");
        UserDao userDao = BaseDaoFactory.getInstance().getDataHelper(UserDao.class, User.class);
        long result = userDao.insert(user);
        Log.d(TAG, "insert: " + result + "条");
    }

    public void delete(View view) {
        User user = new User();
        user.setName("lisi");
        user.setPassword("123456");
        UserDao userDao = BaseDaoFactory.getInstance().getDataHelper(UserDao.class, User.class);
        int result = userDao.delete(user);
        Log.d(TAG, "delete: " + result + "条");
    }

    public void update(View view) {
        User userSrc = new User();
        userSrc.setName("lisi");
        userSrc.setPassword("123456");
        User userDst = new User();
        userDst.setName("张三");
        userDst.setPassword("13579");
        UserDao userDao = BaseDaoFactory.getInstance().getDataHelper(UserDao.class, User.class);
        int result = userDao.update(userDst, userSrc);
        Log.d(TAG, "update: " + result + "条");
    }

    public void query(View view) {
        User user = new User();
//        user.setName("lisi");
//        user.setPassword("123456");
        UserDao userDao = BaseDaoFactory.getInstance().getDataHelper(UserDao.class, User.class);
        List<User> result = userDao.query(user);
        Log.d(TAG, "query: " + result.size() + "条");
        for (User item : result) {
            Log.d(TAG, "-------------");
            Log.d(TAG, "name : " + item.getName());
            Log.d(TAG, "pwd : " + item.getPassword());
        }
    }


}
