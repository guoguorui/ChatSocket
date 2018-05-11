package org.gary.chatsocket.util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtil {

    private final static String FORMAT="EEE, d MMM yyyy HH:mm:ss 'GMT'";
    private final static SimpleDateFormat greenwichDate = new SimpleDateFormat(FORMAT, Locale.US);

    public static boolean judgeExpire(String filename, String lastModified) throws Exception{
        File f = new File(filename);
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(f.lastModified());
        Date newChanged=greenwichDate.parse(greenwichDate.format(cal.getTime()));
        Date oldChanged=greenwichDate.parse(lastModified);
        return newChanged.after(oldChanged);
    }

    public static String getLastModified(String filename){
        File f = new File(filename);
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(f.lastModified());
        return greenwichDate.format(cal.getTime());
    }

}
