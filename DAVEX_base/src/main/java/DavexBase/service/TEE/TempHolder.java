package DavexBase.service.TEE;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class TempHolder {
    private String agentPartyId;
    private String centerPartyId;

    private String agentEncPath;
    private String centerEncPath;

    private String voteRequestSignature;
    private String voterFilePath;

    // 设置 partyId，根据 subject 动态决定
    public void setPartyId(String partyId, String subject) {
        if ("agent".equalsIgnoreCase(subject)) {
            setAgentPartyId(partyId);
        } else if ("center".equalsIgnoreCase(subject)) {
            setCenterPartyId(partyId);
        }
    }

    public String getPartyId(String subject) {
        if ("agent".equalsIgnoreCase(subject)) {
            return agentPartyId;
        } else if ("center".equalsIgnoreCase(subject)) {
            return centerPartyId;
        }
        return "";
    }

    public void setEncPath(String encPath, String subject) {
        if ("agent".equalsIgnoreCase(subject)) {
            setAgentEncPath(encPath);
        } else if ("center".equalsIgnoreCase(subject)) {
            setCenterEncPath(encPath);
        }
    }
}
