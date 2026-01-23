package DavexCenter.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import lombok.Data;

@Data
public class OperationHistory {

    @TableId(type = IdType.AUTO)
    private Long uid;
    private String agentId;
    private String applicationId;
    private java.sql.Timestamp time;
    private String operationType;
    private String operationObject;
    private String result;
    private String remark;
}
