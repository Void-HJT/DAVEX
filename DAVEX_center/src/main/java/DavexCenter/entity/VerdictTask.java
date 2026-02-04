package DavexCenter.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import lombok.Data;

@Data
public class VerdictTask {

    @TableId(type = IdType.AUTO)
    private Long uid;
    private String agentId;
    private String applicationId;
    private java.sql.Timestamp startTime;
    private java.sql.Timestamp filterStart;
    private java.sql.Timestamp filterEnd;
    private String filterType;
    private String filterDistrict;
    private String filterCause;
    private String status;
    private String remark;
    private String resultIds;
    private Integer hasRead;
}
