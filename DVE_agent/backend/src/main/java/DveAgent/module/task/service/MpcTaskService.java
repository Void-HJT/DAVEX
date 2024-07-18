package DveAgent.module.task.service;

import com.baomidou.mybatisplus.extension.service.IService;
import DveAgent.entity.MpcTask;
import java.util.Optional;

public interface MpcTaskService extends IService<MpcTask> {
    // 可以添加自定义的业务逻辑方法
    Optional<MpcTask> getMpcTaskById(Long id);
    boolean saveMpcTask(MpcTask mpcTask);
}