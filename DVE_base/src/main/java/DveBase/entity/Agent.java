package DveBase.entity;

import java.time.LocalDateTime;
import java.util.Arrays;

import com.baomidou.mybatisplus.annotation.TableId;

import lombok.Data;

@Data
public class Agent {

  @TableId
  private Long uid;
  private String name;
  private String ip;
  private int port;
  private byte[] crt;
  private LocalDateTime lastUpdated;
  private String description;

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Agent other = (Agent) obj;
    if (uid != other.uid)
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (ip == null) {
      if (other.ip != null)
        return false;
    } else if (!ip.equals(other.ip))
      return false;
    if (port != other.port)
      return false;
    if (!Arrays.equals(crt, other.crt))
      return false;
    if (description == null) {
      if (other.description != null)
        return false;
    } else if (!description.equals(other.description))
      return false;
    return true;
  }

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
