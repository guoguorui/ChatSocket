package com.gary.chatsocket;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedList;

import com.mysql.jdbc.Connection;

class ConnectPool {

    private LinkedList<DAO> blockedOd = new LinkedList<DAO>();
    private LinkedList<Connection> connList = new LinkedList<Connection>();
    //是指创建的Conneciton，无论是否被获取，或者是否归还
    private int currentSize = 0;

    static {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    ConnectPool() {
        int initSize = 5;
        for (int i = 0; i < initSize; i++) {
            Connection connection = this.getConnection();
            connList.add(connection);
            currentSize++;
        }
    }

    //避免多个线程几乎同时通过if，而先进的又未进行操作
    synchronized Connection getConnFromPool(DAO od) {
        int maxSize = 50;
        //当连接池还没空
        if (connList.size() > 0) {
            Connection connection = connList.getFirst();
            connList.removeFirst();
            return connection;

        } else if (currentSize < maxSize) {
            //连接池被拿空，且连接数没有达到上限，创建新的连接
            currentSize++;
            connList.addLast(this.getConnection());
            Connection connection = connList.getFirst();
            connList.removeFirst();
            return connection;
        } else {
            blockedOd.add(od);
            try {
                od.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return getConnFromPool(od);
        }

    }

    //返回一个代理过的Connection对象
    private Connection getConnection() {
        String url = "jdbc:mysql://localhost/test";
        String user = "root";
        String password = "";
        try {
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
                                listAddLast((Connection) proxy);
                            } else {
                                //其它方法不变
                                value = method.invoke(conn, args);
                            }
                            return value;
                        }
                    }
            );
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void listAddLast(Connection conn) {
        connList.addLast(conn);
        int bloLength = blockedOd.size();
        if (bloLength > 0) {
            int connLength = connList.size();
            int processLength;
            if (connLength >= bloLength) {
                processLength = bloLength;
            } else {
                processLength = connLength;
            }

            for (int i = 0; i < processLength; i++) {
                DAO od = blockedOd.get(0);
                od.notify();
                blockedOd.remove(0);
            }
        }
    }
}
