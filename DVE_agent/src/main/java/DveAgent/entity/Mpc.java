package DveAgent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class Mpc {

  @TableId(type = IdType.AUTO)
  private Long uid;

  private Long centerId;
  private Long path;
  private Long status;

}
