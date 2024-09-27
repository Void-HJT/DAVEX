package DavexBase.entity;

import org.springframework.boot.autoconfigure.domain.EntityScan;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;

import lombok.Data;

@Data
@EntityScan
@TableName(value = "file", autoResultMap = true)
public class File {

    @TableId
    private String uid;

    private String agentId;
    private String folderId;

    private String name;
    private java.sql.Timestamp createDate;
    private java.sql.Timestamp lastUpdate;
    @TableField(typeHandler = FastjsonTypeHandler.class)
    private JSONObject attribute;
    private Long size;
    private String description;
    private java.sql.Timestamp expiredTime;
    private String hash;
    private String example;
    private String type;

}
