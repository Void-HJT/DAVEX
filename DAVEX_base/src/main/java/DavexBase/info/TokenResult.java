package DavexBase.info;

import lombok.Data;

@Data
public class TokenResult {
    private String accessToken;
    private String refreshToken;

    public TokenResult(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
