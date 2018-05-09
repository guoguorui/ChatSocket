package com.gary.chatsocket;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.SQLException;

class Ajax {
    private OutputStream os;
    static ConnectPool cp = new ConnectPool();

    Ajax(OutputStream os) {
        this.os = os;
    }

    void doAjax() throws IOException{
        System.out.println("into doAjax()");
        os.write("Hello,I come from ajax\n".getBytes());
    }

    void doJdbc() throws Exception {
        System.out.println("into doJdbc()");
        DAO od = new DAO(cp);
        String username = od.findName();
        os.write((username + "\n").getBytes());
    }
}
