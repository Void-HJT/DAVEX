package DavexBase.service.TEE.models;


import lombok.Data;

@Data
public class YamlConfig {
    private String host;
    private String tee_plat;
    private TeeConstraints tee_constraints;
    private String root_ca_file;
    private String private_key_file;
    private String cert_chain_file;

    private CommonConfig common;
    private RegisterDataKeys register_data_keys;
    private GetDataPolicys get_data_policys;
    private RegisterDataPolicy register_data_policy;
    private DeleteDataPolicy delete_data_policy;
    private AddDataRule add_data_rule;
    private DeleteDataRule delete_data_rule;
    private GetExportDataKeyB64 get_export_data_key_b64;
    private DeleteDataKey delete_data_key;
}