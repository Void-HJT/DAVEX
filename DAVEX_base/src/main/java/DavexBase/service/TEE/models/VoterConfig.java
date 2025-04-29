package DavexBase.service.TEE.models;

import lombok.Data;

import java.util.List;

@Data
public class VoterConfig {
    private String vote_request_signature;
    private String action;
    private List<String> cert_chain_file;
    private String private_key_file;
}
