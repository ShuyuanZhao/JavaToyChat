package ToyChatCodes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;

import org.apache.http.config.SocketConfig;
import org.apache.http.impl.bootstrap.HttpServer;
import org.apache.http.impl.bootstrap.ServerBootstrap;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.util.EntityUtils;
import org.apache.http.entity.StringEntity;


public class Server {
    public static List<String> OnlineUsers;
    public static List<String> MsgEntityStr;
    public static void main(String[] args) throws Exception {
       int port = 8888;
       SocketConfig socketConfig = SocketConfig.custom().build();
       OnlineUsers = new ArrayList();
       final HttpServer server = ServerBootstrap.bootstrap()
                .setListenerPort(port)
                .setServerInfo("Test/1.1")
                .setSocketConfig(socketConfig)
                .registerHandler("*", new HttpChatHandler())
                .create();
       
       server.start();
    }
    
 static class HttpChatHandler implements HttpRequestHandler  {

     public HttpChatHandler() {
            super(); 
     }

    @Override
    public void handle(HttpRequest hr, HttpResponse hr1, HttpContext hc) throws HttpException, IOException {
        HttpEntity entity = ((HttpEntityEnclosingRequest) hr).getEntity();
        String entityContent = EntityUtils.toString(entity);
        List<NameValuePair> ClientRequestList = new ArrayList();
        ClientRequestList = org.apache.http.client.utils.URLEncodedUtils.parse(entityContent,Consts.UTF_8);
        boolean isLogin = false;
        boolean isChatMsg = false;
        boolean isGetMsg = false;
        
        // 1. if it is login
        for (int i = 0; i < ClientRequestList.size(); i++) {
            // "login" found!
            if (ClientRequestList.get(i).getName().matches("status") && ClientRequestList.get(i).getValue().matches("login")) {
                isLogin = true;
                break;
            }
        }
        if (isLogin) {
            for (int i = 0; i < ClientRequestList.size(); i++) {
            if (ClientRequestList.get(i).getName().matches("nickname")) {
                OnlineUsers.add(ClientRequestList.get(i).getValue());
                break;
            }
        }
        hr1.setStatusCode(HttpStatus.SC_ACCEPTED);
       
       }
       ///////////////////////
       
      // 2. if it is chat
      for (int i = 0; i < ClientRequestList.size(); i++) {
            // "login" found!
            if (ClientRequestList.get(i).getName().matches("status") && ClientRequestList.get(i).getValue().matches("chat")) {
                isChatMsg = true;
                break;
            }
        }
      if (isChatMsg) {
            String tmp_str = "";
            for (int i = 0; i < ClientRequestList.size(); i++) {
            if (ClientRequestList.get(i).getName().matches("origin")) {
                tmp_str += "origin=" + ClientRequestList.get(i).getValue() + "&";
            }
            if (ClientRequestList.get(i).getName().matches("target")) {
                tmp_str += "target=" + ClientRequestList.get(i).getValue() + "&";
            }
            if (ClientRequestList.get(i).getName().matches("msg")) {
                tmp_str += "msg=" + ClientRequestList.get(i).getValue();
            }
        }
            MsgEntityStr.add(tmp_str);
            hr1.setStatusCode(HttpStatus.SC_ACCEPTED);
       }
      ////////////////////////
      
      // 3. get chat msg
      for (int i = 0; i < ClientRequestList.size(); i++) {
            // "login" found!
            if (ClientRequestList.get(i).getName().matches("status") && ClientRequestList.get(i).getValue().matches("getmsg")) {
                isGetMsg = true;
                break;
            }
        }
      if (isGetMsg) {
            String origin = "";
            String target = "";
            for (int i = 0; i < ClientRequestList.size(); i++) {
            if (ClientRequestList.get(i).getName().matches("origin")) {
                origin = ClientRequestList.get(i).getValue();
            }
            if (ClientRequestList.get(i).getName().matches("target")) {
                target = ClientRequestList.get(i).getValue();
            }

        }
            for(String MsgEntitySingleStr : MsgEntityStr) {
                List<NameValuePair> MsgList = new ArrayList();
                MsgList = org.apache.http.client.utils.URLEncodedUtils.parse(MsgEntitySingleStr,Consts.UTF_8);
                boolean isOrig = false;
                boolean isTarget = false;
                String msg = "";
                for (NameValuePair MsgListSingle : MsgList) {
                    if (MsgListSingle.getName().matches("origin") && MsgListSingle.getValue().matches(target)) {
                        isOrig = true;
                    }
                    if (MsgListSingle.getName().matches("target") && MsgListSingle.getValue().matches(origin)) {
                        isTarget = true;
                    }
                    
                    if (MsgListSingle.getName().matches("msg")) {
                        msg = MsgListSingle.getValue();
                    }
                    
                    if(isOrig && isTarget) {
                        hr1.setStatusCode(HttpStatus.SC_ACCEPTED);
                        StringEntity msgentity = new StringEntity(msg);
                        hr1.setEntity(msgentity);
                        MsgEntityStr.remove(MsgEntitySingleStr);
                        break;
                    }
                }
            }
            hr1.setStatusCode(HttpStatus.SC_ACCEPTED);

       }
      ////////////////////////
      
    }
 }
    
}
