package DavexBase.service.TEE.models;

import lombok.Data;

@Data
public class DeleteDataRule {
    private String scope;
    private String data_uuid;
    private String rule_id;
}
