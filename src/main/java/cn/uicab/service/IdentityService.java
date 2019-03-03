package cn.uicab.service;

import cn.uicab.entity.Message;
import cn.uicab.entity.User;
import cn.uicab.mapper.UserMapper;
import cn.uicab.statics.EventType;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.annotation.OnEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@SuppressWarnings("all")
public class IdentityService {
    @Autowired
    private UserMapper userMapper;

    @OnEvent(EventType.REGISTER)
    private void onRegister(SocketIOClient client, Message message) {
        client.sendEvent("收到", message);
    }

    @OnEvent(EventType.LOGIN)
    private void onLogin(SocketIOClient client, User user) {
        client.sendEvent(EventType.LOGIN, userMapper.getOne(user));
    }

    @OnEvent(EventType.LOGIN_OUT)
    private void onLoginOut(SocketIOClient client, Message message) {
        client.sendEvent("收到", message);
    }
}
