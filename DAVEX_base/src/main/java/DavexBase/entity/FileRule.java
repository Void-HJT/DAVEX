package DavexBase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class FileRule {

  @TableId(type = IdType.AUTO)
  private Long uid;
  private Long agentId;
  private Long fileId;
  private Long ruleId;

}
