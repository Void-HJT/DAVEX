package DavexBase.info;

import java.util.List;

import lombok.Data;

@Data
public class ComparisonInfo {
    private String applicationId;
    private String agentId;
    private String fileId;
    private String folderId;
    private List<String> attributes;
    private List<List<String>> valuesList;
}
