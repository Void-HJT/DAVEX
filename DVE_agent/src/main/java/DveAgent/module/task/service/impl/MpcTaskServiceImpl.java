// package DveAgent.module.task.service.impl;

// import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
// import DveAgent.entity.MpcTask;
// import DveAgent.module.task.dao.MpcTaskMapper;
// import DveAgent.module.task.service.MpcTaskService;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import java.util.Optional;

// @Service
// public class MpcTaskServiceImpl extends ServiceImpl<MpcTaskMapper, MpcTask> implements MpcTaskService {
//     @Autowired
//     private MpcTaskMapper mpcTaskMapper;

//     @Override
//     @Transactional
//     public Optional<MpcTask> getMpcTaskById(Long id) {
//         return mpcTaskMapper.getMpcTaskById(id);
//     }

//     @Override
//     @Transactional
//     public boolean saveMpcTask(MpcTask mpcTask) {
//         Long id = mpcTask.getUid();
//         if (id != 0) { // 这里将 null 处理为 0
//             return mpcTaskMapper.updateMpcTask(mpcTask);
//         } else {
//             return mpcTaskMapper.insertMpcTask(mpcTask);
//         }
//     }
// }