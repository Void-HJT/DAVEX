package DavexBase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("mpcTaskOutput")
public class MpcTaskOutput {
    @TableId(type = IdType.AUTO)
    private Long uid;
    private String taskId;
    private String hash;
    private String path;
    private java.sql.Timestamp uploadDate;
    private String applicationId;
    private java.sql.Timestamp expiredTime;
    private String name;
}
