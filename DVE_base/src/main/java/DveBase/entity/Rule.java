package DveBase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class Rule {

  @TableId(type = IdType.AUTO)
  private Long uid;

  private Long agentId;
  private Long groupId;
  private String allowedMethod;

}
