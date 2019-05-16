package cn.boen.uicab.entity;

import com.corundumstudio.socketio.SocketIOClient;
import lombok.Data;

import javax.mail.Store;

@Data
public class SocketData {
    private User user;
    private SocketIOClient client;
    private Store imapStore;
//    private Map<String,Map<Folder,  Message[]>> source;
}
