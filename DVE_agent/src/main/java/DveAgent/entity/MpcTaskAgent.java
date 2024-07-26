package DveAgent.entity;

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
  // 去数据库里修改表名
  @TableField("mpcTask_id")
  private String mpcTaskId;
  private Long centerId;
  private Long agentId;
  private Long part;
  private Long fileId;

}
