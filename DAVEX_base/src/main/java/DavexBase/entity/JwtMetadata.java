package DavexBase.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class JwtMetadata {
    @TableId
    private String uid;          // JWT唯一标识
    private String agentUid;     // 关联的代理UID
    private LocalDateTime issuedTime;  // 签发时间
    private LocalDateTime expiresTime; // 过期时间
    private boolean revoked;    // 是否吊销
}
