package DavexBase.info;

import lombok.Data;

@Data
public class QueryInfo {
    private String agentId;
    private QueryRequest request;
    private Long databaseId;
    private String applicationId;
}
