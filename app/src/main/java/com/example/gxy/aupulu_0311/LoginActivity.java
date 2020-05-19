package com.example.gxy.aupulu_0311;

import android.support.v7.app.AppCompatActivity;
//import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
//import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

//import com.example.administrator.sqlitetest.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.example.gxy.aupulu_0311.SqliteDB;
import com.example.gxy.aupulu_0311.User;
public class LoginActivity extends AppCompatActivity {
    private TextView user;
    private EditText count;
    private TextView password;
    private EditText pwd;
    private Button reg;
    private Button login;
    private TextView state;
    private List<User> userList;
    private List<User> dataList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        user=(TextView)findViewById(R.id.User);
        count= (EditText) findViewById(R.id.count);

        password=(TextView)findViewById(R.id.Password);
        pwd= (EditText) findViewById(R.id.pwd);

        reg= (Button) findViewById(R.id.regin);
        login= (Button) findViewById(R.id.login);

        state= (TextView) findViewById(R.id.state);
        reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    if(TextUtils.isEmpty(count.getText())){state.setText("用户名不能为空！");return;}
                    if(TextUtils.isEmpty(pwd.getText())){state.setText("密码不能为空！");return;}
                    String name = count.getText().toString().trim();
                    String pass = pwd.getText().toString().trim();
                    User user = new User();
                    user.setUsername(name);
                    user.setUserpwd(pass);

                    int result = SqliteDB.getInstance(getApplicationContext()).saveUser(user);
                    if (result == 1) {
                        state.setText("注册成功！");
                    } else if (result == -1) {
                        state.setText("用户名已经存在！");
                    } else {
                        state.setText("！");
                    }

                }

        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    if(TextUtils.isEmpty(count.getText())){state.setText("用户名不能为空！");return;}
                    if(TextUtils.isEmpty(pwd.getText())){state.setText("密码不能为空！");return;}
                    String name = count.getText().toString().trim();
                    String pass = pwd.getText().toString().trim();
                    //userList=SqliteDB.getInstance(getApplicationContext()).loadUser();
                    int result = SqliteDB.getInstance(getApplicationContext()).Quer(pass, name);
                    if (result == 1) {
                        state.setText("登录成功！");
                        Intent intent = new Intent();
                        intent.setClass(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                    } else if (result == 0) {
                        state.setText("用户名不存在！");

                    } else if (result == -1) {
                        state.setText("密码错误！");
                    }
/*                for (User user : userList) {
                    if (user.getUsername().equals(name))
                    {
                        if (user.getUserpwd().equals(pass))
                        {
                            state.setText("登录成功！");
                        }else {
                            state.setText("密码错误！");
                        }
                    }
                    else {
                        state.setText("用户名不存在！");
                    }
                }*/

                }

        });
    }
}
