package DavexBase.service.TEE.models;

import lombok.Data;

@Data
public class AddDataRule {
    private String scope;
    private String data_uuid;
    private Rule rule;
}
