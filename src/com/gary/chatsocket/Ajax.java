package com.gary.chatsocket;

import java.io.PrintWriter;
import java.sql.SQLException;

class Ajax {
    private PrintWriter pw;
    static ConnectPool cp = new ConnectPool();

    Ajax(PrintWriter pw) {
        this.pw = pw;
    }

    void doAjax() {
        System.out.println("into doAjax()");
        pw.write("Hello,I come from ajax\n");
        pw.close();
    }

    void doJdbc() throws SQLException {
        System.out.println("into doJdbc()");
        DAO od = new DAO(cp);
        String username = od.findName();
        pw.write(username + "\n");
        pw.close();
    }
}
