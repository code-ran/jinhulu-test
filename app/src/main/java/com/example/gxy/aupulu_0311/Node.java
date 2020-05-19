package com.example.gxy.aupulu_0311;

//=======================================================================================
//类名：Node
//功能：提供节点，方便数据存储及获取
//方法：（1）Node:本方法重载了3个方法，可以通过多种方式创建Node对象
//      （2）getWidget_xx:获取节点xx的值
//      （3）setWidget_xx:设置节点xx的值
//      （4）setAll:设置节点的值
//=======================================================================================
public class Node {
    private int widget_id;
    private String widget_type;
    private String widget_value;
    private String widget_name;
    private String widget_size;
    private String widget_othername;
    private String widget_wr;
    private Node nextnode;

    //=======================================================================================
    //函数名称：getWidget_id
    //函数返回：int:控件id
    //参数说明：无
    //功能概要：本函数可以获取控件id值
    //=======================================================================================
    public int getWidget_id(){
        return this.widget_id;
    }

    //=======================================================================================
    //函数名称：getWidget_type
    //函数返回：String:控件数据类型
    //参数说明：无
    //功能概要：本函数可以获取控件数据类型
    //=======================================================================================
    public String getWidget_type(){
        return this.widget_type;
    }

    //=======================================================================================
    //函数名称：getWidget_value
    //函数返回：String:控件value值
    //参数说明：无
    //功能概要：本函数可以获取控件value值
    //=======================================================================================
    public String getWidget_value(){
        return this.widget_value;
    }

    //=======================================================================================
    //函数名称：getWidget_name
    //函数返回：String:控件数据名称
    //参数说明：无
    //功能概要：本函数可以获取控件数据名称
    //=======================================================================================
    public String getWidget_name(){
        return this.widget_name;
    }

    //=======================================================================================
    //函数名称：getWidget_size
    //函数返回：String:控件数据类型大小
    //参数说明：无
    //功能概要：本函数可以获取控件数据类型大小
    //=======================================================================================
    public String getWidget_size(){
        return this.widget_size;
    }

    //=======================================================================================
    //函数名称：getWidget_othername
    //函数返回：String:控件数据别称
    //参数说明：无
    //功能概要：本函数可以获取控件数据别称
    //=======================================================================================
    public String getWidget_othername(){
        return this.widget_othername;
    }

    //=======================================================================================
    //函数名称：getWidget_wr
    //函数返回：String:控件数据读写性
    //参数说明：无
    //功能概要：本函数可以获取控件数据读写性
    //=======================================================================================
    public String getWidget_wr(){
        return this.widget_wr;
    }

    //=======================================================================================
    //函数名称：getNextnode
    //函数返回：Node:下一节点
    //参数说明：无
    //功能概要：本函数可以获取下一节点
    //=======================================================================================
    public Node getNextnode(){
        return this.nextnode;
    }

    //=======================================================================================
    //函数名称：setWidget_id
    //函数返回：无
    //参数说明：id:设置的id值
    //功能概要：本函数可以设置控件id值
    //=======================================================================================
    public void setWidget_id(int id){
        this.widget_id = id;
    }

    //=======================================================================================
    //函数名称：setWidget_type
    //函数返回：无
    //参数说明：type:设置的数据类型
    //功能概要：本函数可以设置控件数据类型
    //=======================================================================================
    public void setWidget_type(String type){
        this.widget_type = type;
    }

    //=======================================================================================
    //函数名称：setWidget_value
    //函数返回：无
    //参数说明：value:设置的value值
    //功能概要：本函数可以设置控件value值
    //=======================================================================================
    public void setWidget_value(String value){
        this.widget_value = value;
    }

    //=======================================================================================
    //函数名称：setWidget_name
    //函数返回：无
    //参数说明：name:设置的数据名称
    //功能概要：本函数可以设置控件数据名称
    //=======================================================================================
    public void setWidget_name(String name){
        this.widget_name = name;
    }

    //=======================================================================================
    //函数名称：setWidget_size
    //函数返回：无
    //参数说明：size:设置的数据类型大小
    //功能概要：本函数可以设置控件数据类型大小
    //=======================================================================================
    public void setWidget_size(String size){
        this.widget_size = size;
    }

    //=======================================================================================
    //函数名称：setWidget_othername
    //函数返回：无
    //参数说明：othername:设置的数据别称
    //功能概要：本函数可以设置控件数据别称
    //=======================================================================================
    public void setWidget_othername(String othername){
        this.widget_othername = othername;
    }

    //=======================================================================================
    //函数名称：setWidget_wr
    //函数返回：无
    //参数说明：wr:设置的数据读写性
    //功能概要：本函数可以设置控件数据读写性
    //=======================================================================================
    public void setWidget_wr(String wr){
        this.widget_wr = wr;
    }

    //=======================================================================================
    //函数名称：setNextnode
    //函数返回：无
    //参数说明：node:设置的节点
    //功能概要：本函数可以设置节点
    //=======================================================================================
    public void setNextnode(Node node){
        this.nextnode = node;
    }

    //=======================================================================================
    //函数名称：setAll
    //函数返回：无
    //参数说明：id:控件创建的id
    //          name:数据的名称
    //          type:数据的类型
    //          value:数据的值
    //          size:数据类型的大小
    //          othername:数据的别称
    //          wr:数据的读写性
    //功能概要：本函数可以设置节点的值
    //=======================================================================================
    public void setAll(int id, String name, String type, String value, String size, String othername, String wr)
    {
        this.widget_id = id;
        this.widget_type = type;
        this.widget_value = value;
        this.widget_name = name;
        this.widget_size = size;
        this.widget_othername = othername;
        this.widget_wr = wr;
    }
    //=======================================================================================
    //函数名称：Node
    //函数返回：无
    //参数说明：node:创建的节点
    //功能概要：本函数为Node类的构造函数
    //=======================================================================================
    Node(Node node){
        this.widget_id = node.getWidget_id();
        this.widget_type = node.getWidget_type();
        this.widget_value = node.getWidget_value();
        this.widget_name = node.getWidget_name();
        this.widget_size = node.getWidget_size();
        this.widget_othername= node.getWidget_othername();
        this.widget_wr = node.getWidget_wr();
        this.nextnode = node.getNextnode();
    }
    //=======================================================================================
    //函数名称：Node
    //函数返回：无
    //参数说明：id:控件创建的id
    //          name:数据的名称
    //          type:数据的类型
    //          value:数据的值
    //          size:数据类型的大小
    //          othername:数据的别称
    //          wr:数据的读写性
    //功能概要：本函数为Node类的构造函数
    //=======================================================================================
    Node(int id, String name, String type, String value,
         String size, String othername, String wr)
    {
        this.widget_id = id;
        this.widget_type = type;
        this.widget_value = value;
        this.widget_name = name;
        this.widget_size = size;
        this.widget_othername = othername;
        this.widget_wr = wr;
        this.nextnode = null;
    }
    //=======================================================================================
    //函数名称：Node
    //函数返回：无
    //参数说明：无
    //功能概要：本函数为Node类的默认构造函数
    //=======================================================================================
    Node(){
        this.widget_id = 0;
        this.widget_name = "";
        this.widget_type = "";
        this.widget_value = "";
        this.widget_size = "";
        this.widget_othername = "";
        this.widget_wr = "";
        this.nextnode = null;
    }
}
