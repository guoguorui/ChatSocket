package org.gary.chatsocket.dao;

import org.gary.chatsocket.util.MD5Util;

import java.sql.*;

public class AccountDao {

    private Connection conn = null;
    private Statement stmt = null;
    private ResultSet rs = null;
    private ConnectPool cp = null;

    public AccountDao(ConnectPool cp) {
        this.cp=cp;
    }

    private void init(){
        try {
            conn = cp.getConnFromPool();
            stmt = conn.createStatement();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String findName() {
        init();
        String sql = "SELECT name FROM user";
        String name = null;
        try {
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                name = rs.getString("name");
            }
            cleanClose();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return name;
    }

    public boolean authenticate(String name, String password) {
        String uploadPassword= MD5Util.encrypt(password);
        int status=Cache.judgeAuth(name,uploadPassword);
        if(status==1){
            System.out.println("Cache hit");
            return true;
        }else if(status==-1){
            return false;
        }else{
            init();
            boolean flag = false;
            String sql = "SELECT password FROM user where name='" + name + "'";
            try {
                rs = stmt.executeQuery(sql);
                while (rs.next()) {
                    String basePassword=rs.getString("password");
                    if(basePassword!=null){
                        Cache.addAuth(name,basePassword);
                        if (basePassword.equals(uploadPassword)) {
                            flag = true;
                        }
                    }
                }
                cleanClose();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return flag;
        }

    }

    private void cleanClose(){
        boolean flag = false;
        try {
            rs.close();
            stmt.close();
            conn.close();
            flag = true;
        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            try {
                if (stmt != null)
                    stmt.close();
            } catch (SQLException se2) {
            }
            try {
                if (!flag) {
                    conn.close();
                }

            } catch (SQLException se2) {
            }
        }
    }
}
