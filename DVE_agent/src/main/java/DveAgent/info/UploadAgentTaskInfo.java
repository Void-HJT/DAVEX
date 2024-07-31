package DveAgent.info;

import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import DveAgent.entity.MpcTask;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UploadAgentTaskInfo extends MpcTask {
    // (agentID, (part, fileID))
    private Map<Long, Pair<Long, Long>> agentID2fileID;

    public Map<Long, Pair<Long, Long>> getAgentID2fileID() {
        return agentID2fileID;
    }

    public void setAgentID2fileID(Map<Long, Pair<Long, Long>> agentID2fileID) {
        this.agentID2fileID = agentID2fileID;
    }

    public void setNull() {
        for (Map.Entry<Long, Pair<Long, Long>> entry : agentID2fileID.entrySet()) {
            Pair<Long, Long> originalPair = entry.getValue();
            Pair<Long, Long> updatedPair = new ImmutablePair<Long, Long>(originalPair.getLeft(), 0L);
            entry.setValue(updatedPair);
        }
    }
}