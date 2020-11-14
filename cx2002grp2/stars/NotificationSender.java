package cx2002grp2.stars;

import cx2002grp2.stars.data.dataitem.Registration;
import cx2002grp2.stars.data.dataitem.User;

public interface NotificationSender {
    /**
     * Send the given text message to the given user.
     * 
     * @param targetUser the target user to send the notification.
     * @param msg        the notification to be sent
     */
    void sendNotification(User targetUser, String msg);
    void sendWaitlistNotification(Registration reg, String msg);
}
