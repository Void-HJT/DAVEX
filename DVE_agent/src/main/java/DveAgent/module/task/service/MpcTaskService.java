
package DveAgent.module.task.service;

import com.baomidou.mybatisplus.extension.service.IService;
import DveAgent.entity.MpcTask;
import DveAgent.entity.MpcTaskAgent;
import DveAgent.mapper.MpcTaskAgentMapper;
import DveAgent.mapper.MpcTaskMapper;
import DveAgent.entity.Agent;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@Service
public class MpcTaskService {

    @Autowired
    private MpcTaskMapper mpcTaskMapper;

    @Autowired
    private MpcTaskAgentMapper mpcTaskAgentMapper;

    public MpcTask createMpcTask(MpcTask mpcTask) {
        mpcTaskMapper.insert(mpcTask);
        return mpcTask;
    }

    public void addAgentToMPCTask(Long mpcTaskId, List<MpcTaskAgent> agents) {
        //检查List的大小 是否与mpcTaskId对应的MpcTask的相等
        // int size = agents.size();
        // if(size != mpcTaskMapper.getMpcTaskById(mpcTaskId).get().getPn()){
        //     throw new RuntimeException("The number of agents is not equal to the number of agents required by the task");
        // }

        //将agents插入到MpcTaskAgent表中
        //晚点补几个插入的逻辑
        //检测id
        //检测size
        //检测重复
        for(MpcTaskAgent agent : agents){
            mpcTaskAgentMapper.insert(agent);
        }

    }
}