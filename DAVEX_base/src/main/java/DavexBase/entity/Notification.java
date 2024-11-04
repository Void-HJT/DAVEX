package DavexBase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;

import lombok.Data;

@Data
public class Notification {
    @TableId(type = IdType.AUTO)
    private Long uid;
    @TableField("appID")
    private String appID;
    private String title;
    private String content;
    private java.sql.Timestamp time;
    @TableField("hasRead")
    private Boolean hasRead;
}
