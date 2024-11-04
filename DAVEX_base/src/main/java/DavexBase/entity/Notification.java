package DavexBase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import lombok.Data;

@Data
public class Notification {
    @TableId(type = IdType.AUTO)
    private Long uid;
    private String appID;
    private String topic;
    private String note;
    private java.sql.Timestamp time;
    private Boolean hasRead;
}
