package com.gary.chatsocket;

import java.sql.*;

class DAO {

    private Connection conn = null;
    private Statement stmt = null;
    private ResultSet rs = null;
    ConnectPool cp = null;

    DAO(ConnectPool cp) {
        try {
            conn = cp.getConnFromPool(this);
            stmt = conn.createStatement();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    String findName() {
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

    boolean authenticate(String name, String password) {
        boolean flag = false;
        String sql = "SELECT password FROM users where name='" + name + "'";
        try {
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String encryptPassword=MD5Util.encrypt(password);
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

    private void cleanClose() {
        boolean flag = false;
        try {
            rs.close();
            stmt.close();
            conn.close();
            flag = true;
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        } finally {
            //finally block used to close resources
            try {
                if (stmt != null)
                    stmt.close();
            } catch (SQLException se2) {
            }// nothing we can do
            try {
                if (!flag) {
                    conn.close();
                }

            } catch (SQLException se2) {
            }
        }//end try
    }
}
