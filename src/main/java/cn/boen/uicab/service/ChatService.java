package cn.boen.uicab.service;

import cn.boen.uicab.entity.Message;
import cn.boen.uicab.statics.EventType;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.annotation.OnEvent;
import org.springframework.stereotype.Service;

@Service
@SuppressWarnings("all")
public class ChatService {

    @OnEvent(EventType.MESSAGING)
    private void onMessage(SocketIOClient client, Message message) {
        client.sendEvent(EventType.MESSAGING, message);
    }
}
