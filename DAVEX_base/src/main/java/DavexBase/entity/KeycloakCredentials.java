package DavexBase.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class KeycloakCredentials {

    @TableId
    private String targetId;

    private String publicKey;
    private java.sql.Timestamp expiredTime;
}
