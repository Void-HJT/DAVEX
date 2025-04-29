package DavexBase.service.TEE.models;

import lombok.Data;

import java.util.List;

@Data
public class CommonConfig {
    private String party_id;
    private List<String> cert_pems_file;
    private String scheme = "RSA"; // 默认是 RSA
    private String private_key_file;
}
