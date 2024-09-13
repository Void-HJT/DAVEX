package DavexBase.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class RabbitmqConnection {
    @TableId
    private String centerId;
    private String host;
    private int port;
    private String username;
    private String password;
    private String virtualHost;
}
