package DavexCenter.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import lombok.Data;

@Data
public class ComparisonOutput {
    @TableId(type = IdType.AUTO)
    private Long uid;
    private String hash;
    private String path;
    private java.sql.Timestamp uploadDate;
    private String applicationId;
    private java.sql.Timestamp expiredTime;
    private String name;
    private String agentId;
    private String fileId;
    private String folderId;
    private String destName;

}
