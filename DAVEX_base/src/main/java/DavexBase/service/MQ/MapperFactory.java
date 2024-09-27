package DavexBase.service.MQ;

import DavexBase.entity.*;
import DavexBase.mapper.*;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class MapperFactory {

    // 定义一个 Map，存储 Mapper 和它对应的实体类
    private final Map<String, BaseMapper<?>> mapperMap = new HashMap<>();
    private final Map<String, Class<?>> entityTypeMap = new HashMap<>();

    @Autowired
    public MapperFactory(ApplicationMapper applicationMapper, CenterMapper centerMapper, AgentMapper agentMapper,
                         FileMapper fileMapper, FolderMapper folderMapper,RuleMapper ruleMapper,FileRuleMapper fileRuleMapper,
                         VisibilityMapper visibilityMapper,FolderVisibilityMapper folderVisibilityMapper) {
        // 注册不同的 Mapper 实现类
        mapperMap.put("application", applicationMapper);
        entityTypeMap.put("application", Application.class);

        mapperMap.put("center", centerMapper);
        entityTypeMap.put("center", Center.class);

        mapperMap.put("agent", agentMapper);
        entityTypeMap.put("agent", Agent.class);

        mapperMap.put("file", fileMapper);
        entityTypeMap.put("file", File.class);

        mapperMap.put("folder", folderMapper);
        entityTypeMap.put("folder", Folder.class);

        mapperMap.put("rule", ruleMapper);
        entityTypeMap.put("rule", Rule.class);

        mapperMap.put("fileRule", fileRuleMapper);
        entityTypeMap.put("fileRule", FileRule.class);

        mapperMap.put("visibility", visibilityMapper);
        entityTypeMap.put("visibility", Visibility.class);

        mapperMap.put("folderVisibility", folderVisibilityMapper);
        entityTypeMap.put("folderVisibility", FolderVisibility.class);
    }

    // 根据类名获取相应的 Mapper 实例
    public BaseMapper<?> getMapper(String className) {
        return mapperMap.get(className.toLowerCase());
    }

    // 根据类名获取对应的实体类
    public Class<?> getEntityType(String className) {
        return entityTypeMap.get(className.toLowerCase());
    }
}



