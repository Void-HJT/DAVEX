package DavexAgent.module.task.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import DavexBase.common.Body;
import DavexBase.entity.File;
import DavexBase.entity.Mpc;
import DavexBase.entity.MpcTaskAgent;
import DavexBase.entity.MpcTask;
import DavexBase.mapper.FileMapper;
import DavexBase.mapper.MpcMapper;
import DavexBase.mapper.MpcTaskAgentMapper;
import DavexBase.mapper.MpcTaskMapper;
import DavexBase.task.command.MpcTaskCommand;
import DavexBase.task.command.ParticipantInput;

import DavexAgent.module.task.assembler.MpcTaskCommandAssembler;

@Service
public class SecureInferenceService {

    @Autowired
    private FileMapper fileMapper;
    @Autowired
    private MpcMapper mpcMapper;
    @Autowired
    private MpcTaskService mpcTaskService;
    @Autowired
    private MpcTaskMapper mpcTaskMapper;
    @Autowired
    private MpcTaskAgentMapper mpcTaskAgentMapper;

    public Body<String> setMpc(File file, String mpcID) {
        if (mpcMapper.selectById(mpcID) == null) {
            return Body.error("MPC not found");
        }
        if (file.getAttribute() == null) {
            file.setAttribute(new JSONObject());
        }
        file.getAttribute().put("mpcID", mpcID);
        fileMapper.updateById(file);
        return null;
    }

    public Mpc getMpc(File file) throws Exception {
        if (file.getAttribute() == null) {
            throw new Exception("MPC not found");
        }
        String mpcID;
        try {
            mpcID = file.getAttribute().getString("mpcID");
        } catch (Exception e) {
            throw new Exception("MPC not found");
        }
        Mpc mpc = mpcMapper.selectById(mpcID);
        if (mpc == null) {
            throw new Exception("MPC not found");
        }
        return mpc;
    }

    public void create(MpcTaskCommand command) throws Exception {
        if (command.uid() != null
                && mpcTaskMapper.selectById(command.uid()) != null) {
            throw new Exception("任务已存在");
        }

        if (command.taskType()
                != MpcTaskCommand.TaskType.GARNET_INFERENCE) {
            throw new Exception("任务类型不匹配");
        }

        File file = fileMapper.selectById(command.dataId());
        if (file == null) {
            throw new Exception("文件不存在");
        }

        if (!file.getType().toLowerCase().contains("secureinfer")) {
            throw new Exception("文件类型不符");
        }

        for (ParticipantInput participant : command.participants()) {
            MpcTaskAgent mpcTaskAgent = new MpcTaskAgent();
            mpcTaskAgent.setAgentId(participant.agentId());
            mpcTaskAgent.setPart(participant.part());
            mpcTaskAgent.setMpcTaskId(command.uid());
            mpcTaskAgent.setCenterId(command.centerId());
            mpcTaskAgentMapper.insert(mpcTaskAgent);
        }

        // 将内部业务命令装配为数据库实体，参与方等命令信息由 Service
        // 单独处理，实体只负责任务持久化和执行。
        MpcTask mpcTask = MpcTaskCommandAssembler.toEntity(command);
        mpcTaskMapper.insert(mpcTask);
        preprocess(mpcTask);
    }

    public void preprocess(MpcTask mpcTask) throws Exception {
        mpcTaskService.preprocess(mpcTask);
    }
}
