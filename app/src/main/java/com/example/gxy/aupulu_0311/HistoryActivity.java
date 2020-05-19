package com.example.gxy.aupulu_0311;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.String;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class HistoryActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    //（1）定义并初始化全局变量
    Parame  parame= new Parame();//用来存放控件结构体
    int parameSum = 0;//自动生成的控件总数
    //消息对象宏定义
    private static final int msgKey1 = 1;
    private static final int msgKey2 = 2;

    int FirstFlag = 0;//第一次请求标志
    //IMSI号列表
    List<String> IMSI_all = new ArrayList<String>();
    ArrayAdapter<String> adapter;
    String SelectIMSI;//当前选择IMSI号
    String CurrentIMSI;//当前选择IMSI号
    String imsiStr;//用来存放imsi号
    String currentData;//用来存放websocket接收到的数据
    boolean MonitorStatus = true;//监听程序运行标志
    String CurrentFrame;//当前帧数
    String TotalFrames;//最大帧数

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
        setContentView(R.layout.activity_history);
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
        //（2.6）重写相关按钮点击事件
        ImageView image_newest = (ImageView) findViewById(R.id.btn_newest);
        ImageView image_right = (ImageView) findViewById(R.id.btn_right);
        ImageView image_left = (ImageView) findViewById(R.id.btn_left);
        ImageView image_old = (ImageView) findViewById(R.id.btn_old);
        ImageView image_return = (ImageView) findViewById(R.id.btn_return);
        //=======================================================================================
        //函数名称：image_newest.setOnClickListener
        //函数返回：无
        //参数说明：无
        //功能概要：最新帧按钮按下监听，获取最新一帧数据
        //=======================================================================================
        image_newest.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //当前帧号为总帧号
                CurrentFrame = TotalFrames;
                //向服务器请求数据
                SendData(CurrentFrame);
            }
        });
        //=======================================================================================
        //函数名称：image_right.setOnClickListener
        //函数返回：无
        //参数说明：无
        //功能概要：下一帧按钮按下监听，获取下一帧数据
        //=======================================================================================
        image_right.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //当前帧号加1，若超出总帧数，则为1
                int frameNo = Integer.valueOf(CurrentFrame).intValue();
                int frameMax = Integer.valueOf(TotalFrames).intValue();
                if (frameNo >= frameMax)
                    CurrentFrame = "1";
                else {
                    frameNo++;
                    CurrentFrame = String.valueOf(frameNo);
                }
                //向服务器请求数据
                SendData(CurrentFrame);
            }
        });
        //=======================================================================================
        //函数名称：image_left.setOnClickListener
        //函数返回：无
        //参数说明：无
        //功能概要：上一帧按钮按下监听，获取上一帧数据
        //=======================================================================================
        image_left.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //当前帧号减1，若低于第一帧，则为总帧数
                int frameNo = Integer.valueOf(CurrentFrame).intValue();
                if (frameNo <= 1)
                    CurrentFrame = TotalFrames;
                else {
                    frameNo--;
                    CurrentFrame = String.valueOf(frameNo);
                }
                //向服务器请求数据
                SendData(CurrentFrame);
            }
        });
        //=======================================================================================
        //函数名称：image_old.setOnClickListener
        //函数返回：无
        //参数说明：无
        //功能概要：最旧帧按钮按下监听，获取最旧一帧数据
        //=======================================================================================
        image_old.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //当前帧号为1
                CurrentFrame = "1";
                //向服务器请求数据
                SendData(CurrentFrame);
            }
        });
        //=======================================================================================
        //函数名称：btn_resend.setOnClickListener
        //函数返回：无
        //参数说明：无
        //功能概要：回发按钮的按下监听（回发数据）
        //=======================================================================================
        image_return.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
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
            switch (msg.what) {
                case 1:
                    InitPage();//初始化文本框
                    break;
                case 2:
                    TextView textView = (TextView) findViewById(R.id.stateText);
                    textView.setText("当前为第"+CurrentFrame+"/"+TotalFrames+"帧");
                    updataPage();//更新文本框数据
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
        getMenuInflater().inflate(R.menu.menu_history, menu);
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
            Intent intent = new Intent(HistoryActivity.this,ProjectActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_realtime) {
            //跳转到实时界面
            Intent intent = new Intent(HistoryActivity.this,MainActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_history) {

        } else if (id == R.id.nav_help) {
            //跳转到Help界面
            Intent intent = new Intent(HistoryActivity.this,HelpActivity.class);
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
                    SendData("1");
                }
                //接收到数据，判断命令做相应操作
                @Override
                public void onMessage(String s) {
                    Log.i("javaWebsocket", "onMessage: " + s);
                    try{
                        JSONObject jsonObject = new JSONObject(s);
                        //reAsk命令
                        if (jsonObject.getString("command").equals("reAsk") &&
                                jsonObject.getString("source").equals("CS-Monitor"))
                        {
                            //第一次，发送msgKey1消息给mHandler，执行初始化文本框操作
                            currentData = jsonObject.getString("data");
                            CurrentFrame = jsonObject.getString("currentRow");
                            TotalFrames = jsonObject.getString("totalRows");
                            if(FirstFlag == 0) {
                                FirstFlag = 1;
                                Message msg = new Message();
                                msg.what = msgKey1;
                                mHandler.sendMessage(msg);
                                SendData(TotalFrames);
                            }
                            //第一次后，发送msgKey2消息给mHandler，执行更新文本框操作
                            else
                            {
                                Message msg = new Message();
                                msg.what = msgKey2;
                                mHandler.sendMessage(msg);
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
                }
            };
            mWebSocketClient.connect();//连接服务器
        }
    }

    //=======================================================================================
    //函数名称：SendData
    //函数返回：无
    //参数说明：row：数据帧在数据库中的行数
    //功能概要：向服务器请求数据
    //=======================================================================================
    private void SendData(String row) {
        try {
            JSONObject sendjson = new JSONObject();
            sendjson.put("command", "ask");
            sendjson.put("source", "Android");
            sendjson.put("password", "");
            sendjson.put("value", row);
            String sendStr = sendjson.toString();
            mWebSocketClient.send(sendStr);
        }catch (JSONException e)
        {
        }
    }

    //=======================================================================================
    //函数名称：InitPage
    //函数返回：无
    //参数说明：无
    //功能概要：初始化页面控件
    //=======================================================================================
    private void InitPage(){
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
                RelativeLayout Layout = new RelativeLayout(HistoryActivity.this);
                //标题，otherName
                TextView textView = new TextView(HistoryActivity.this);
                //文本框，用来放置value
                EditText editText = new EditText(HistoryActivity.this);

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

                //（6）设置控件在其父布局中的属性
                //（6.1）用来将相对布局放入父布局（线性布局中），及设置其在父布局中的属性
                LinearLayout.LayoutParams relativeLayout_parent_params
                        = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                //（6.2）用来将文本放入父布局（相对布局中），及设置其在父布局中的属性
                RelativeLayout.LayoutParams edit_parent_params
                        = new RelativeLayout.LayoutParams(600,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                //（6.3）用来将文本放入父布局（相对布局中），及设置其在父布局中的属性
                RelativeLayout.LayoutParams text_parent_params
                        = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
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
            TextView textView = new TextView(HistoryActivity.this);//标题，otherName
            textView.setText("    ");
            textView.setTextSize(40);

            //用来将相对布局放入父布局（线性布局中），及设置其在父布局中的属性
            LinearLayout.LayoutParams text_parent_params
                    = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);

            //将新生成控件放入父布局中
            root_layout.addView(textView, text_parent_params);
            parameSum = paraNum-1;
        }
        catch (JSONException e){
        }
    }

    //=======================================================================================
    //函数名称：updataPage
    //函数返回：无
    //参数说明：无
    //功能概要：获取实时数据，并更新页面
    //=======================================================================================
    private void updataPage() {
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
//                        //（2.1）判断是否添加该IMSI号
//                        if (name.equals("IMSI")) {
//                            for (int k = 0; k < IMSI_all.size(); k++) {
//                                if (value.equals(IMSI_all.get(k))) {
//                                    IMSI_Flag = 1;
//                                }
//                            }
//                            if (IMSI_Flag == 1) {
//                                IMSI_Flag = 0;
//                            } else {
//                                IMSI_Flag = 0;
//                                IMSI_all.add(value);
//                                adapter = new ArrayAdapter<String>(HistoryActivity.this,
//                                        android.R.layout.simple_dropdown_item_1line, IMSI_all);
//                                Spinner spinner = (Spinner) findViewById(R.id.spinner);
//                                spinner.setAdapter(adapter);
//                            }
//                        }
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
    //函数名称：SelectIMSI
    //函数返回：无
    //参数说明：String value 选择的IMSI号
    //功能概要：选择监听的IMSI号
    //=======================================================================================
    private void Init_IMSI_Select() {
        IMSI_all.add("全部");//添加“全部”进入IMSI列表
        adapter = new ArrayAdapter<String>(HistoryActivity.this,android.
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


