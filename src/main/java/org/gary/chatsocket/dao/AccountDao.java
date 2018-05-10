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

    private void init() throws Exception{
        conn = cp.getConnFromPool();
        stmt = conn.createStatement();
    }

    public String findName() throws Exception{
        init();
        String sql = "SELECT name FROM user";
        String name = null;
        rs = stmt.executeQuery(sql);
        while (rs.next()) {
            name = rs.getString("name");
        }
        cleanClose();
        return name;
    }

    public boolean authenticate(String name, String password) throws Exception{
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
