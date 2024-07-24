package DveAgent.entity;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@EntityScan
@TableName("mpcTask")
public class MpcTask {

  @TableId
  private long uid;
  private long applicationId;
  private long centerId;
  private long mpcId;
  private String parameter;
  private long pn;
  private String host;
  private long port;
  private long data;
  private String protocol;

}
