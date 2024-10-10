package DavexAgent.module.task.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import DavexBase.common.Body;
import DavexBase.entity.File;
import DavexBase.entity.Mpc;
import DavexBase.entity.MpcTask.TaskType;
import DavexBase.entity.MpcTaskAgent;
import DavexBase.info.UploadAgentTaskInfo;
import DavexBase.mapper.FileMapper;
import DavexBase.mapper.MpcMapper;
import DavexBase.mapper.MpcTaskAgentMapper;
import DavexBase.mapper.MpcTaskMapper;

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

    public void create(UploadAgentTaskInfo mpcTaskInfo) throws Exception {
        if (mpcTaskInfo.getUid() != null && mpcTaskMapper.selectById(mpcTaskInfo.getUid()) != null) {
            throw new Exception("任务已存在");
        }
        if (mpcTaskInfo.getTaskType() != TaskType.GARNET_INFERENCE) {
            throw new Exception("任务类型不匹配");
        }
        File file = fileMapper.selectById(mpcTaskInfo.getDataId());
        if (file == null) {
            throw new Exception("文件不存在");
        }
        if (file.getType() != "model") {
            throw new Exception("文件不是模型");
        }
        for (UploadAgentTaskInfo.PartInfo partInfo : mpcTaskInfo.getPartInfo()) {
            MpcTaskAgent mpcTaskAgent = new MpcTaskAgent();
            mpcTaskAgent.setAgentId(partInfo.getAgentID());
            mpcTaskAgent.setPart(partInfo.getPart());
            mpcTaskAgent.setMpcTaskId(mpcTaskInfo.getUid());
            mpcTaskAgent.setCenterId(mpcTaskInfo.getCenterId());
            mpcTaskAgentMapper.insert(mpcTaskAgent);
        }
        mpcTaskMapper.insert(mpcTaskInfo);
        preprocess(mpcTaskInfo);

    }

    public void preprocess(UploadAgentTaskInfo mpcTask) throws Exception {
        mpcTaskService.preprocess(mpcTask);
    }
}
