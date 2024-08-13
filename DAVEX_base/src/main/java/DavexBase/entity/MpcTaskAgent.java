package DavexBase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("mpcTask_agent")
public class MpcTaskAgent {

  @TableId(type = IdType.AUTO)
  private Long uid;
  @TableField("mpcTask_id")
  private String mpcTaskId;
  private Long centerId;
  private Long agentId;
  private Long part;

}
