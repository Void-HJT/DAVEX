package DveAgent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@EntityScan
@TableName("mpcTask")
public class MpcTask {

  @TableId(type = IdType.AUTO)
  private Long uid;
  private Long applicationId;
  private Long centerId;
  private Long mpcId;
  private String parameter;
  private Long pn;
  private String host;
  private Long port;
  private Long data;
  private String protocol;

}
