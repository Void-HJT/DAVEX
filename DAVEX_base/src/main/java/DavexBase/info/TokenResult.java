package DavexBase.info;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class TokenResult {
    private String targetId;
    private String accessToken;
    private String refreshToken;
    private java.sql.Timestamp updateTime;

    public TokenResult(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public TokenResult(String targetId, String accessToken, String refreshToken, Timestamp updateTime) {
        this.targetId = targetId;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.updateTime = updateTime;
    }
}
