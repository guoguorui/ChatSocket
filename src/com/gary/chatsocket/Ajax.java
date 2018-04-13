package com.gary.chatsocket;

import java.io.PrintWriter;
import java.sql.SQLException;

public class Ajax {
    PrintWriter pw;
    static ConnectPool cp = new ConnectPool();

    public Ajax(PrintWriter pw) {
        this.pw = pw;
    }

    public void doAjax() {
        System.out.println("into doAjax()");
        pw.write("Hello,I come from ajax\n");
        pw.close();
    }

    public void doJdbc() throws SQLException {
        System.out.println("into doJdbc()");
        OperData od = new OperData(cp);
        String username = od.findName();
        pw.write(username + "\n");
        pw.close();
    }
}
