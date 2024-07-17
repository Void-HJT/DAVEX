package DveCenter.module.File;

import DveCenter.common.Body;
import DveCenter.entity.File;
import DveCenter.entity.Output;
import DveCenter.mapper.FileMapper;
import DveCenter.mapper.OutputMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

//import java.io.File; 命名冲突，使用全限定名
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class FileService {

    @Autowired
    private FileMapper fileMapper;
    @Autowired
    private OutputMapper outputMapper;

    public Body<String> uploadFile(MultipartFile file, Integer fileId, String base) {

        //根据文件id查找文件表
        LambdaQueryWrapper<File> queryWrapper = Wrappers.<File>lambdaQuery()
                .eq(File::getUid, fileId);
        File queryFile = fileMapper.selectOne(queryWrapper);
        if(queryFile == null){return Body.error("找不到该文件");}

        //文件信息加入结果表
        //若已存在，则进行覆盖
        LambdaQueryWrapper<Output> queryWrapper1 = Wrappers.<Output>lambdaQuery()
                .eq(Output::getUid, fileId);
        Output queryOutput = outputMapper.selectOne(queryWrapper1);
        if(queryOutput != null){
            outputMapper.deleteById(fileId);
        }
        Output newOutput = new Output();
        String fileName = queryFile.getName();
        newOutput.setUid(fileId);newOutput.setName(queryFile.getName());newOutput.setType(queryFile.getType());
        newOutput.setDownloadDate(Timestamp.valueOf(LocalDateTime.now()));newOutput.setTag(queryFile.getTag());
        newOutput.setSize(queryFile.getSize());newOutput.setDescription(queryFile.getDescription());
        newOutput.setPath(base + fileName);
        //失效时间设置为一周后
        newOutput.setExpiredTime(Timestamp.valueOf(LocalDateTime.now().plus(1, ChronoUnit.WEEKS)));
        outputMapper.insert(newOutput);

        //存储文件到结果管理区
        try {
            // 新建一个文件路径
            java.io.File uploadFile = new java.io.File(base + fileName);
            // 当父级目录不存在时，自动创建
            if (!uploadFile.getParentFile().exists()) {
                uploadFile.getParentFile().mkdirs();
            }
            // 存储文件到电脑磁盘
            file.transferTo(uploadFile);

        } catch (IOException e) {
            e.printStackTrace();
            return Body.error("上传失败: " + e.getMessage());
        }
        return Body.success("上传成功");
    }
}
