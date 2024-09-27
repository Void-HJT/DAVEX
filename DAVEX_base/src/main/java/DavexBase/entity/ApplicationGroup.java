package DavexBase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import lombok.Data;

@Data
public class ApplicationGroup {

    @TableId(type = IdType.AUTO)
    private Long uid;
    private String agentId;
    private String centerId;
    private String applicationId;
    private Long groupId;

}
