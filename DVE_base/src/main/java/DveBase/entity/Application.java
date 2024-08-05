package DveBase.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import lombok.Data;

@Data
public class Application {

  @TableId(type = IdType.AUTO)
  private Long uid;
  private Long centerId;
  private String name;
  private byte[] crt;
  private LocalDateTime lastUpdated;
  private String description;

}
