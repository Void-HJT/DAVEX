package DavexBase.service.TEE.models;

import lombok.Data;

import java.util.List;

@Data
public class OpConstraint {
    private String op_name;
    private List<String> constraints;
}
