package DavexBase.entity;


import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Keycloak {

    @TableId
    private String authenticationId;

    private String serverUrl;
    private String realm;
    private String clientId;
    private String clientSecret;

}
