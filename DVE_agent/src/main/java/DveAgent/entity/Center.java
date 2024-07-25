package DveAgent.entity;

import java.time.LocalDateTime;
import java.util.Arrays;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class Center {

  @TableId(type = IdType.AUTO)
  private Long uid;

  private String name;
  private String ip;
  private Long port;
  private byte[] crt;
  private LocalDateTime lastUpdated;
  private String description;

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int) (uid ^ (uid >>> 32));
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((ip == null) ? 0 : ip.hashCode());
    result = prime * result + (int) (port ^ (port >>> 32));
    result = prime * result + Arrays.hashCode(crt);
    result = prime * result + ((description == null) ? 0 : description.hashCode());
    return result;
  }

}
