package cn.boen.uicab.entity;

import com.corundumstudio.socketio.SocketIOClient;
import lombok.Data;

@Data
public class SocketData {
    private User user;
    private SocketIOClient client;
}
