package DveCenter.module.task;


import DveAgent.entity.Agent;
import DveAgent.entity.MpcTask;
import DveAgent.entity.MpcTaskAgent;
import DveCenter.entity.Input;
import DveAgent.mapper.MpcTaskAgentMapper;
import DveAgent.mapper.MpcTaskMapper;
import DveCenter.mapper.InputMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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

    public void addAgentToMPCTask(Long mpcTaskId, Long centerId,List<MpcTaskAgent> agents) {
        //检查List的大小 是否与mpcTaskId对应的MpcTask的相等
        // int size = agents.size();
        // if(size != mpcTaskMapper.getMpcTaskById(mpcTaskId).get().getPn()){
        //     throw new RuntimeException("The number of agents is not equal to the number of agents required by the task");
        // }

        //将agents插入到MpcTaskAgent表
        //检测重复 对重复的处理逻辑：直接按照mpcTaskId 和 centerId 把表项全部删掉 然后重新插入
        LambdaQueryWrapper<MpcTaskAgent> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MpcTaskAgent::getMpcTaskId, mpcTaskId)
                .eq(MpcTaskAgent::getCenterId, centerId);
        // 执行删除操作
        mpcTaskAgentMapper.delete(queryWrapper);
        for(MpcTaskAgent agent : agents){
            mpcTaskAgentMapper.insert(agent);
        }

    }


    public MpcTask getMpcTaskById(Long mpcTaskId) {
        return mpcTaskMapper.selectById(mpcTaskId);
    }

    public List<MpcTaskAgent> getAgentsByMpcTaskIdAndCenterId(Long mpcTaskId, Long centerId){
        LambdaQueryWrapper<MpcTaskAgent> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MpcTaskAgent::getMpcTaskId, mpcTaskId)
                .eq(MpcTaskAgent::getCenterId, centerId);
        return mpcTaskAgentMapper.selectList(queryWrapper);
    }

    public MpcTask getTaskByMpcTaskIdAndCenterId(Long mpcTaskId,Long centerId){
        // 创建LambdaQueryWrapper实例
        LambdaQueryWrapper<MpcTask> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MpcTask::getUid, mpcTaskId)
                .eq(MpcTask::getCenterId, centerId);
        return mpcTaskMapper.selectOne(queryWrapper);
    }
}
