package org.gary.chatsocket.dao;

import org.gary.chatsocket.util.MD5Util;

import java.sql.*;

public class AccountDao {

    private Connection conn = null;
    private Statement stmt = null;
    private ResultSet rs = null;
    ConnectPool cp = null;

    public AccountDao(ConnectPool cp) {
        try {
            conn = cp.getConnFromPool();
            stmt = conn.createStatement();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String findName() {
        String sql = "SELECT name FROM users";
        String name = null;
        try {
            rs = stmt.executeQuery(sql);
            ;
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
        boolean flag = false;
        String sql = "SELECT password FROM users where name='" + name + "'";
        try {
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String encryptPassword= MD5Util.encrypt(password);
                if (rs.getString("password").equals(encryptPassword)) {
                    flag = true;
                }
            }
            cleanClose();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
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
