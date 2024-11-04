package DavexBase.service.notification;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import DavexBase.entity.Notification;
import DavexBase.mapper.NotificationMapper;

@Service
public class NotificationService {
    @Autowired
    NotificationMapper notificationMapper;

    public List<Notification> getNotification(String appID) {
        LambdaQueryWrapper<Notification> queryWrapper = Wrappers.<Notification>lambdaQuery().eq(Notification::getAppID,
                appID);
        return notificationMapper.selectList(queryWrapper);
    }

    public Boolean unreadNotification(String appID) {
        LambdaQueryWrapper<Notification> queryWrapper = Wrappers.<Notification>lambdaQuery()
                .eq(Notification::getAppID, appID)
                .eq(Notification::getHasRead, false);
        return notificationMapper.selectList(queryWrapper).size() != 0;
    }

    public void setMessage(String appID, String topic, String m) {
        Notification message = new Notification();
        message.setAppID(appID);
        message.setTopic(topic);
        message.setNote(m);
        message.setHasRead(false);
        message.setTime(new java.sql.Timestamp(System.currentTimeMillis()));
        notificationMapper.insert(message);
    }

    public void read(Long uid) {
        Notification message = new Notification();
        message.setUid(uid);
        message.setHasRead(true);
        notificationMapper.updateById(message);
    }

}
