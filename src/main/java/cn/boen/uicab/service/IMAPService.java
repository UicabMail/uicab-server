package cn.boen.uicab.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Properties;

@Service
public class IMAPService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${spring.mail.host}")
    private String host;

    @Value("${spring.mail.username}")
    private String username;

    @Value("${spring.mail.password}")
    private String password;

    /**
     * 接收邮件
     */
    public void receive(){
        Properties props = System.getProperties();
        props.setProperty("mail.imap.host",this.host);
        props.setProperty("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.setProperty("mail.imap.socketFactory.fallback", "false");
        props.setProperty("mail.imap.starttls.enable", "true");
        props.setProperty("mail.imap.port", "143");
        props.setProperty("mail.imap.socketFactory.port", "993");
        props.setProperty("mail.store.protocol", "imap");
        Session session = Session.getInstance(props);
        session.setDebug(false);
        URLName urln = new URLName("imap", this.host, 143, null,this.username,this.password);

        try {
            Store store = session.getStore(urln);
            store.connect();
            /**
             *  获得收件箱INBOX
             *  除了收件箱，其他的可以使用以下方法查看到
             *  Folder defaultFolder = store.getDefaultFolder();
             *  Folder[] allFolder = defaultFolder.list();
             *  for(int i = 0; i < allFolder.length; i++) {
             *  		System.out.println(allFolder[i].getName());
             *  }
             *  比如垃圾箱：Junk  已删除：Deleted Messages  草稿：Drafts  已发送：Sent Messages
             */
            Folder folder = store.getFolder("INBOX");
            /**
             * Folder.READ_ONLY：只读权限
             * Folder.READ_WRITE：可读可写（可以修改邮件的状态）
             */
            folder.open(Folder.READ_WRITE);

            System.out.println("未读邮件数: " + folder.getUnreadMessageCount());
            System.out.println("邮件总数: " + folder.getMessageCount());

            // 得到收件箱中的所有邮件,并解析
//        Message[] messages = folder.getMessages();
//        parseMessage(messages);

            //删除邮件第一封邮件
//        deleteMessage(messages[0]);

            folder.close(true);
            store.close();
        } catch (Exception e) {
        }
    }

    /**
     * 解析邮件
     */
    public static void parseMessage(Message... messages) throws MessagingException, IOException {
        if (messages == null || messages.length < 1)
            throw new MessagingException("未找到要解析的邮件!");
        // 解析所有邮件
        int count = messages.length;
        for (int i = count-1 ; i >= 0; i--) {
            MimeMessage msg = (MimeMessage) messages[i];
            System.out.println("------------------解析第" + msg.getMessageNumber() + "封邮件-------------------- ");
            // 如果有编码，可以先解码： MimeUtility.decodeText(msg.getSubject());
            System.out.println("主题: " + msg.getSubject());
            System.out.println("发件人名称: " + getFrom(msg)[0]);
            System.out.println("发件人邮箱: " + getFrom(msg)[1]);
            System.out.println("收件人：" + getReceiveAddress(msg, null));
            System.out.println("发送时间：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(msg.getSentDate()));
            System.out.println("是否已读：" + msg.getFlags().contains(Flags.Flag.SEEN));
            System.out.println("邮件优先级：" + getPriority(msg));
            System.out.println("是否需要回执：" + (msg.getHeader("Disposition-Notification-To") != null ? true : false));
            System.out.println("邮件大小：" + msg.getSize() * 1024 + "kb");
            StringBuffer content = new StringBuffer(100);
            getMailTextContent(msg, content);
            System.out.println("邮件正文：" + content);

            boolean isContainerAttachment = isContainAttachment(msg);
            System.out.println("是否包含附件：" + isContainerAttachment);
            if (isContainerAttachment) {
                saveAttachment(msg, "/Users/dev/Desktop/"); // 保存附件
            }

            System.out.println("------------------第" + msg.getMessageNumber() + "封邮件解析结束-------------------- ");
            System.out.println();

        }
    }

    /**
     * 删除邮件
     */
    public static void deleteMessage(Message... messages) throws MessagingException, IOException {
        if (messages == null || messages.length < 1)
            throw new MessagingException("未找到要解析的邮件!");
        // 解析所有邮件
        for (int i = 0, count = messages.length; i < count; i++) {
            Message message = messages[i];
            String subject = message.getSubject();
            // set the DELETE flag to true
            message.setFlag(Flags.Flag.DELETED, true);
            System.out.println("Marked DELETE for message: " + subject);
        }
    }


    /**
     * 获得邮件发件人
     */
    public static String[] getFrom(MimeMessage msg) throws MessagingException, UnsupportedEncodingException {
        String[] from = new String[2];
        Address[] froms = msg.getFrom();
        if (froms.length < 1)
            throw new MessagingException("没有发件人!");
        InternetAddress address = (InternetAddress) froms[0];
        from[0] = address.getPersonal(); //需要解码的话：MimeUtility.decodeText(address.getPersonal())
        from[1] = address.getAddress();
        return from;
    }

    /**
     * 根据收件人类型，获取邮件收件人、抄送和密送地址。如果收件人类型为空，则获得所有的收件人
     * Message.RecipientType.TO 收件人
     * Message.RecipientType.CC 抄送
     * Message.RecipientType.BCC 密送
     * @return 收件人1 <邮件地址1>, 收件人2 <邮件地址2>, ...
     */
    public static String getReceiveAddress(MimeMessage msg, Message.RecipientType type) throws MessagingException {
        StringBuffer receiveAddress = new StringBuffer();
        Address[] addresss = null;
        if (type == null) {
            addresss = msg.getAllRecipients();
        } else {
            addresss = msg.getRecipients(type);
        }
        if (addresss == null || addresss.length < 1)
            throw new MessagingException("没有收件人!");
        for (Address address : addresss) {
            InternetAddress internetAddress = (InternetAddress) address;
            receiveAddress.append(internetAddress.toUnicodeString()).append(",");
        }
        receiveAddress.deleteCharAt(receiveAddress.length() - 1); // 删除最后一个逗号
        return receiveAddress.toString();
    }


    /**
     * 判断邮件中是否包含附件
     */
    public static boolean isContainAttachment(Part part) throws MessagingException, IOException {
        boolean flag = false;
        if (part.isMimeType("multipart/*")) {
            MimeMultipart multipart = (MimeMultipart) part.getContent();
            int partCount = multipart.getCount();
            for (int i = 0; i < partCount; i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                String disp = bodyPart.getDisposition();
                if (disp != null && (disp.equalsIgnoreCase(Part.ATTACHMENT) || disp.equalsIgnoreCase(Part.INLINE))) {
                    flag = true;
                } else if (bodyPart.isMimeType("multipart/*")) {
                    flag = isContainAttachment(bodyPart);
                } else {
                    String contentType = bodyPart.getContentType();
                    if (contentType.indexOf("application") != -1) {
                        flag = true;
                    }
                    if (contentType.indexOf("name") != -1) {
                        flag = true;
                    }
                }
                if (flag)
                    break;
            }
        } else if (part.isMimeType("message/rfc822")) {
            flag = isContainAttachment((Part) part.getContent());
        }
        return flag;
    }


    /**
     * 获得邮件的优先级
     * @return 1(High):紧急 3:普通(Normal) 5:低(Low)
     */
    public static String getPriority(MimeMessage msg) throws MessagingException {
        String priority = "普通";
        String[] headers = msg.getHeader("X-Priority");
        if (headers != null) {
            String headerPriority = headers[0];
            if (headerPriority.indexOf("1") != -1 || headerPriority.indexOf("High") != -1)
                priority = "紧急";
            else if (headerPriority.indexOf("5") != -1 || headerPriority.indexOf("Low") != -1)
                priority = "低";
            else
                priority = "普通";
        }
        return priority;
    }

    public static void getMailTextContent(Part part, StringBuffer content) throws MessagingException, IOException {
        System.out.println(part.getContentType());
        boolean isContainTextAttach = part.getContentType().indexOf("name") > 0;
        if (part.isMimeType("text/*") && !isContainTextAttach) {
            content.append(part.getContent().toString());
        } else if (part.isMimeType("message/rfc822")) {
            getMailTextContent((Part) part.getContent(), content);
        } else if (part.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) part.getContent();
            int partCount = multipart.getCount();
            for (int i = 0; i < partCount; i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                getMailTextContent(bodyPart, content);
            }
        }
    }

    /**
     * 保存附件
     */
    public static void saveAttachment(Part part, String destDir)
            throws UnsupportedEncodingException, MessagingException, FileNotFoundException, IOException {
        if (part.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) part.getContent(); // 复杂体邮件
            // 复杂体邮件包含多个邮件体
            int partCount = multipart.getCount();
            for (int i = 0; i < partCount; i++) {
                // 获得复杂体邮件中其中一个邮件体
                BodyPart bodyPart = multipart.getBodyPart(i);
                // 某一个邮件体也有可能是由多个邮件体组成的复杂体
                String disp = bodyPart.getDisposition();
                if (disp != null && (disp.equalsIgnoreCase(Part.ATTACHMENT) || disp.equalsIgnoreCase(Part.INLINE))) {
                    InputStream is = bodyPart.getInputStream();
                    saveFile(is, destDir, decodeText(bodyPart.getFileName()));
                } else if (bodyPart.isMimeType("multipart/*")) {
                    saveAttachment(bodyPart, destDir);
                } else {
                    String contentType = bodyPart.getContentType();
                    if (contentType.indexOf("name") != -1 || contentType.indexOf("application") != -1) {
                        saveFile(bodyPart.getInputStream(), destDir, decodeText(bodyPart.getFileName()));
                    }
                }
            }
        } else if (part.isMimeType("message/rfc822")) {
            saveAttachment((Part) part.getContent(), destDir);
        }
    }

    /**
     * 读取输入流中的数据保存至指定目录
     */
    private static void saveFile(InputStream is, String destDir, String fileName)
            throws FileNotFoundException, IOException {
        BufferedInputStream bis = new BufferedInputStream(is);
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(destDir + fileName)));
        int len = -1;
        while ((len = bis.read()) != -1) {
            bos.write(len);
            bos.flush();
        }
        bos.close();
        bis.close();
    }

    /**
     * 文本解码
     */
    public static String decodeText(String encodeText) throws UnsupportedEncodingException {
        if (encodeText == null || "".equals(encodeText)) {
            return "";
        } else {
            return MimeUtility.decodeText(encodeText);
        }
    }

}
