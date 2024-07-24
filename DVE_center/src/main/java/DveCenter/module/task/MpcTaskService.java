package DveCenter.module.task;


import DveAgent.entity.MpcTask;
import DveAgent.entity.MpcTaskAgent;
import DveCenter.entity.Input;
import DveAgent.mapper.MpcTaskAgentMapper;
import DveAgent.mapper.MpcTaskMapper;
import DveCenter.mapper.InputMapper;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
@Service
public class MpcTaskService {

    @Autowired
    private MpcTaskMapper mpcTaskMapper;

    @Autowired
    private MpcTaskAgentMapper mpcTaskAgentMapper;

    @Autowired
    private InputMapper inputMapper;

    public MpcTask createMpcTask(MpcTask mpcTask) {
        mpcTaskMapper.insert(mpcTask);
        return mpcTask;
    }

    public Input createInput (Input input) {
        inputMapper.insert(input);
        return input;
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


    public MpcTask getMpcTaskById(Long mpcTaskId) {
        return mpcTaskMapper.selectById(mpcTaskId);
    }
}
