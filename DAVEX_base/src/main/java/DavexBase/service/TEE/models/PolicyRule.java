package DavexBase.service.TEE.models;

import lombok.Data;

import java.util.List;

@Data
public class PolicyRule {
    private String rule_id;
    private List<String> grantee_party_ids;
    private List<String> columns;
    private List<String> global_constraints;
    private List<OpConstraint> op_constraints;
}
