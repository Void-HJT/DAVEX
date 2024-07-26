package DveAgent.info;

import java.util.Map;
import org.apache.commons.lang3.tuple.Pair;
import DveAgent.entity.MpcTask;

public class UploadAgentTaskInfo extends MpcTask {
    // (agentID, (part, fileID))
    private Map<Long, Pair<Long, Long>> agentID2fileID;

    public Map<Long, Pair<Long, Long>> getAgentID2fileID() {
        return agentID2fileID;
    }

    public void setAgentID2fileID(Map<Long, Pair<Long, Long>> agentID2fileID) {
        this.agentID2fileID = agentID2fileID;
    }

}
