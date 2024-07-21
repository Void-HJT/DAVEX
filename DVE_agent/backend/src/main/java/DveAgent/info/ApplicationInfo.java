package DveAgent.info;

import DveAgent.entity.Application;
import DveAgent.entity.Group;
import lombok.Data;

import java.util.List;

@Data
public class ApplicationInfo extends Application {
    private long agentId;
    private long groupId;
    private String groupName;
}
