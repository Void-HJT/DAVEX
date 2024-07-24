package DveAgent.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class Mpc {

  @TableId
  private Long uid;
  private long centerId;
  private long path;
  private long status;

}
