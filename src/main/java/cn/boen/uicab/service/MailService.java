package cn.boen.uicab.service;

import cn.boen.uicab.entity.Mail;
import cn.boen.uicab.entity.SocketData;
import cn.boen.uicab.entity.User;
import cn.boen.uicab.statics.EventType;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.annotation.OnEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@SuppressWarnings("all")
public class MailService {

    @Autowired
    private SocketService socketService;

    @Autowired
    private SMTPService smtpService;

    @Autowired
    private IMAPService imapService;

    @OnEvent(EventType.SEND)
    private void send(SocketIOClient client, Mail mail, String[] receives,Boolean isHtml ) {
        SocketData socketData = socketService.clientsMap.get(client.getSessionId().toString());

        if(socketData != null) {
            User user =  socketData.getUser();

            if(user != null) {
                if(isHtml) {
                    client.sendEvent(EventType.SEND, smtpService.sendHtmlMail(user, mail, receives));
                }else {
                    client.sendEvent(EventType.SEND, smtpService.sendSimpleMail(user, mail, receives));
                }
            }
        }
    }

    @OnEvent(EventType.RECEIVED)
    private void recevied(SocketIOClient client, String folder, int page) {
        SocketData socketData = socketService.clientsMap.get(client.getSessionId().toString());

        if(socketData != null) {
            User user =  socketData.getUser();

            if(user != null) {
                client.sendEvent(EventType.RECEIVED, imapService.receive(socketData, folder, page));
            }
        }
    }
}
