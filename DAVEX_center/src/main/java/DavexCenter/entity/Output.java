package DavexCenter.entity;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import lombok.Data;

@Data
public class Output {

    @TableId(type = IdType.AUTO)
    private Long uid;
    private String name;
    private String type;
    private java.sql.Timestamp uploadDate;
    private JSONObject attribute;
    private long size;
    private String description;
    private String path;
    private java.sql.Timestamp expiredTime;
    private String hash;
    private String fileId;
    private String agentId;
    private String applicationId;
    private String tag;
}
