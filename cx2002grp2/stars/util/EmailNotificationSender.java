package cx2002grp2.stars.util;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import cx2002grp2.stars.NotificationSender;
import cx2002grp2.stars.data.dataitem.User;

public class EmailNotificationSender implements NotificationSender {

    @Override
    public void sendNotification(User targetUser, String msg) {
        throw new UnsupportedOperationException();
        // final String username = " "; // to be added
        // final String password = " "; // to be added

        // Properties props = new Properties();
        // props.put("mail.smtp.auth", "true");
        // props.put("mail.smtp.starttls.enable", "true");
        // props.put("mail.smtp.host", "smtp.gmail.com");
        // props.put("mail.smtp.port", "587");

        // Session session = Session.getInstance(props, new javax.mail.Authenticator() {
        //     protected PasswordAuthentication getPasswordAuthentication() {
        //         return new PasswordAuthentication(username, password);
        //     }
        // });

        // try {

        //     Message message = new MimeMessage(session);
        //     message.setFrom(new InternetAddress("from-email@gmail.com"));
        //     message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(" ")); // to be added an email addr
        //     message.setSubject("Testing Subject");
        //     message.setText("Dear Mail Crawler," + "\n\n No spam to my email, please!");

        //     Transport.send(message);

        //     System.out.println("Done");

        // } catch (MessagingException e) {
        //     throw new RuntimeException(e);
        // }
    }

}