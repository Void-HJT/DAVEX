package DavexBase.service.TEE;

import lombok.Data;

@Data
public class TEEEncryptAndUploadResponse {
    private String dataKey;
    private boolean uploadSuccess;
    private String partyId;
}
