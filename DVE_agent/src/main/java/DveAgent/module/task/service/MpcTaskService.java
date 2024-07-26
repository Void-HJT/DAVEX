
package DveAgent.module.task.service;

import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import DveAgent.common.My;
import DveAgent.entity.MpcTaskAgent;
import DveAgent.info.UploadAgentTaskInfo;
import DveAgent.mapper.MpcTaskAgentMapper;
import DveAgent.mapper.MpcTaskMapper;

@Service
public class MpcTaskService {

    @Autowired
    private MpcTaskMapper mpcTaskMapper;

    @Autowired
    private MpcTaskAgentMapper mpcTaskAgentMapper;

    @Autowired
    private My my;

    // TODO 检查File权限
    public void createMpcTask(UploadAgentTaskInfo mpctTaskInfo) throws Exception {
        if (mpcTaskMapper.selectById(mpctTaskInfo.getUid()) != null) {
            throw new Exception("任务已存在");
        }
        Map<Long, Pair<Long, Long>> map = mpctTaskInfo.getAgentID2fileID();
        if (!map.containsKey(my.getId())) {
            throw new Exception("发送错误");
        }
        mpctTaskInfo.setData(map.get(my.getId()).getRight());
        for (Map.Entry<Long, Pair<Long, Long>> entry : map.entrySet()) {
            MpcTaskAgent mpcTaskAgent = new MpcTaskAgent();
            mpcTaskAgent.setAgentId(entry.getKey());
            mpcTaskAgent.setFileId(entry.getValue().getRight());
            mpcTaskAgent.setPart(entry.getValue().getLeft());
            mpcTaskAgent.setMpcTaskId(mpctTaskInfo.getUid());
            mpcTaskAgent.setCenterId(mpctTaskInfo.getCenterId());
            mpcTaskAgentMapper.insert(mpcTaskAgent);
        }
        mpcTaskMapper.insert(mpctTaskInfo);
    }

    public Boolean ready(Long mpcTaskId) throws Exception {
        return mpcTaskMapper.selectById(mpcTaskId).getReady();
    }
}