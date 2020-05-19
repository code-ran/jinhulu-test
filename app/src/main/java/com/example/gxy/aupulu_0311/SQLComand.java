package com.example.gxy.aupulu_0311;

import android.os.StrictMode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

//=======================================================================================
//类名：SQLComand
//功能：提供通过安卓对于远程服务器进行数据库操作的方法
//方法：（1）select：本方法重载了3个方法，可以通过多种方式查询数据库
//      （2）update: 修改数据库中表
//      （3）insert: 向数据库中的表插入一条记录
//      （4）executeSQL:执行sql语句，并使用JSONArray格式返回，JSONArray的成员也是
//        JSONArray格式，第一个成员为字段名，后面为表格内容
//=======================================================================================

public class SQLComand {
    private String ip;
    private String Database;
    private String user;
    private String password;
    //=======================================================================================
    //函数名称：SQLComand
    //函数返回：无
    //参数说明：ip:数据库所在远程服务器的ip地址
    //          DataBase：数据库名称
    //          user：用户名
    //          password：用户密码
    //功能概要：本函数为SQLComand类的构造函数，函数中初始化了成员比变量，并设置允许主线程联网
    //=======================================================================================
    public  SQLComand(final String ip, final String Database, final String user,
                      final String password)
    {
        //（1）初始化新建对象的成员变量
        this.ip = ip;
        this.Database = Database;
        this.user = user;
        this.password = password;
        //（2）设置允许主线程联网
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads().detectDiskWrites().detectNetwork()
                .penaltyLog().build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects().detectLeakedClosableObjects()
                .penaltyLog().penaltyDeath().build());
    }

    //=======================================================================================
    //函数名称：select
    //函数返回：无
    //参数说明：tableName:待查询数据库的表名
    //功能概要：本函数将查询数据库中指定表的所有数据，并使用JSONArray格式返回，JSONArray的成员也是
    //        JSONArray格式，第一个成员为字段名，后面为表格内容
    //=======================================================================================
    public JSONArray select(final String tableName)
    {
        String str;
        str = "select * from " + tableName;
        return this.executeSQL(str);   //执行语句，返回结果
    }

    //=======================================================================================
    //函数名称：select
    //函数返回：无
    //参数说明：tableName:待查询数据库的表名
    //          column：筛选条件的字段名
    //          value： 筛选条件的值，可用“，”号隔开筛选多个值，查询关系为“或”
    //功能概要：本函数将根据条件查询数据库中指定表的数据，并使用JSONArray格式返回，JSONArray的成员也是
    //        JSONArray格式，第一个成员为字段名，后面为表格内容
    //=======================================================================================
    public JSONArray select(final String tableName, final String column,String value)
    {
        String str;
        str = "select ";
        int i;
        String[] Value = value.split(",");
        //            if (Column.Count() != Value.Count()) return null;
        str = "select * from " + tableName;
        str += " where ";
        for (i = 0; i < Value.length; i++)
        {
            if (Value[i] == "*") continue;
            str += column + " = '" + Value[i] + "' or ";
        }
        str = str.substring(0,str.length() - 4);
        return this.executeSQL(str);   //执行语句，返回结果
    }

    //=======================================================================================
    //函数名称：select
    //函数返回：无
    //参数说明：tableName:待查询数据库的表名
    //          column：筛选条件的字段名，为字符串数组，每个成员表示一个字段
    //          value： 筛选条件的值，可用“，”号隔开筛选多个值，查询关系为“或”。字符串数组，每个
    //                  成员需要与column的成员对应
    //功能概要：本函数将根据条件查询数据库中指定表的数据，并使用JSONArray格式返回，JSONArray的成员也是
    //        JSONArray格式，第一个成员为字段名，后面为表格内容
    //=======================================================================================
    public JSONArray select(final String tableName, final String[]column,String[] value)
    {
        String sql = "";
        if(column.length!=value.length)return null;
        sql = "select * from " + tableName + " where ";
        for (int i = 0; i < column.length; i++)
        {
            if (value[i] == "*") continue;
            String[] Value = value[i].split(",");
            String thisValue = "";
            for (int j = 0; j < Value.length; j++)
            {
                thisValue += column[i] + " = '" + Value[j] + "' or ";
            }
            thisValue = thisValue.substring(0,thisValue.length() - 4);
            sql += "(" + thisValue + ") and ";
        }
        sql = sql.substring(0,sql.length()-5);
        return this.executeSQL(sql);
    }

    //=======================================================================================
    //函数名称：update
    //函数返回：0：成功；1：查询字段名和值不匹配；2：value的值有错
    //参数说明：tableName:待修改数据库的表名
    //          ID：需要修改的记录的“ID”字段的值
    //          column：待修改的字段名，可用“，”隔开表示修改多个字段
    //          value： 待写入的值，可用“，”号隔开筛选多个值，需要与column一一对应
    //功能概要：本函数将根据条件修改数据库中指定表的数据，并使用JSONArray格式返回，JSONArray的成员也是
    //        JSONArray格式，第一个成员为字段名，后面为表格内容
    //=======================================================================================
    public int update(String tableName,String ID, String column, String value)
    {

        String strSQL = "";
        String[] Column = column.split(",");
        String[] Value = value.split(",");
        if (Column.length != Value.length) return 1;
        if (value.length() < Column.length) return 2;
        strSQL = "update " + tableName + " set ";
        for (int i = 0; i < Column.length; i++)
        {
            strSQL += Column[i] + " = '" + Value[i] + "',";
        }
        strSQL = strSQL.substring(0,strSQL.length() - 1);       //去掉结尾的","
        strSQL += " where ID = " + ID;
        this.executeSQL(strSQL);    //执行语句，返回结果
        return 0;
    }


    //=======================================================================================
    //函数名称：insert
    //函数返回：0：成功；1：查询字段名和值不匹配；2：value的值有错
    //参数说明：tableName:待修改数据库的表名
    //          column：待插入的字段名，可用“，”隔开表示修改多个字段
    //          value： 待插入的值，可用“，”号隔开筛选多个值，需要与column一一对应
    //功能概要：本函数将根据条件向数据库中指定表插入数据，并使用JSONArray格式返回，JSONArray的成员也是
    //        JSONArray格式，第一个成员为字段名，后面为表格内容
    //=======================================================================================
    public int insert(String tableName,String column, String value)
    {
        String strSQL = "", strColumn = "", strValue = "";
        String[] Column = column.split("\\$");
        String[] Value = value.split("\\$");
        if (Column.length != Value.length) return 1;
        if (value.length() < Column.length) return 2;
        for (int i = 0; i < Column.length; i++)
        {
            strColumn += Column[i] + ",";
            strValue += Value[i] + "','";
        }
        strColumn = strColumn.substring(0,strColumn.length() - 1); //去掉结尾的","
        strValue = strValue.substring(0,strValue.length() - 2);    //去掉结尾的",'"
        strSQL = "insert into " + tableName + "(";
        strSQL += strColumn + ") values('";
        strSQL += strValue + ")";
        this.executeSQL(strSQL);    //执行语句，返回结果
        return 0;
    }


    //=======================================================================================
    //函数名称：executeSQL
    //函数返回：执行完SQL语句后返回的数据（已转为Jason格式）
    //参数说明：sql：待执行的sql语句
    //功能概要： 本函数将执行传入的sql语句，并使用JSONArray格式返回，JSONArray的成员也是
    //        JSONArray格式，第一个成员为字段名，后面为表格内容
    //=======================================================================================
    public JSONArray executeSQL(String sql)
    {
        //（1）定义使用到的变量
        JSONArray jsonArray = new JSONArray();
        JSONArray jsonNames = new JSONArray();
        Connection conn = null;
        //（2）创建数据库操作对象
        try
        {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:jtds:sqlserver://"+
                    ip+":1433/" + Database + ";charset=gbk", user, password);
        }catch (ClassNotFoundException e){
            e.printStackTrace();
        }catch (SQLException e){
            e.printStackTrace();
        }
        try
        {
            //（3）建立sql查询
            Statement stmt = conn.createStatement();//
            ResultSet rs = stmt.executeQuery(sql);
            //（4）将查询到的数据表组成JSONArray数据格式
            //该格式的第一个对象为JSONArray，存储变量名
            //其后的每一个对象为JSONObject格式，存放一条记录的数据
            // ，关键字为第一个对象存储的变量名
            while (rs.next())
            {
                JSONArray oneJason = new JSONArray();
//                JSONObject oneJason = new JSONObject();
                ResultSetMetaData rsmd = rs.getMetaData();
                int columnsNum = rsmd.getColumnCount();
                for(int i=0;i<columnsNum;i++)
                {
                    String name = rsmd.getColumnName(i+1);
                    if(jsonNames.length()<columnsNum)jsonNames.put(name);
                    String data = rs.getString(name);
                    if(data == null)data="";
                    oneJason.put(data);
                }
                if(jsonArray.length()==0)jsonArray.put(jsonNames);
                jsonArray.put(oneJason);
            }
            rs.close();
            stmt.close();
            conn.close();
        }
        catch (SQLException e){}
//        catch (JSONException e){}
        return jsonArray;
    }
}