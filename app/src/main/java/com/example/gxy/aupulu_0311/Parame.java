package com.example.gxy.aupulu_0311;

//=======================================================================================
//类名：Parame
//功能：提供动态链表用来存储控件信息
//方法：（1）size：获取链表长度
//      （2）add: 向链表末尾添加节点
//      （3）insert: 向链表指定位置插入节点
//      （4）delete:删除链表指定位置节点
//      （5）update:更新链表指定位置节点信息
//      （6）update_value:更新链表指定位置节点value值
//      （7）getNode:获取链表指定位置节点
//=======================================================================================
public class Parame {

    private Node head;

    //=======================================================================================
    //函数名称：size
    //函数返回：int:当前Parame链表的长度
    //参数说明：无
    //功能概要：本函数将返回某个Parame链表的长度
    //=======================================================================================
    public int size(){
        //（1）头节点为空，则长度为0
        if(head == null){
            return 0;
        }
        int size = 1;
        Node node = head;
        //（2）遍历链表，并返回长度
        while(node.getNextnode() != null){
            node = node.getNextnode();
            size++;
        }
        return size;
    }

    //=======================================================================================
    //函数名称：add
    //函数返回：无
    //参数说明：id:控件创建的id
    //          name:数据的名称
    //          type:数据的类型
    //          value:数据的值
    //          size:数据类型的大小
    //          othername:数据的别称
    //          wr:数据的读写性
    //功能概要：本函数将在某个Parame链表末尾添加一个Parame节点
    //=======================================================================================
    public void add(int id, String name, String type, String value,
                    String size, String othername, String wr)
    {
        Node newNode = new Node(id, name, type, value, size, othername, wr);
        //（1）如果头结点为空，直接添加到头结点
        if(head == null){
            head = newNode;
            return ;
        }
        //（2）否则添加到末尾
        Node node = head;
        while(node.getNextnode() != null){
            node = node.getNextnode();
        }
        node.setNextnode(newNode);
    }

    //=======================================================================================
    //函数名称：delete
    //函数返回：boolean：true,删除成功:false,删除失败.
    //参数说明：delete_node:待删除节点
    //功能概要：本函数将删除某个Parame链表中指定位置节点
    //=======================================================================================
    public boolean delete(Node delete_node){
        return false;
    }

    //=======================================================================================
    //函数名称：getNode
    //函数返回：Node：获取的节点
    //参数说明：index:获取节点的位置
    //功能概要：本函数将获取某个Parame链表中指定位置节点
    //=======================================================================================
    public Node getNode(int index){
        //防止越界
        if(index>size()||index<0){
            try {
                throw new Exception("下标越界");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        int count = 0;
        Node node = head;
        //获取指定位置节点
        while(count != index){
            node = node.getNextnode();
            count++;
        }
        return node;
    }

    //=======================================================================================
    //函数名称：insert
    //函数返回：boolean：true,插入成功:false,插入失败.
    //参数说明：id:控件创建的id
    //          name:数据的名称
    //          type:数据的类型
    //          value:数据的值
    //          size:数据类型的大小
    //          othername:数据的别称
    //          wr:数据的读写性
    //          index:待添加节点的位置
    //功能概要：本函数将在某个Parame链表指定位置插入一个Parame对象
    //=======================================================================================
    public boolean insert(int id, String name, String type, String value,
                          String size, String othername, String wr,int index)
    {
        //防止越界
        if(index<0||index>size()){
            try {
                throw new Exception("输入下标越界");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }
        //链表为空，则直接创建一个新的头结点
        if(size() == 0){
            head = new Node(id, name, type, value, size, othername, wr);
            return true;
        }
        //链表不为空，插入位置为头，则替换原头结点，原头结点为头结点的下一节点
        else if(index == 0){
            Node newNode = new Node(id, name, type, value, size, othername, wr);
            Node node = head;
            head = newNode;
            head.setNextnode(node);
            return true;
        }
        //链表不为空，插入位置不为头，则在指定位置插入该结点
        Node newNode = new Node(id, name, type, value, size, othername, wr);
        Node node = head;
        int count = 0;
        while(count!=index-1){
            node = node.getNextnode();
            count++;
        }
        Node nodeNext = node.getNextnode();
        node.setNextnode(newNode);
        newNode.setNextnode(nodeNext);
        return true;
    }

    //=======================================================================================
    //函数名称：update
    //函数返回：boolean：true,修改成功:false,修改失败.
    //参数说明：id:控件创建的id
    //          name:数据的名称
    //          type:数据的类型
    //          value:数据的值
    //          size:数据类型的大小
    //          othername:数据的别称
    //          wr:数据的读写性
    //          index:待修改节点的位置
    //功能概要：本函数将修改某个Parame链表指定位置节点的值
    //=======================================================================================
    public boolean update(int id, String name, String type, String value,
                          String size, String othername, String wr,int index)
    {
        //防止越界
        if(index>size()||index<0){
            try {
                throw new Exception("下标越界");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        int count = 0;
        Node node = head;
        //遍历链表找到该结点
        while(count != index){
            node = node.getNextnode();
            count++;
        }
        //修改节点值
        node.setAll(id, name, type, value, size, othername, wr);
        return true;
    }

    //=======================================================================================
    //函数名称：update_value
    //函数返回：boolean：true,修改成功:false,修改失败.
    //参数说明：value:数据的值
    //          index:待修改节点的位置
    //功能概要：本函数将修改某个Parame链表指定位置节点的value值
    //=======================================================================================
    public boolean update_value(String value,int index){
        //防止越界
        if(index>size()||index<0){
            try {
                throw new Exception("下标越界");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        int count = 0;
        Node node = head;
        //遍历链表找到该结点
        while(count != index){
            node = node.getNextnode();
            count++;
        }
        //修改节点的value值
        node.setWidget_value(value);
        return true;
    }
}
