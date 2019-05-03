package cn.boen.uicab.service;

import cn.boen.uicab.entity.SocketData;
import cn.boen.uicab.statics.EventType;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class SocketService {

    @Autowired
    private SocketIOServer server;

     static Map<String, SocketData> clientsMap = new HashMap<String, SocketData>();

    @OnConnect
    public void onConnect(SocketIOClient client) {
        String uuid = client.getSessionId().toString();
        SocketData data =  new SocketData();
        data.setClient(client);
        clientsMap.put(uuid, data);

        client.sendEvent(EventType.INITIALIZATION);
    }

    @OnDisconnect
    public void onDisconnect(SocketIOClient client) {
        String uuid = client.getSessionId().toString();
        clientsMap.remove(uuid);
    }

//    public void sendMessageToAllClient(String message) {
//        Collection<SocketIOClient> clients = server.getAllClients();
//        for (SocketIOClient client : clients) {
//            client.sendEvent("hello", message);
//        }
//    }
}
