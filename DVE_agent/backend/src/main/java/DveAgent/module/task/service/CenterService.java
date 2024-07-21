
package DveAgent.module.task.service;

import com.baomidou.mybatisplus.extension.service.IService;
import DveAgent.module.task.dao.CenterMapper;
import DveAgent.entity.Center;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class CenterService {

    @Autowired
    private CenterMapper centerMapper;

    public Center createCenter(Center center) {
        centerMapper.insert(center);
        return center;
    }
}