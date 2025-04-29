package DavexBase.service.TEE.models;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DataKey {
    private String resource_uri;
    private String data_key_b64;

    public DataKey(String resourceUri, String dataKeyB64) {
        this.resource_uri = resourceUri;
        this.data_key_b64 = dataKeyB64;
    }
}
