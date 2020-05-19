package com.example.gxy.aupulu_0311;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.os.Handler;
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
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
//import android.widget.Toolbar;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.drafts.Draft;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class MainActivity extends AppCompatActivity
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

    private  WebSocketClient mWebSocketClient;//定义Websocket对象
    //private String address = "wss://sudamcu.com/wsServices";//服务器地址
    private String address;//用来存放服务器地址
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
        setContentView(R.layout.activity_main);
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
        //读取values.xml配置文件内的值
        try {
            //传入文件名：language.xml；用来获取流  
            InputStream is = getAssets().open("AHL.xml");
            //首先创造：DocumentBuilderFactory对象
            DocumentBuilderFactory dBuilderFactory = DocumentBuilderFactory.newInstance();
            //获取：DocumentBuilder对象
            DocumentBuilder dBuilder = dBuilderFactory.newDocumentBuilder();
            //将数据源转换成：document 对象
            Document document = dBuilder.parse(is);
            //获取根元素
            Element element = (Element) document.getDocumentElement();
            //获取子对象的数值 读取lan标签的内容
            NodeList nodeList = element.getElementsByTagName("string");
            for (int i = 0; i < nodeList.getLength(); i++) {
                //获取对应的对象
                Element value = (Element) nodeList.item(i);
                if(value.getAttribute("name").equals("sever_address"))
                {
                    address = value.getAttribute("value");
                }
            }
        }catch(IOException e)
        {
            e.printStackTrace();
        }catch(ParserConfigurationException e)
        {
            e.printStackTrace();
        } catch(SAXException e)
        {
            e.printStackTrace();
        }
        //初始化WebSocket连接
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
        Button btn_empty = (Button) findViewById(R.id.btn_empty);
        Button btn_resend = (Button) findViewById(R.id.btn_resend);
        Button btn_inquire = (Button) findViewById(R.id.btn_inquire);
        //=======================================================================================
        //函数名称：btn_empty.setOnClickListener
        //函数返回：无
        //参数说明：无
        //功能概要：清空按钮的按下监听（清空页面）
        //=======================================================================================
        btn_empty.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                EmptyView();//清空页面
                //发送msgKey4消息给mHandler，执行相应操作
                Message msg = new Message();
                msg.what = msgKey4;
                mHandler.sendMessage(msg);
            }
        });

        //=======================================================================================
        //函数名称：btn_resend.setOnClickListener
        //函数返回：无
        //参数说明：无
        //功能概要：回发按钮的按下监听（回发数据）
        //=======================================================================================
        btn_resend.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                ResendData();//回发数据给服务器
            }
        });

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
                        adapter = new ArrayAdapter<String>(MainActivity.
                                this,android.R.layout.simple_dropdown_item_1line,IMSI_all);
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
            Button btn_rs = (Button) findViewById(R.id.btn_resend);
            TextView textView = (TextView) findViewById(R.id.stateText);
            switch (msg.what) {
                case 2:
                    InitPageControl();//初始化文本框
                    break;
                case 3:
                    textView.setText("接收到最新数据，正在等待下一帧数据");
                    getRealData();//更新文本框数据
                    btn_rs.setEnabled(true);//使能回发按钮
                    break;
                case 4:
                    textView.setText("正在等待接收数据");
                    btn_rs.setEnabled(false);
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
            //跳转到项目界面
            Intent intent = new Intent(MainActivity.this,ProjectActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_realtime) {

        } else if (id == R.id.nav_history) {
            //跳转到历史界面
            Intent intent = new Intent(MainActivity.this,HistoryActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_help) {
            //跳转到Help界面
            Intent intent = new Intent(MainActivity.this,HelpActivity.class);
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

                //（3）获取需要创建控件的父布局
                //线性布局，原界面content部分
                LinearLayout root_layout = (LinearLayout) findViewById(R.id.content);
                //（4）新建需要添加的控件
                //相对布局，用来放置标题及内容
                RelativeLayout Layout = new RelativeLayout(MainActivity.this);
                //标题，otherName
                TextView textView = new TextView(MainActivity.this);
                //文本框，用来放置value
                EditText editText = new EditText(MainActivity.this);

                //（5）设置控件属性
                //（5.1）标题属性设置
                textView.setText(otherName);
                textView.setTextSize(16);
                textView.setTextColor(Color.BLACK);
                textView.setPadding(20, 10, 10, 10);
                //（5.2）文本属性设置
                editText.setPadding(20, 30, 40, 30);
                editText.setTextSize(12);
                Layout.setBackgroundResource(R.drawable.bg_data);
                Layout.setPadding(20, 20, 20, 20);
                editText.setId(paraNum + 20);

                //（5.3）判断是否可读,并设置不同背景
                if (wr_type.equals("read")) {
                    editText.setFocusable(false);
                    editText.setFocusableInTouchMode(false);
                    editText.setBackgroundResource(R.drawable.bg_r);
                }
                if (wr_type.equals("write")) {
                    editText.setBackgroundResource(R.drawable.bg_w);
                }

                WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
                int width = wm.getDefaultDisplay().getWidth();

                //（6）设置控件在其父布局中的属性
                //（6.1）用来将相对布局放入父布局（线性布局中），及设置其在父布局中的属性
                LinearLayout.LayoutParams relativeLayout_parent_params
                        = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                //（6.2）用来将文本放入父布局（相对布局中），及设置其在父布局中的属性
                RelativeLayout.LayoutParams edit_parent_params
                        = new RelativeLayout.LayoutParams((int)(width*0.6),
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                //（6.3）用来将文本放入父布局（相对布局中），及设置其在父布局中的属性
                RelativeLayout.LayoutParams text_parent_params
                        = new RelativeLayout.LayoutParams(
                        (int)(width*0.3),
                        RelativeLayout.LayoutParams.WRAP_CONTENT);

                //（6.4）设置相关标准
                edit_parent_params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                text_parent_params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

                //（7）将新生成控件放入父布局中
                Layout.addView(textView, text_parent_params);
                Layout.addView(editText, edit_parent_params);
                root_layout.addView(Layout, relativeLayout_parent_params);
                //（8）存入生成文本框的相关信息（id, name, type, value）
                parame.add(paraNum + 20, name, type, value,size,otherName,wr_type);
                paraNum++;//控件个数自增
            }

            //（9）主动添加一个空控件，防止按钮遮挡文本框
            //线性布局，原界面content部分
            LinearLayout root_layout = (LinearLayout) findViewById(R.id.content);
            TextView textView = new TextView(MainActivity.this);//标题，otherName
            textView.setText("    ");
            textView.setTextSize(40);

            //用来将相对布局放入父布局（线性布局中），及设置其在父布局中的属性
            LinearLayout.LayoutParams text_parent_params
                    = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);

            //将新生成控件放入父布局中
            root_layout.addView(textView, text_parent_params);
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
                    int IMSI_Flag = 0;
                    //（2）更新文本框
                    for (int i = 0; i < Newdata.length(); i++) {
                        JSONObject jsonObject = Newdata.getJSONObject(i);
                        name = jsonObject.getString("name");
                        value = jsonObject.getString("value");
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
                                adapter = new ArrayAdapter<String>(MainActivity.this,
                                        android.R.layout.simple_dropdown_item_1line, IMSI_all);
                                Spinner spinner = (Spinner) findViewById(R.id.spinner);
                                spinner.setAdapter(adapter);
                            }
                        }
                        //（2.2）更新文本框内值，并将更新值保存
                        for (int j = 0; j <= parameSum; j++) {
                            if (name.equals(parame.getNode(j).getWidget_name())) {
                                int temp = parame.getNode(j).getWidget_id();
                                EditText editText = (EditText) findViewById(temp);
                                editText.setText(value);
                                parame.update_value(value, j);
                                break;
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
    //函数名称：EmptyView
    //函数返回：无
    //参数说明：无
    //功能概要：清空页面数据
    //=======================================================================================
    private void EmptyView() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i <= parame.size(); i++)
                    {
                        //获取控件id
                        int temp = parame.getNode(i).getWidget_id();
                        //根据id清空文本框
                        EditText editText = (EditText) findViewById(temp);
                        editText.setText("");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
    //=======================================================================================
    //函数名称：ResendData
    //函数返回：无
    //参数说明：无
    //功能概要：构造回发数据，并写入下行表
    //=======================================================================================
    private void ResendData() {
        try {
            String valuetemp;
            String sendStr = "";
            String sendData = "[";
            JSONArray sendarr = new JSONArray();
            for (int i = 0; i < parame.size(); i++)
            {
                //（1）获取控件id，并根据id获取其文本框内值
                int temp = parame.getNode(i).getWidget_id();
                EditText editText = (EditText) findViewById(temp);
                valuetemp = editText.getText().toString();
                if(parame.getNode(i).getWidget_name().equals("IMSI"))
                    imsiStr = parame.getNode(i).getWidget_value();
                if(parame.getNode(i).getWidget_name().equals("currentTime")) {
                    long currentTime=System.currentTimeMillis()/1000+28790;
                    valuetemp = String.valueOf(currentTime);
                }
                //（2）新建json对象，并将各元素值添加进去
                JSONObject sendjson = new JSONObject();
                sendjson.put("type",parame.getNode(i).getWidget_type());
                sendjson.put("value",valuetemp);
                sendjson.put("name",parame.getNode(i).getWidget_name());
                sendjson.put("size",parame.getNode(i).getWidget_size());
                sendjson.put("othername",parame.getNode(i).getWidget_othername());
                sendjson.put("wr",parame.getNode(i).getWidget_wr());
                sendarr.put(sendjson);
            }
            //（3）将json对象转为字符串，并作格式处理
            sendData = sendarr.toString();
            sendStr = "{\"command\":\"send\",\"dest\":\""+imsiStr+
                    "\",\"source\":\"Android\",\"password\":\"\",\"data\":"+sendData+"}";
            //（4）将该字符串发送给服务器
            mWebSocketClient.send(sendStr);
        }catch (JSONException e)
        {

        }
    }

    //=======================================================================================
    //函数名称：getDateToString
    //函数返回：String 生成的日期
    //参数说明：long milSecond 时间戳
    //          String pattern 日期字符串形式
    //功能概要：将时间戳转化为日期字符串
    //=======================================================================================
    public static String getDateToString(long milSecond, String pattern) {
        Date date = new Date(milSecond);
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(date);
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
        adapter = new ArrayAdapter<String>(MainActivity.this,android.
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
