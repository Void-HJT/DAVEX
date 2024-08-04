package DveCommon.info;

import DveAgent.entity.Application;
import lombok.Data;

@Data
public class ApplicationInfo extends Application {
    private Long agentId;
    private Long groupId;
    private String groupName;
}
