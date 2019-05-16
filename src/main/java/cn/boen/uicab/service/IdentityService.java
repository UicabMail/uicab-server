package cn.boen.uicab.service;

import cn.boen.uicab.entity.SocketData;
import cn.boen.uicab.entity.User;
import cn.boen.uicab.mapper.UserMapper;
import cn.boen.uicab.statics.EventType;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.annotation.OnEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@SuppressWarnings("all")
public class IdentityService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private  SocketService socketService;

    @OnEvent(EventType.REGISTER)
    private void onRegister(SocketIOClient client) {
    }

    @OnEvent(EventType.LOGIN)
    private void onLogin(SocketIOClient client, User user) {
        User loginedUser =  userMapper.getOne(user);

        if(loginedUser != null ) {

            if(loginedUser.getStatus() == 1) {
                client.sendEvent(EventType.ERROR_MESSAGE, "账户被冻结，请联系管理员");
                return;
            }

            String uuid = client.getSessionId().toString();
            SocketData data =  socketService.clientsMap.get(uuid);
            data.setUser(loginedUser);
            socketService.clientsMap.put(uuid, data);
        }

        client.sendEvent(EventType.LOGIN, loginedUser);
    }

    @OnEvent(EventType.LOGIN_OUT)
    private void onLoginOut(SocketIOClient client) {
    }

    @OnEvent(EventType.CHANGE_PASS)
    private void onChangePass(SocketIOClient client, User user, String newPass) {
        client.sendEvent(EventType.CHANGE_PASS, userMapper.updatePass(user.getUsername(),user.getPassword(), newPass));
    }
}
