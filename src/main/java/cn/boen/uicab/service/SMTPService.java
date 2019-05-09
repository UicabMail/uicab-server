package cn.boen.uicab.service;

import cn.boen.uicab.entity.Mail;
import cn.boen.uicab.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.util.Properties;

@Service
public class SMTPService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.host}")
    private String host;

    private Properties properties;

    SMTPService(){
        Properties properties = new Properties();
        properties.setProperty("mail.transport.protocol", "smtp");
        properties.setProperty("mail.smtp.port", "587");
        properties.setProperty("mail.smtp.auth", "true");
        properties.setProperty("mail.smtp.starttls.enable", "true");
        properties.setProperty("mail.smtp.starttls.required", "true");

        this.properties = properties;
    }


    public boolean sendSimpleMail(User user, Mail mail, String[] receives) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setUsername(user.getMail());
        mailSender.setPassword(user.getPassword());
        mailSender.setJavaMailProperties(properties);

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(user.getMail());
        message.setTo(receives);
        message.setSubject(mail.getSubject());
        message.setText(mail.getContent());

        try {
            mailSender.send(message);
            logger.info("简单邮件已经发送。");
            return true;
        } catch (Exception e) {
            logger.error("发送简单邮件时发生异常！", e);
            return false;
        }
    }

    public boolean sendHtmlMail(User user, Mail mail, String[] receives)  {
        try {
            JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
            mailSender.setHost(host);
            mailSender.setUsername(user.getMail());
            mailSender.setPassword(user.getPassword());
            mailSender.setJavaMailProperties(properties);
            mailSender.setDefaultEncoding("UTF-8");

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(user.getMail());
            helper.setTo(receives);
            helper.setSubject(mail.getSubject());
            helper.setText(mail.getContent(), true);

            mailSender.send(message);
            logger.info("HTML邮件已经发送。");
            return true;
        } catch (Exception e) {
            logger.error("发送HTML邮件时发生异常！", e);
            return false;
        }
    }

    public void sendAttachmentsMail(String to, String subject, String content, String filePath){
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom("");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);

            FileSystemResource file = new FileSystemResource(new File(filePath));
            String fileName = filePath.substring(filePath.lastIndexOf(File.separator));
            helper.addAttachment(fileName, file);

            // 添加多个附件可以使用多条 helper.addAttachment(fileName, file)
            mailSender.send(message);
            logger.info("带附件的邮件已经发送。");
        } catch (MessagingException e) {
            logger.error("发送带附件的邮件时发生异常！", e);
        }
    }

    public void sendInlineResourceMail(String to, String subject, String content, String rscPath, String rscId){
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom("");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);

            FileSystemResource res = new FileSystemResource(new File(rscPath));
            helper.addInline(rscId, res);

            mailSender.send(message);
            logger.info("嵌入静态资源的邮件已经发送。");
        } catch (MessagingException e) {
            logger.error("发送嵌入静态资源的邮件时发生异常！", e);
        }
    }
}