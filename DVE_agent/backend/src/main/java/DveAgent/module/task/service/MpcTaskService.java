package DveAgent.module.task.service;

import DveAgent.entity.MpcTask;
import DveAgent.module.task.dao.MpcTaskDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MpcTaskService {
    @Autowired
    private MpcTaskDao mpcTaskDao;

    public List<MpcTask> findAll() {
        return mpcTaskDao.findAll();
    }

    public MpcTask findById(Long id) {
        return mpcTaskDao.findById(id).orElse(null);
    }

    public MpcTask save(MpcTask mpcTask) {
        return mpcTaskDao.save(mpcTask);
    }

    public void deleteById(Long id) {
        mpcTaskDao.deleteById(id);
    }
}