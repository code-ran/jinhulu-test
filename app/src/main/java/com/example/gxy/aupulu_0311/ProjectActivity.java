package com.example.gxy.aupulu_0311;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ProjectActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    //（1）定义并初始化全局变量
    Parame  parame= new Parame();//用来存放控件结构体
    int parameSum = 0;//自动生成的控件总数
    //消息对象宏定义
    private static final int msgKey2 = 2;
    private static final int msgKey3 = 3;
    private static final int msgKey4 = 4;

    int FirstFlag = 0;//第一次请求标志
    //IMSI号列表
    List<String> IMSI_all = new ArrayList<String>();
    ArrayAdapter<String> adapter;

    String SelectIMSI;//当前选择IMSI号
    String CurrentIMSI;//当前选择IMSI号
    String imsiStr;//用来存放imsi号
    String currentData;//用来存放websocket接收到的数据
    boolean MonitorStatus = true;//监听程序运行标志

    private WebSocketClient mWebSocketClient;//定义Websocket对象
    private String address = "ws://121.41.100.186:10086/wsServices";//服务器地址
    private URI uri;//用来存放服务器的URL

    //=======================================================================================
    //函数名称：onCreate
    //函数返回：无
    //参数说明：savedInstanceState：用来保存调用本Activity的状态信息，防止其被杀死造成数据丢失
    //功能概要：本函数为本Activity的入口函数，主要完成本Activity的界面初始化和相关变量的赋值
    //更新记录：20180611 by GXY
    //=======================================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //（2）根据布局，创建界面
        super.onCreate(savedInstanceState);//调用父类的onCreate函数
        //（2.1）根据activity_main布局，设置正文界面
        setContentView(R.layout.activity_project);
        //（2.2）根据toolbar布局，设置标题
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //（2.2）根据drawer_layout布局，设置侧滑菜单
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        //旋转特效按钮
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer,
                toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        //滑动菜单
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        final SharedPreferences sp = getSharedPreferences("userInfo", MODE_PRIVATE);
        //（2.4）初始化IMSI选择列表
        Init_IMSI_Select();

        //（2.5）初始化WebSocket连接
        initSockect();
        //（2.6）检查侦听程序是否运行
        if(checkMonitorStatus())
        {
            //帧听程序正在运行，提示正在等待数据
            TextView textView = (TextView) findViewById(R.id.stateText);
            textView.setText("正在等待接收数据");
        }
        else
        {
            //帧听程序不在运行，提示检查侦听程序
            TextView textView = (TextView) findViewById(R.id.stateText);
            textView.setText("侦听程序未开启，请检查！");
        }

        //（2.7）重写相关按钮点击事件
        Button btn_inquire = (Button) findViewById(R.id.btn_inquire);
        //=======================================================================================
        //函数名称：btn_inquire.setOnClickListener
        //函数返回：无
        //参数说明：无
        //功能概要：查询按钮的按下监听（判断IMSI是否符合，并添加进列表中页面）
        //=======================================================================================
        btn_inquire.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                EditText editText = (EditText) findViewById(R.id.IMSI_No);
                TextView textView = (TextView) findViewById(R.id.stateText);
                String temp = "";
                temp += editText.getText();
                int flag = 0;
                if(temp.length() == 15) {
                    //判断IMSI是否符合格式
                    for (int i = 0; i < temp.length(); i++) {
                        if((Integer.valueOf(temp.substring(i,i+1)) < 0)
                                | (Integer.valueOf(temp.substring(i,i+1))> 9))
                        {
                            flag = 1;
                            break;
                        }
                    }
                    if (flag == 0)
                    {
                        //符合格式，添加进IMSI列表，并侦听该IMSI信息
                        IMSI_all.add(temp);
                        adapter = new ArrayAdapter<String>(ProjectActivity.this,android.R.layout.simple_dropdown_item_1line,IMSI_all);
                        Spinner spinner = (Spinner) findViewById(R.id.spinner);
                        spinner.setAdapter(adapter);
                        spinner.setSelection(IMSI_all.size()-1);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("IMSI",editText.getText().toString());
                        editor.commit();
                    }
                    //不符合格式，提示输入正确IMSI号
                    else
                    {
                        textView.setText("请输入正确的IMSI号！");
                    }
                }
                else
                {
                    textView.setText("请输入正确的IMSI号！");
                }
            }
        });
    }

    //=======================================================================================
    //函数名称：handleMessage
    //函数返回：无
    //参数说明：Message msg 接收到的消息
    //功能概要：根据接收到的消息执行相应操作（android在不同线程中修改控件属性，需要通过消息传递）
    //=======================================================================================
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            TextView textView = (TextView) findViewById(R.id.stateText);
            switch (msg.what) {
                case 2:
                    InitPageControl();//初始化文本框
                    break;
                case 3:
                    textView.setText("接收到最新数据，正在等待下一帧数据");
                    getRealData();//更新文本框数据
                    break;
                case 4:
                    textView.setText("正在等待接收数据");
                    break;
                default:
                    break;
            }
        }
    };

    //=======================================================================================
    //函数名称：onBackPressed
    //函数返回：无
    //参数说明：无
    //功能概要：回退操作
    //=======================================================================================
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    //=======================================================================================
    //函数名称：onCreateOptionsMenu
    //函数返回：无
    //参数说明：无
    //功能概要：菜单初始化函数
    //=======================================================================================
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_realtime, menu);
        return true;
    }

    //=======================================================================================
    //函数名称：onOptionsItemSelected
    //函数返回：无
    //参数说明：无
    //功能概要：菜单对象选择函数
    //=======================================================================================
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //=======================================================================================
    //函数名称：onNavigationItemSelected
    //函数返回：无
    //参数说明：无
    //功能概要：侧滑栏对象选择函数
    //=======================================================================================
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_project) {

        } else if (id == R.id.nav_realtime) {
            //跳转到实时界面
            Intent intent = new Intent(ProjectActivity.this,MainActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_history) {
            //跳转到历史界面
            Intent intent = new Intent(ProjectActivity.this,HistoryActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_help) {
            //跳转到Help界面
            Intent intent = new Intent(ProjectActivity.this,HelpActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_exit) {
            //退出程序
            finish();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //=======================================================================================
    //函数名称：initSockect
    //函数返回：无
    //参数说明：无
    //功能概要：初始化Websocket连接，并重写相关事件函数
    //=======================================================================================
    public void initSockect() {
        try {
            uri = new URI(address);//读取服务器地址
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        if (null == mWebSocketClient) {
            mWebSocketClient = new WebSocketClient(uri) {
                //连接成功，发送命令获取第一帧数据
                @Override
                public void onOpen(ServerHandshake serverHandshake) {
                    Log.i("javaWebsocket", "onOpen: ");
                    MonitorStatus = true;
                    try {
                        JSONObject sendjson = new JSONObject();
                        sendjson.put("command", "ask");
                        sendjson.put("source", "Android");
                        sendjson.put("password", "");
                        sendjson.put("value", "1");
                        String sendStr = sendjson.toString();
                        mWebSocketClient.send(sendStr);
                    }catch (Exception exception)
                    {
                    }
                }
                //接收到数据，判断命令做相应操作
                @Override
                public void onMessage(String s) {
                    Log.i("javaWebsocket", "onMessage: " + s);
                    try {
                        JSONObject jsonObject = new JSONObject(s);
                        //reAsk命令
                        if (jsonObject.getString("command").equals("reAsk"))
                        {
                            currentData = jsonObject.getString("data");
                            //第一次，发送msgKey2消息给mHandler，执行初始化文本框操作
                            if (FirstFlag == 0) {
                                Message msg = new Message();
                                msg.what = msgKey2;
                                mHandler.sendMessage(msg);
                                FirstFlag = 1;
                            }
                            //第一次后，发送msgKey3消息给mHandler，执行更新文本框操作
                            else {
                                Message msg = new Message();
                                msg.what = msgKey3;
                                mHandler.sendMessage(msg);
                            }
                        }
                        //recv命令
                        else if(jsonObject.getString("command").equals("recv")){
                            CurrentIMSI = jsonObject.getString("source");
                            //根据收到数据的行号，向服务器请求数据
                            if(SelectIMSI.equals("全部") | SelectIMSI.equals(CurrentIMSI)) {
                                JSONObject sendjson = new JSONObject();
                                sendjson.put("command", "ask");
                                sendjson.put("source", "Android");
                                sendjson.put("password", "");
                                sendjson.put("value", jsonObject.getString("value"));
                                String sendStr = sendjson.toString();
                                mWebSocketClient.send(sendStr);
                            }
                        }
                    } catch (JSONException e) {

                    }
                }
                //连接断开，重新尝试连接
                @Override
                public void onClose(int i, String s, boolean b) {
                    Log.i("javaWebsocket", "onClose: ");
                    mWebSocketClient.connect();
                }
                //连接异常，服务器状态置为关
                @Override
                public void onError(Exception e) {
                    Log.i("javaWebsocket", "onError: ");
                    MonitorStatus = false;
                }
            };
            mWebSocketClient.connect();//连接服务器
        }
    }

    //=======================================================================================
    //函数名称：checkMonitorStatus
    //函数返回：boolean：true  侦听程序正在运行；false  侦听程序不在运行
    //参数说明：无
    //功能概要：检查监听程序是否运行
    //=======================================================================================
    private boolean checkMonitorStatus() {
        //侦听程序正在运行
        if(MonitorStatus == true)
        {
            return true;
        }
        //侦听程序不在运行
        return false;
    }

    //=======================================================================================
    //函数名称：InitPageControl
    //函数返回：无
    //参数说明：无
    //功能概要：初始化页面控件
    //=======================================================================================
    private void InitPageControl(){

        String name = null;//名称
        String otherName = null;//别名
        String wr_type = null;//读写类型
        String value = null;//文本值
        String type = null;//数据类型
        String size = null;//数据大小
        int paraNum = 0;//控件个数
        try
        {
            //动态生成文本框
            //（1）将收到的数据转换为json格式
            JSONArray jsonArray = new JSONArray(currentData);

            for(int i = 0; i < jsonArray.length(); i++)
            {
                //（2）获取各数据值
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                name = jsonObject.getString("name");
                otherName = jsonObject.getString("otherName");
                wr_type = jsonObject.getString("wr");
                value = jsonObject.getString("value");
                type = jsonObject.getString("type");
                size = jsonObject.getString("size");
                //（3）根据需求创建所需数据的控件
                if(otherName.equals("芯片温度"))
                {
                    //（3.1）获取需要创建控件的父布局
                    //线性布局，原界面content部分
                    LinearLayout root_layout = (LinearLayout) findViewById(R.id.content);
                    //（3.2）新建需要添加的控件
                    // 标题框，用来表示温度状况
                    TextView Tmptext = new TextView(ProjectActivity.this);
                    // 文本框，用来显示温度值及警告
                    TextView textView = new TextView(ProjectActivity.this);
                    TextView textView1 = new TextView(ProjectActivity.this);
                    //（3.3）设置控件属性
                    //设置标题框属性
                    Tmptext.setText("实验室温度状况：");
                    Tmptext.setTextColor(Color.GRAY);
                    Tmptext.setTextSize(20);
                    //设置文本框属性
                    textView.setText(otherName+"(℃ ):    37.5        正常");
                    textView.setTextColor(Color.BLACK);
                    textView.setPadding(20, 10, 10, 10);
                    textView.setId(paraNum + 20);
                    textView.setTextSize(16);
                    textView1.setText("环境温度(℃ ):    28.1       正常");
                    textView1.setTextColor(Color.BLACK);
                    textView1.setPadding(20, 10, 10, 10);
                    textView1.setId(paraNum + 20);
                    textView1.setTextSize(16);
                    //（3.4）设置控件在其父布局中的属性
                    //用来将标题框放入父布局（线性布局中），及设置其在父布局中的属性
                    LinearLayout.LayoutParams tmptext_parent_params
                            = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    //用来将文本框放入父布局（相对布局中），及设置其在父布局中的属性
                    LinearLayout.LayoutParams text_parent_params
                            = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    LinearLayout.LayoutParams text_parent_params1
                            = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    //（3.5）将新生成控件放入父布局中
                    root_layout.addView(Tmptext, tmptext_parent_params);
                    root_layout.addView(textView, text_parent_params);
                    root_layout.addView(textView1, text_parent_params1);

                }
                if(otherName.equals("光线强度"))
                {
                    //（3.1）获取需要创建控件的父布局
                    //线性布局，原界面content部分
                    LinearLayout root_layout = (LinearLayout) findViewById(R.id.content);
                    //（3.2）新建需要添加的控件
                    // 标题框，用来表示温度状况
                    TextView Tmptext = new TextView(ProjectActivity.this);
                    // 文本框，用来显示温度值及警告
                    TextView textView = new TextView(ProjectActivity.this);
                    //（3.3）设置控件属性
                    //设置标题框属性
                    Tmptext.setText("实验室亮度状况：");
                    Tmptext.setTextColor(Color.GRAY);
                    Tmptext.setTextSize(20);
                    //设置文本框属性
                    textView.setText(otherName+"(等级 ):    3        正常");
                    textView.setTextColor(Color.BLACK);
                    textView.setPadding(20, 10, 10, 10);
                    textView.setId(paraNum + 20);
                    textView.setTextSize(16);
                    //（3.4）设置控件在其父布局中的属性
                    //用来将标题框放入父布局（线性布局中），及设置其在父布局中的属性
                    LinearLayout.LayoutParams tmptext_parent_params
                            = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    //用来将文本框放入父布局（相对布局中），及设置其在父布局中的属性
                    LinearLayout.LayoutParams text_parent_params
                            = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    //（3.5）将新生成控件放入父布局中
                    root_layout.addView(Tmptext, tmptext_parent_params);
                    root_layout.addView(textView, text_parent_params);
                }

                //（8）存入生成文本框的相关信息（id, name, type, value）
                parame.add(paraNum + 20, name, type, value,size,otherName,wr_type);
                paraNum++;//控件个数自增
            }
            parameSum = paraNum-1;
        }
        catch (JSONException e){
        }
    }

    //=======================================================================================
    //函数名称：getRealData
    //函数返回：无
    //参数说明：无
    //功能概要：获取实时数据，并更新页面
    //=======================================================================================
    private void getRealData() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    //（1）将收到的数据转换为json格式
                    JSONArray Newdata = new JSONArray(currentData);
                    String value;//存放文本值
                    String name;//存放文本框名称
                    String otherName;
                    int IMSI_Flag = 0;
                    //（2）更新文本框
                    for (int i = 0; i < Newdata.length(); i++) {
                        JSONObject jsonObject = Newdata.getJSONObject(i);
                        name = jsonObject.getString("name");
                        value = jsonObject.getString("value");
                        otherName = jsonObject.getString("otherName");
                        //（2.1）判断是否添加该IMSI号
                        if (name.equals("IMSI")) {
                            for (int k = 0; k < IMSI_all.size(); k++) {
                                if (value.equals(IMSI_all.get(k))) {
                                    IMSI_Flag = 1;
                                }
                            }
                            if (IMSI_Flag == 1) {
                                IMSI_Flag = 0;
                            } else {
                                IMSI_Flag = 0;
                                IMSI_all.add(value);
                                adapter = new ArrayAdapter<String>(ProjectActivity.this,
                                        android.R.layout.simple_dropdown_item_1line, IMSI_all);
                                Spinner spinner = (Spinner) findViewById(R.id.spinner);
                                spinner.setAdapter(adapter);
                            }
                        }

                        if(name.equals("mcuTemp")) {
                            //（2.2）更新文本框内值，并将更新值保存
                            for (int j = 0; j <= parameSum; j++) {
                                if (name.equals(parame.getNode(j).getWidget_name())) {
                                    int temp = parame.getNode(j).getWidget_id();
                                    TextView textView = (TextView) findViewById(temp);
                                    String Tmpvalue;
                                    Tmpvalue = value.substring(0,value.length()-1)+"."+ value.substring(value.length()-1);
                                    int McuTmp = Integer.valueOf(value).intValue();

                                    if(McuTmp < 222 )
                                    {
                                        textView.setText(otherName+"(℃ ):    "+Tmpvalue+"        过低");
                                        textView.setTextColor(Color.BLUE);
                                    }
                                    else if(McuTmp > 302 )
                                    {
                                        textView.setText(otherName+"(℃ ):    "+Tmpvalue+"        过高");
                                        textView.setTextColor(Color.RED);
                                    }
                                    else
                                    {
                                        textView.setText(otherName+"(℃ ):    "+Tmpvalue+"        正常");
                                        textView.setTextColor(Color.BLACK);
                                    }
                                    parame.update_value(value, j);
                                    break;
                                }
                            }
                        }

                        if(name.equals("bright")){
                            //（2.2）更新文本框内值，并将更新值保存
                            for (int j = 0; j <= parameSum; j++) {
                                if (name.equals(parame.getNode(j).getWidget_name())) {
                                    int temp = parame.getNode(j).getWidget_id();
                                    TextView textView = (TextView) findViewById(temp);
                                    int Bright = Integer.valueOf(value).intValue();
                                    String BrightRate = value.substring(0,1);
                                    if(Bright > 50000 )
                                    {
                                        textView.setText(otherName+"(等级 ):    "+BrightRate+"        过低");
                                        textView.setTextColor(Color.BLUE);
                                    }
                                    else if(Bright < 20000 )
                                    {
                                        textView.setText(otherName+"(等级):    "+BrightRate+"        过高");
                                        textView.setTextColor(Color.RED);
                                    }
                                    else
                                    {
                                        textView.setText(otherName+"(等级):    "+BrightRate+"        正常");
                                        textView.setTextColor(Color.BLACK);
                                    }
                                    break;
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //=======================================================================================
    //函数名称：SelectIMSI
    //函数返回：无
    //参数说明：String value 选择的IMSI号
    //功能概要：选择监听的IMSI号
    //=======================================================================================
    private void Init_IMSI_Select() {
        SharedPreferences sp = getSharedPreferences("userInfo", MODE_PRIVATE);//本地存储文件
        String temp;//用来存放本地IMSI号
        temp = sp.getString("IMSI","");
        IMSI_all.add("全部");//添加“全部”进入IMSI列表
        if(!temp.equals(""))
        {
            IMSI_all.add(temp);//添加本地IMSI号进入IMSI列表
        }
        adapter = new ArrayAdapter<String>(ProjectActivity.this,android.
                R.layout.simple_dropdown_item_1line,IMSI_all);
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setAdapter(adapter);
        //重写spinner监听事件函数
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            //选择对象事件
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SelectIMSI = adapter.getItem(position);//获取当前选择IMSI号
            }
            //无选择事件，若无选择，默认IMSI值为"全部"
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                SelectIMSI = "全部";
            }
        });
    }
}
