package cn.boen.uicab.service;

import cn.boen.uicab.entity.Mail;
import cn.boen.uicab.entity.SocketData;
import cn.boen.uicab.entity.User;
import com.sun.mail.imap.IMAPFolder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Service
public class IMAPService {

    @Value("${spring.mail.host}")
    private String host;

    private Properties properties;

    private Session imapSession;

    IMAPService(){
        Properties properties = new Properties();

        properties.setProperty("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.setProperty("mail.imap.socketFactory.fallback", "false");
        properties.setProperty("mail.imap.starttls.enable", "true");
        properties.setProperty("mail.imap.port", "143");
        properties.setProperty("mail.imap.socketFactory.port", "993");
        properties.setProperty("mail.store.protocol", "imap");

        this.properties = properties;
    }

    private  Session getImapSession(){

        if(imapSession == null) {
            properties.setProperty("mail.imap.host",host);

            Session session = Session.getInstance(properties);
            session.setDebug(false);

            imapSession = session;
        }

        return imapSession;
    }

    public List<Mail> receive(SocketData socketData, String folderName, int page){

        try {
            User user =  socketData.getUser();
            Store store = socketData.getImapStore();

            if(store == null || !store.isConnected()) {
                URLName urlName = new URLName("imap", host, 143, null,user.getMail(),user.getPassword());
                store =  getImapSession().getStore(urlName);
                store.connect();

                socketData.setImapStore(store);
            }

//            Map<Folder, Message[]> map =  socketData.getSource().get(folderName);

//            if(map == null) {
//
//            }
//               比如垃圾箱：Junk  已删除：Deleted Messages  草稿：Drafts  已发送：Sent INBOX


            Folder folder = store.getFolder(folderName);

            folder.open(Folder.READ_WRITE);

            if(!folder.exists()){
                return null;
            };

            int start = (page - 1) * 50 + 1 ;
            int end = Math.min(start + 50, folder.getMessageCount());

            Message[] messages = folder.getMessages(start , end);

            FetchProfile fp = new FetchProfile();
            fp.add(FetchProfile.Item.ENVELOPE);
            fp.add(IMAPFolder.FetchProfileItem.FLAGS);
            fp.add(IMAPFolder.FetchProfileItem.CONTENT_INFO);
            fp.add("X-mailer");

            folder.fetch(messages, fp);


            List<Mail> mails= parseMessage(messages);
            folder.close(true);

            return  mails;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return  null;
        }
    }

    public static List<Mail> parseMessage(Message... messages) throws MessagingException, IOException {
        long t = System.currentTimeMillis();

        List mails = new ArrayList<Mail>();

        if (messages == null || messages.length < 1)
            throw new MessagingException("未找到要解析的邮件!");
        for (Message msg: messages) {
            Mail mail = new Mail();
            mail.setSubject(msg.getSubject());
            String [] fromInfo = getFrom(msg);
            mail.setFromName(fromInfo[0]);
            mail.setFrom(fromInfo[1]);
            mail.setTo(getReceiveAddress(msg, null));
            mail.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(msg.getSentDate()));
            mail.setSeen(msg.getFlags().contains(Flags.Flag.SEEN));
//            System.out.println("邮件优先级：" + getPriority(msg));
//            System.out.println("是否需要回执：" + (msg.getHeader("Disposition-Notification-To") != null ? true : false));
//            System.out.println("邮件大小：" + msg.getSize() * 1024 + "kb");
            StringBuffer content = new StringBuffer(100);
            getMailTextContent(msg, content);
            mail.setContent(content.toString());
            mail.setContentType(msg.getContentType());

//            boolean isContainerAttachment = isContainAttachment(msg);
//            System.out.println("是否包含附件：" + isContainerAttachment);
//            if (isContainerAttachment) {
//                saveAttachment(msg, "/Users/dev/Desktop/"); // 保存附件
//            }
            mails.add(mail);
        }

        System.out.println(System.currentTimeMillis() - t);

        return mails;
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
    public static String[] getFrom(Message msg) throws MessagingException, UnsupportedEncodingException {
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
    public static String getReceiveAddress(Message msg, Message.RecipientType type) throws MessagingException {
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
    public static String getPriority(Message msg) throws MessagingException {
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
//        System.out.println(part.getContentType());
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
