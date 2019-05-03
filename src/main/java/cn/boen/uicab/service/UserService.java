package cn.boen.uicab.service;

import cn.boen.uicab.entity.SocketData;
import cn.boen.uicab.entity.User;
import cn.boen.uicab.mapper.UserMapper;
import cn.boen.uicab.statics.EventType;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.annotation.OnEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@SuppressWarnings("all")
public class UserService {

    @Value("${spring.mail.suffix}")
    private String suffix;

    @Autowired
    private MIABService miabService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private  SocketService socketService;

    @OnEvent(EventType.ADD_USER)
    private void addUser(SocketIOClient client, User user) {
        user.setMail(user.getUsername() + "@" + suffix);

        if(miabService.addUser(user)) {
            client.sendEvent(EventType.ADD_USER,userMapper.insert(user));
        } else  {
            client.sendEvent(EventType.ERROR_MESSAGE, "参数错误，添加用户失败");
        }
    }

    @OnEvent(EventType.UPDATE_USER)
    private void updateUser(SocketIOClient client, User user) {
        try {
            client.sendEvent(EventType.UPDATE_USER, userMapper.update(user));
        }catch (Exception e) {
            client.sendEvent(EventType.ERROR_MESSAGE, e.getMessage());
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

        if(miabService.removeUser(user)) {
            client.sendEvent(EventType.REMOVE_USER,userMapper.delete(user));
        } else  {
            client.sendEvent(EventType.ERROR_MESSAGE, "参数错误，删除用户失败");
        }
    }

    @OnEvent(EventType.GET_USER)
    private void getUser(SocketIOClient client, boolean isAll) {

        if(isAll){
            client.sendEvent(EventType.GET_USER,userMapper.getAll());
        }else {
            SocketData socketData = socketService.clientsMap.get(client.getSessionId().toString());

            if(socketData != null ) {
                User user = socketData.getUser();

                if(user != null) {
                    client.sendEvent(EventType.GET_USER,userMapper.getDept(user.getDept()));
                }
            }
        }
    }

    @OnEvent(EventType.CHECK_ONLINE)
    private void checkOnline(SocketIOClient client, Integer id) {
        for (SocketData socketData: socketService.clientsMap.values()) {
             User user = socketData.getUser();

             if(user!=null && user.getId() == id) {
                 client.sendEvent(EventType.CHECK_ONLINE,true,socketData.getClient().getSessionId().toString());
                 return;
             }
        }

        client.sendEvent(EventType.CHECK_ONLINE,false);
    }
}
