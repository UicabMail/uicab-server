package cn.boen.uicab.service;

import cn.boen.uicab.entity.SocketData;
import cn.boen.uicab.entity.User;
import cn.boen.uicab.statics.EventType;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.annotation.OnEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@SuppressWarnings("all")
public class ChatService {

    @Autowired
    private SocketService socketService;

    @OnEvent(EventType.MESSAGING)
    private void onMessage(SocketIOClient client, String session, String content) {
        SocketData targrtSocketData = socketService.clientsMap.get(session);
        SocketData currentSocketData = socketService.clientsMap.get(client.getSessionId().toString());

        if(targrtSocketData!=null && currentSocketData != null) {
            User currentUser =  currentSocketData.getUser();

            if(currentUser != null) {
                targrtSocketData.getClient().sendEvent(EventType.MESSAGING, currentUser.getId(),content);
            }
        }
    }
}
