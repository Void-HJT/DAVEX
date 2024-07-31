package DveCenter.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import lombok.Data;

@Data
public class Task {

    @TableId(type = IdType.AUTO)
    private Long uid;
    private Long fileId;
    private Long agentId;
    private Long applicationId;
    private Long outputId;
    private java.sql.Timestamp downloadTime;
}
