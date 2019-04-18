package cn.boen.uicab.service;

import cn.boen.uicab.entity.User;
import cn.boen.uicab.mapper.UserMapper;
import cn.boen.uicab.statics.EventType;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.annotation.OnEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@SuppressWarnings("all")
public class UserService {

//    @Value("${spring.mail.suffix}")
    private String suffix;

    @Autowired
    private MIABService miabService;

    @Autowired
    private UserMapper userMapper;

    @OnEvent(EventType.ADD_USER)
    private void addUser(SocketIOClient client, User user) {
        user.setMail(user.getUsername() + "@" + suffix);

        if(miabService.addUser(user)) {
            client.sendEvent(EventType.ADD_USER,userMapper.insert(user));
        } else  {
            client.sendEvent(EventType.ERROR_MESSAGE, "参数错误，添加用户失败");
        }
    }

    @OnEvent(EventType.SEARCH_USER)
    private void searchUser(SocketIOClient client, String keyword) {
        try {
            client.sendEvent(EventType.SEARCH_USER, userMapper.searchUser(keyword));
        }catch (Exception e) {
            client.sendEvent(EventType.ERROR_MESSAGE, e.getMessage());
        }
    }

    @OnEvent(EventType.REMOVE_USER)
    private void removeUser(SocketIOClient client, User user) {
        user.setMail(user.getUsername() + "@" + suffix);

        if(miabService.addUser(user)) {
            client.sendEvent(EventType.ADD_USER,userMapper.insert(user));
        } else  {
            client.sendEvent(EventType.ERROR_MESSAGE, "参数错误，添加用户失败");
        }
    }
}
