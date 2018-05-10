package org.gary.chatsocket.dao;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.mysql.jdbc.Connection;

public class ConnectPool {

    private BlockingQueue<Connection> queue=new LinkedBlockingQueue<>();
    private int createdSize = 0;
    private int maxSize;

    static {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public ConnectPool() throws Exception{
        this(10,50);
    }

    public ConnectPool(int initSize) throws Exception{
        this(initSize,50);
    }

    public ConnectPool(int initSize, int maxSize) throws Exception{
        for (int i = 0; i < initSize; i++) {
            Connection connection = this.getConnFromJDBC();
            queue.add(connection);
            createdSize++;
        }
        this.maxSize=maxSize;
    }


    Connection getConnFromPool() throws Exception{
        Connection conn=queue.poll();
        if(conn==null){
            if(createdSize<maxSize){
                createdSize++;
                queue.offer(getConnFromJDBC());
                conn=queue.take();
            }
        }
        return conn;
    }

    //返回一个代理过的Connection对象
    private Connection getConnFromJDBC() throws SQLException{
        String url = "jdbc:mysql://localhost/chatsocket";
        String user = "root";
        String password = "234";
        final Connection conn = (Connection) DriverManager.getConnection(url, user, password);
        return (Connection) Proxy.newProxyInstance(
                ConnectPool.class.getClassLoader(),
                new Class[]{Connection.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args)
                            throws Throwable {
                        Object value = null;
                        //当遇到close方法，就会把对象放回连接池中，而不是关闭连接
                        if (method.getName().equals("close")) {
                            //将代理的对象回收，而不是原始的JDBC4Connection
                            queue.offer((Connection)proxy);
                        } else {
                            //其它方法不变
                            value = method.invoke(conn, args);
                        }
                        return value;
                    }
                }
        );
    }
}
