package DavexBase.service.TEE.models;

import lombok.Data;

import java.util.List;

@Data
public class VoteRequest {
    private String type;
    private Integer approved_threshold;
    private String approved_action;
    private List<String> cert_chain_file;
    private String private_key_file;
}
