package DavexBase.service.notification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import DavexBase.entity.Notification;
import DavexBase.mapper.NotificationMapper;

@Service
public class NotificationService {
    @Autowired
    NotificationMapper notificationMapper;

    public Page<Notification> getNotification(String appID, Integer page, Integer size) {
        Page<Notification> rowPage = new Page<>(page, size);
        rowPage.addOrder(OrderItem.desc("time"));
        LambdaQueryWrapper<Notification> queryWrapper = Wrappers.<Notification>lambdaQuery().eq(Notification::getAppID,
                appID);
        return notificationMapper.selectPage(rowPage, queryWrapper);
    }

    public Boolean unreadNotification(String appID) {
        LambdaQueryWrapper<Notification> queryWrapper = Wrappers.<Notification>lambdaQuery()
                .eq(Notification::getAppID, appID)
                .eq(Notification::getHasRead, false);
        return notificationMapper.selectList(queryWrapper).size() != 0;
    }

    public void setMessage(String appID, String title, String content, String taskID, Integer code, String type) {
        // 默认值重载方法
        setMessage(appID, title, content, taskID, code, type, false);
    }

    public void setMessage(String appID, String title, String content, String taskID, Integer code, String type, Boolean hasRead) {
        LambdaQueryWrapper<Notification> queryWrapper = Wrappers.<Notification>lambdaQuery()
                .eq(Notification::getTaskID, taskID)
                .eq(Notification::getCode, code);
        if (notificationMapper.selectCount(queryWrapper) > 0) {
            return;
        }

        Notification message = new Notification();
        message.setAppID(appID);
        message.setTitle(title);
        message.setContent(content);
        message.setHasRead(hasRead);
        message.setTime(new java.sql.Timestamp(System.currentTimeMillis()));
        message.setTaskID(taskID);
        message.setCode(code);
        message.setType(type);
        notificationMapper.insert(message);
    }

    public void read(Long uid)throws Exception {
        Notification message = notificationMapper.selectById(uid);
        message.setUid(uid);
        message.setHasRead(true);
        notificationMapper.updateById(message);
    }

    public Page<Notification> getUnreadNotification(String appID, Integer page, Integer size) {
        Page<Notification> rowPage = new Page<>(page, size);
        rowPage.addOrder(OrderItem.desc("time"));
        LambdaQueryWrapper<Notification> queryWrapper = Wrappers.<Notification>lambdaQuery()
                .eq(Notification::getAppID, appID)
                .eq(Notification::getHasRead, false);
        return notificationMapper.selectPage(rowPage, queryWrapper);
    }

}
