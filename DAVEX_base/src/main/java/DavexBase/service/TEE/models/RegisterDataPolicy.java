package DavexBase.service.TEE.models;

import lombok.Data;

import java.util.List;

@Data
public class RegisterDataPolicy {
    private String scope;
    private String data_uuid;
    private List<PolicyRule> rules;
}
