package cn.uicab.service;

import org.springframework.beans.factory.annotation.Value;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import java.util.Properties;

public class ReceiverService {

    @Value("${spring.mail.host}")
    private String host;

    @Value("${spring.mail.username}")
    private String username;

    @Value("${spring.mail.password}")
    private String password;


   public void init () throws MessagingException {
       Properties props = new Properties();
       props.setProperty("mail.imaps.host", "imap.gmail.com");
       props.setProperty("mail.imaps.port", "993");
       props.setProperty("mail.imaps.connectiontimeout", "5000");
       props.setProperty("mail.imaps.timeout", "5000");
       Session session=Session.getInstance(props);
       //从会话对象中获得POP3协议的Store对象
       Store store = null;
           store = session.getStore("imaps");
//如果需要查看接收邮件的详细信息，需要设置Debug标志
//连接邮件服务器
       store.connect(host, username, password);
       //获取邮件服务器的收件箱
       Folder folder = store.getFolder("INBOX");
//以只读权限打开收件箱
       folder.open(Folder.READ_ONLY);
   }
}
