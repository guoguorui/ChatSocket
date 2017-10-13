package com.gary.chatsocket;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedList;

import com.mysql.jdbc.Connection;

public class ConnectPool {
	private String url = "jdbc:mysql://localhost/test";
	private String user = "root";
	private String password = "";
	//在static块被引用才变成static
	private static String driverClass="com.mysql.jdbc.Driver";
    private int initSize = 5;
    private int maxSize = 50;
    
    private LinkedList<Connection> connList = new LinkedList<Connection>();
    //是指创建的Conneciton，无论是否被获取，或者是否归还
    private int currentsize = 0;
    static{
        try {
            Class.forName(driverClass);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    public ConnectPool() {
        for(int i = 0; i < initSize; i++)
        {
            Connection connection = this.getConnection();
            connList.add(connection);
            currentsize++;
        }
    }
    
    public Connection getConnFromPool()
    {
        //当连接池还没空
        if(connList.size()>0){
            Connection connection = connList.getFirst();
            connList.removeFirst();
            System.out.println("if-getConnFromPool后: 连接池剩下的Connection:"+connList.size());
            return connection;
        
        }else if(connList.size()==0 && currentsize<maxSize){
            //连接池被拿空，且连接数没有达到上限，创建新的连接
            currentsize++;
            connList.addLast(this.getConnection());   
            Connection connection = connList.getFirst();
            connList.removeFirst();
            System.out.println("else if-getConnFromPool后: 连接池剩下的Connection:"+connList.size());
            return connection;        
        }
        else {
        	throw new RuntimeException("连接数达到上限，请等待");
        }
        
    }
    
    //返回一个代理过的Connection对象
    private Connection getConnection()
    {
        
        try {
            //获取一个连接
            final Connection conn=(Connection) DriverManager.getConnection(url, user, password);
            
            //把连接交给动态代理类转换为代理的连接对象
            Connection myconn = (Connection)Proxy.newProxyInstance(
                    ConnectPool.class.getClassLoader(), 
                    new Class[] {Connection.class}, 
                             //编写一个方法处理器
                    new InvocationHandler() {
                
                @Override
                public Object invoke(Object proxy, Method method, Object[] args)
                        throws Throwable {
                    Object value = null;
                    
                    //当遇到close方法，就会把对象放回连接池中，而不是关闭连接
                    if(method.getName().equals("close"))
                    {
                        //connList.addLast(conn);
                    	//将代理的对象回收，而不是原始的JDBC4Connection
                    	connList.addLast((Connection)proxy);
                        System.out.println("close后: 连接池剩下的Connection:"+connList.size());
                    }else
                    {
                        //其它方法不变
    
                        value = method.invoke(conn, args);
                    }
                    return value;
                }}
            );    
            return myconn;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    
}
