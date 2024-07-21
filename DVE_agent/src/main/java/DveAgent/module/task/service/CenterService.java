
package DveAgent.module.task.service;

import com.baomidou.mybatisplus.extension.service.IService;

import DveAgent.entity.Center;
import DveAgent.mapper.CenterMapper;

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