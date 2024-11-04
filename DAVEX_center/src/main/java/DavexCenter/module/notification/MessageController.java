package DavexCenter.module.notification;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import DavexBase.entity.Notification;
import DavexBase.service.notification.NotificationService;

@Controller
@RestController("/message")
public class MessageController {
    @Autowired
    NotificationService notificationService;

    @GetMapping("/get")
    public List<Notification> get(@RequestParam String appID) {
        return notificationService.getNotification(appID);
    }

    @GetMapping("/unread")
    public Boolean unread(@RequestParam String appID) {
        return notificationService.unreadNotification(appID);
    }

    @GetMapping("/read")
    public void read(@RequestParam Long uid) {
        notificationService.read(uid);
    }

}
