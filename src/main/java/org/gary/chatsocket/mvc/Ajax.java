package org.gary.chatsocket.mvc;


import org.gary.chatsocket.dao.ConnectPool;
import org.gary.chatsocket.dao.AccountDao;
import java.io.IOException;
import java.io.OutputStream;

public class Ajax {
    private OutputStream os;
    public static ConnectPool cp;

    static {
        try {
            cp=new ConnectPool(10);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    Ajax(OutputStream os) {
        this.os = os;
    }

    void doAjax() throws IOException{
        System.out.println("into doAjax()");
        os.write("Hello,I come from ajax\n".getBytes());
    }

    void doJdbc() throws Exception {
        System.out.println("into doJdbc()");
        AccountDao od = new AccountDao(cp);
        String username = od.findName();
        os.write((username + "\n").getBytes());
    }
}
