package DveCenter.module.directory;

import DveCenter.common.Body;
import DveCenter.entity.Folder;
import DveCenter.mapper.FolderMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DirectoryService {

    @Autowired
    private FolderMapper folderMapper;

    public Body<String> createFolder(String name, String path,Integer agent_id, Integer parent_id){
        //1.查询数据库相同父文件夹下是否有同名文件夹
        LambdaQueryWrapper<Folder> queryWrapper = Wrappers.<Folder>lambdaQuery()
                .eq(Folder::getParentId,parent_id)
                .eq(Folder::getName,name);
        List<Folder> folderList = folderMapper.selectList(queryWrapper);
        if(!folderList.isEmpty()){return Body.error("重名文件夹");}
        //2.新文件夹插入
        Folder new_folder = new Folder();
        new_folder.setName(name);new_folder.setAgentId(agent_id);new_folder.setParentId(parent_id);
        new_folder.setCreateDate(Timestamp.valueOf(LocalDateTime.now()));new_folder.setLastUpdate(Timestamp.valueOf(LocalDateTime.now()));
        folderMapper.insert(new_folder);
        //3.本地创建新文件夹
        Path create_path = Paths.get(path,name);
        try {
            Files.createDirectories(create_path);
        } catch (IOException e) {
            e.printStackTrace();
            return Body.error("文件夹创建失败: " + e.getMessage());
        }
        return Body.success("插入新文件夹成功");
    }

}
