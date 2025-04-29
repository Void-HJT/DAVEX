package DavexBase.service.TEE.models;


import lombok.Data;

@Data
public class GetExportDataKeyB64 {
    private String party_id;
    private String resource_uri;
    private String data_export_certificate_file;
}
