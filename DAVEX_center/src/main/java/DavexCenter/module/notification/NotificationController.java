package DavexCenter.module.notification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import DavexBase.entity.Notification;
import DavexBase.service.notification.NotificationService;

@RestController
@RequestMapping("notification")
public class NotificationController {
    @Autowired
    NotificationService notificationService;

    @GetMapping("/list")
    public Page<Notification> list(@RequestParam String appID, @RequestParam Integer page, @RequestParam Integer size) {
        return notificationService.getNotification(appID, page, size);
    }

    @GetMapping("/unread")
    public Boolean unread(@RequestParam String appID) {
        return notificationService.unreadNotification(appID);
    }

    @GetMapping("/read")
    public String read(@RequestParam Long uid) {
        try {
            notificationService.read(uid);
        } catch (Exception e) {
            return e.getMessage();
        }
        return "success";
    }

    @GetMapping("/listUnread")
    public Page<Notification> listUnread(@RequestParam String appID, @RequestParam Integer page, @RequestParam Integer size) {
        return notificationService.getUnreadNotification(appID, page, size);
    }

    @PostMapping("/set")
    public String set(@RequestParam String appID, @RequestParam String title, @RequestParam String content,
                      @RequestParam String taskID, @RequestParam Integer code, @RequestParam String type) {
        try {
            notificationService.setMessage(appID, title, content, taskID, code, type);
        } catch (Exception e) {
            return e.getMessage();
        }
        return "success";
    }

    // @GetMapping("test")
    // public void test(@RequestParam String param) {
    // notificationService.setMessage("DAVEX-C1-AXX1", param, param);
    // }

}
