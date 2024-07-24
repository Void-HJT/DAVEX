package DveAgent.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class Application {

  @TableId
  private Long uid;
  private long centerId;
  private String name;
  private byte[] crt;
  private LocalDateTime lastUpdated;
  private String description;

}
