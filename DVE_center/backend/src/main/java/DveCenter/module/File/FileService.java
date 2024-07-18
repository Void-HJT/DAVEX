package DveCenter.module.File;

import DveCenter.common.Body;
import DveCenter.entity.File;
import DveCenter.entity.Output;
import DveCenter.entity.Task;
import DveCenter.mapper.FileMapper;
import DveCenter.mapper.OutputMapper;
import DveCenter.mapper.TaskMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

//import java.io.File; 命名冲突，使用全限定名
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class FileService {

    @Autowired
    private FileMapper fileMapper;
    @Autowired
    private OutputMapper outputMapper;
    @Autowired
    private TaskMapper taskMapper;

    public Body<String> uploadFile(MultipartFile file, Integer fileId, String base, java.sql.Timestamp expiredTime) {

        // 根据文件id查找文件表
        LambdaQueryWrapper<File> queryWrapper = Wrappers.<File>lambdaQuery()
                .eq(File::getUid, fileId);
        File queryFile = fileMapper.selectOne(queryWrapper);
        if(queryFile == null){return Body.error("找不到该文件");}

        // 文件信息加入结果表
        // 若已存在，则进行覆盖
        LambdaQueryWrapper<Output> queryWrapper1 = Wrappers.<Output>lambdaQuery()
                .eq(Output::getUid, fileId);
        Output queryOutput = outputMapper.selectOne(queryWrapper1);
        if(queryOutput != null){
            outputMapper.deleteById(fileId);
        }
        Output newOutput = new Output();
        String fileName = queryFile.getName();
        newOutput.setUid(fileId);newOutput.setName(queryFile.getName());newOutput.setType(queryFile.getType());
        newOutput.setUploadDate(Timestamp.valueOf(LocalDateTime.now()));newOutput.setTag(queryFile.getTag());
        newOutput.setSize(queryFile.getSize());newOutput.setDescription(queryFile.getDescription());
        newOutput.setPath(base + fileName);newOutput.setExpiredTime(expiredTime);newOutput.setHash(queryFile.getHash());
        outputMapper.insert(newOutput);

        // 存储文件到结果管理区
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


    public Body<String> downloadFile(Integer fileId, Integer applicationId, HttpServletResponse response) {

        // 根据文件id查找结果表
        LambdaQueryWrapper<Output> queryWrapper = Wrappers.<Output>lambdaQuery()
                .eq(Output::getUid, fileId);
        Output queryOutput = outputMapper.selectOne(queryWrapper);
        if(queryOutput == null){return Body.error("找不到该文件");}
        // 判断文件是否过期
        Timestamp expiredTime = queryOutput.getExpiredTime();
        if (expiredTime != null && LocalDateTime.now().isAfter(expiredTime.toLocalDateTime())) {
            return Body.error("该文件已过期");
        }

        // 添加下载任务记录到任务表
        LambdaQueryWrapper<File> queryWrapper1 = Wrappers.<File>lambdaQuery()
                .eq(File::getUid, fileId);
        File queryFile = fileMapper.selectOne(queryWrapper1);
        Task newTask = new Task();
        newTask.setFileId(fileId);newTask.setAgentId(queryFile.getAgentId());newTask.setApplicationId(applicationId);
        newTask.setOutputId(fileId);newTask.setDownloadTime(Timestamp.valueOf(LocalDateTime.now()));
        taskMapper.insert(newTask);

        // 新建文件流，从磁盘读取文件流
        String filePath = queryOutput.getPath();
        String fileName = queryOutput.getName();
        try (FileInputStream fis = new FileInputStream(filePath);
             BufferedInputStream bis = new BufferedInputStream(fis);
             OutputStream os = response.getOutputStream()) {    //  OutputStream 是文件写出流，将文件下载到浏览器客户端
            // 新建字节数组，长度是文件的大小，比如文件 6kb, bis.available() = 1024 * 6
            byte[] bytes = new byte[bis.available()];
            // 从文件流读取字节到字节数组中
            bis.read(bytes);
            // 重置 response
            response.reset();
            // 设置 response 的下载响应头
            response.setContentType("application/octet-stream");
            response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));  // 注意，这里要设置文件名的编码，否则中文的文件名下载后不显示
            // 写出字节数组到输出流
            os.write(bytes);
            // 刷新输出流
            os.flush();
        } catch (Exception e) {
            e.printStackTrace();
            return Body.error("下载失败: " + e.getMessage());
        }
        return Body.success("下载成功");
    }


    public Body<List<Output>> queryFile() {

        List<Output> outputs = outputMapper.selectList(null);
        return Body.success(outputs, "查询成功");
    }
}
