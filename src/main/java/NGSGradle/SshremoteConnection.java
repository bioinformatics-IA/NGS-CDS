/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package NGSGradle;

import com.jcraft.jsch.*;
import java.util.List;
import java.util.logging.Level;
import java.io.IOException;
import java.io.InputStream;

public class SshremoteConnection {
    private static  String pwd,userName,host;
    private static  int port;

    private static Session jschSession;
    private static JSch jsch;
    
    public SshremoteConnection (String uN, String password, String h,int p) {
     userName=uN;
         pwd = password;
         host=h;
         port=p;
    }
    public static void startSession(){
        try {
            jsch= new JSch();
            jsch.setKnownHosts(host);
             jschSession = jsch.getSession(userName, host, port);
             jschSession.setPassword(pwd);
            System.out.println("Session Started");
            
        } catch (JSchException ex) {
            java.util.logging.Logger.getLogger(SshremoteConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
    
    }
    public static void executeShell(List<String> cmdList){
       
        try {
            ChannelExec channelExec = (ChannelExec) jschSession.openChannel("shell");
              
            channelExec.setCommand(cmdList.toString());
            
            System.out.println("Command Recived:  "+cmdList.toString());
             
            channelExec.setErrStream(System.err);
               InputStream in = channelExec.getInputStream();
             // read the result from remote server
            byte[] tmp = new byte[1024];
            while (true) {
                while (in.available() > 0) {
                    int i = in.read(tmp, 0, 1024);
                    if (i < 0) break;
                    System.out.print(new String(tmp, 0, i));
                }
                if (channelExec.isClosed()) {
                    if (in.available() > 0) continue;
                    System.out.println("exit-status: "
                         + channelExec.getExitStatus());
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception ee) {
                }
            }

            channelExec.disconnect();
             
        } catch (JSchException ex) {
            java.util.logging.Logger.getLogger(SshremoteConnection.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(SshremoteConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
     
        
    
    }
    
}