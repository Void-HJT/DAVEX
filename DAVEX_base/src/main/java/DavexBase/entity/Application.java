package DavexBase.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import lombok.Data;

@Data
public class Application {

  @TableId
  private String uid;

  private String centerId;
  private String name;
  private byte[] crt;
  private LocalDateTime lastUpdated;
  private String description;

}
