package DveCenter.module.File;

import DveAgent.common.Body;
import DveAgent.entity.File;
import DveAgent.mapper.FileMapper;
import DveCenter.entity.Output;
import DveCenter.entity.Task;
import DveCenter.mapper.OutputMapper;
import DveCenter.mapper.TaskMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.apache.commons.io.FileUtils;

//import java.io.File; 命名冲突，使用全限定名
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileService {

    @Autowired
    private FileMapper fileMapper;
    @Autowired
    private OutputMapper outputMapper;
    @Autowired
    private TaskMapper taskMapper;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Body<String> saveFile(MultipartFile file, Integer fileId, Integer agentId, Integer applicationId,
                                 String base, java.sql.Timestamp expiredTime) {

        // 根据文件id查找文件表
        LambdaQueryWrapper<File> queryWrapper = Wrappers.<File>lambdaQuery()
                .eq(File::getUid, fileId)
                .eq(File::getAgentId, agentId);
        File queryFile = fileMapper.selectOne(queryWrapper);
        if(queryFile == null){return Body.error(String.format("找不到该文件，文件id: %d，代理id: %d", fileId, agentId));}

        // 校验sha256
        String fileHash = getSha256(file);
        if (!fileHash.equals(queryFile.getHash())) {return Body.error(String.format("哈希校验失败，文件id: %d，代理id: %d", fileId, agentId));}

        // 文件信息加入结果表
        // 若已存在，则进行覆盖
        LambdaQueryWrapper<Output> queryWrapper1 = Wrappers.<Output>lambdaQuery()
                .eq(Output::getFileId, fileId)
                .eq(Output::getAgentId, agentId)
                .eq(Output::getApplicationId, applicationId);
        Output queryOutput = outputMapper.selectOne(queryWrapper1);
        if(queryOutput != null){
            if (fileHash.equals(queryOutput.getHash())) {
                jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
                outputMapper.deleteById(queryOutput.getUid());
                jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
            }
        }
        Output newOutput = new Output();
        String fileName = queryFile.getName();
        newOutput.setName(queryFile.getName());newOutput.setType(queryFile.getType());
        newOutput.setUploadDate(Timestamp.valueOf(LocalDateTime.now()));newOutput.setTag(queryFile.getTag());
        newOutput.setSize(queryFile.getSize());newOutput.setDescription(queryFile.getDescription());
        newOutput.setPath(base + fileHash + "_appid_" + applicationId);newOutput.setExpiredTime(expiredTime);newOutput.setHash(fileHash);
        newOutput.setFileId(fileId);newOutput.setAgentId(agentId);newOutput.setApplicationId(applicationId);
        outputMapper.insert(newOutput);

        // 存储文件到结果管理区
        try {
            // 新建一个文件路径
            java.io.File uploadFile = new java.io.File(newOutput.getPath());
            // 当父级目录不存在时，自动创建
            if (!uploadFile.getParentFile().exists()) {
                uploadFile.getParentFile().mkdirs();
            }
            // 存储文件到电脑磁盘
//            file.transferTo(uploadFile);
            FileUtils.copyInputStreamToFile(file.getInputStream(), uploadFile);

        } catch (IOException e) {
            e.printStackTrace();
            return Body.error(String.format("保存失败: 文件id %d，代理id: %d，文件名: %s，错误信息: %s", fileId, agentId, fileName, e.getMessage()));
        }
        return Body.success(String.format("保存成功，文件id: %d，代理id: %d，文件名: %s", fileId, agentId, fileName));
    }


    public Body<List<String>> saveFiles(List<MultipartFile> files, List<Integer> fileIds, List<Integer> agentIds,
                                        Integer applicationId, String base, List<java.sql.Timestamp> expiredTimes) {
        List<String> results = new ArrayList<>();

        if (files.size() != fileIds.size() || files.size() != agentIds.size() || files.size() != expiredTimes.size()) {
            return Body.error("文件数量、文件ID数量、代理ID数量和失效时间数量不匹配");
        }

        boolean flag = true;
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            Integer fileId = fileIds.get(i);
            Integer agentId = agentIds.get(i);
            java.sql.Timestamp expiredTime = expiredTimes.get(i);

            // 根据文件id查找文件表
            LambdaQueryWrapper<File> queryWrapper = Wrappers.<File>lambdaQuery()
                    .eq(File::getUid, fileId)
                    .eq(File::getAgentId, agentId);
            File queryFile = fileMapper.selectOne(queryWrapper);
            if (queryFile == null) {
                results.add(String.format("找不到该文件，文件id: %d，代理id: %d", fileId, agentId));
                flag = false;
                continue;
            }

            // 校验md5
            String fileHash = getSha256(file);
            if (!fileHash.equals(queryFile.getHash())) {return Body.error(String.format("哈希校验失败，文件id: %d，代理id: %d", fileId, agentId));}

            // 文件信息加入结果表
            // 若已存在，则进行覆盖
            LambdaQueryWrapper<Output> queryWrapper1 = Wrappers.<Output>lambdaQuery()
                    .eq(Output::getFileId, fileId)
                    .eq(Output::getAgentId, agentId)
                    .eq(Output::getApplicationId, applicationId);
            Output queryOutput = outputMapper.selectOne(queryWrapper1);
            if (queryOutput != null) {
                if (fileHash.equals(queryOutput.getHash())) {
                    jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
                    outputMapper.deleteById(queryOutput.getUid());
                    jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
                }
            }
            Output newOutput = new Output();
            String fileName = queryFile.getName();
            newOutput.setName(queryFile.getName());
            newOutput.setType(queryFile.getType());
            newOutput.setUploadDate(Timestamp.valueOf(LocalDateTime.now()));
            newOutput.setTag(queryFile.getTag());
            newOutput.setSize(queryFile.getSize());
            newOutput.setDescription(queryFile.getDescription());
            newOutput.setPath(base + fileHash + "_appid_" + applicationId);
            newOutput.setExpiredTime(expiredTime);
            newOutput.setHash(fileHash);
            newOutput.setFileId(fileId);
            newOutput.setAgentId(agentId);
            newOutput.setApplicationId(applicationId);
            outputMapper.insert(newOutput);

            // 存储文件到结果管理区
            try {
                // 新建一个文件路径
                java.io.File uploadFile = new java.io.File(newOutput.getPath());
                // 当父级目录不存在时，自动创建
                if (!uploadFile.getParentFile().exists()) {
                    uploadFile.getParentFile().mkdirs();
                }
                // 存储文件到电脑磁盘
//                file.transferTo(uploadFile);
                FileUtils.copyInputStreamToFile(file.getInputStream(), uploadFile);
                results.add(String.format("保存成功，文件id: %d，代理id: %d，文件名: %s", fileId, agentId, fileName));
            } catch (IOException e) {
                e.printStackTrace();
                results.add(String.format("保存失败: 文件id %d，代理id: %d，文件名: %s，错误信息: %s", fileId, agentId, fileName, e.getMessage()));
                flag = false;
            }
        }

        if (flag == false) return Body.error(results, "部分文件保存失败");
        return Body.success(results, "文件保存处理完成");
    }


    public Body<String> fetchFile(Integer outputId, Long applicationId, HttpServletResponse response) {

        // 根据结果id查找结果表
        LambdaQueryWrapper<Output> queryWrapper = Wrappers.<Output>lambdaQuery()
                .eq(Output::getUid, outputId)
                .eq(Output::getApplicationId, applicationId);
        Output queryOutput = outputMapper.selectOne(queryWrapper);
        if(queryOutput == null){return Body.error(String.format("找不到该文件，结果id: %d", outputId));}
        // 判断文件是否过期
        Timestamp expiredTime = queryOutput.getExpiredTime();
        if (expiredTime != null && LocalDateTime.now().isAfter(expiredTime.toLocalDateTime())) {
            return Body.error(String.format("该文件已过期，结果id: %d，文件名: %s，失效时间: %s", outputId, queryOutput.getName(),
                    queryOutput.getExpiredTime()));
        }

        // 添加下载任务记录到任务表
        Task newTask = new Task();
        newTask.setFileId(queryOutput.getFileId());newTask.setAgentId(queryOutput.getAgentId());newTask.setApplicationId(applicationId);
        newTask.setOutputId(queryOutput.getUid());newTask.setDownloadTime(Timestamp.valueOf(LocalDateTime.now()));
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
            response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));  // 这里要设置文件名的编码，否则中文的文件名下载后不显示
            // 写出字节数组到输出流
            os.write(bytes);
            // 刷新输出流
            os.flush();
        } catch (Exception e) {
            e.printStackTrace();
            return Body.error(String.format("获取失败: 结果id %d，文件名: %s，错误信息: %s", outputId, fileName, e.getMessage()));
        }
        return Body.success(String.format("获取成功，结果id: %d，文件名: %s", outputId, fileName));
    }


    public Body<String> fetchFileByPath(Integer outputId, Long applicationId, String downloadPath) {

        // 根据结果id查找结果表
        LambdaQueryWrapper<Output> queryWrapper = Wrappers.<Output>lambdaQuery()
                .eq(Output::getUid, outputId)
                .eq(Output::getApplicationId, applicationId);
        Output queryOutput = outputMapper.selectOne(queryWrapper);
        if(queryOutput == null){return Body.error(String.format("找不到该文件，结果id: %d", outputId));}
        // 判断文件是否过期
        Timestamp expiredTime = queryOutput.getExpiredTime();
        if (expiredTime != null && LocalDateTime.now().isAfter(expiredTime.toLocalDateTime())) {
            return Body.error(String.format("该文件已过期，结果id: %d，文件名: %s，失效时间: %s", outputId, queryOutput.getName(),
                    queryOutput.getExpiredTime()));
        }

        // 添加下载任务记录到任务表
        Task newTask = new Task();
        newTask.setFileId(queryOutput.getFileId());newTask.setAgentId(queryOutput.getAgentId());newTask.setApplicationId(applicationId);
        newTask.setOutputId(queryOutput.getUid());newTask.setDownloadTime(Timestamp.valueOf(LocalDateTime.now()));
        taskMapper.insert(newTask);

        //直接通过路径访问文件
        String filePath = queryOutput.getPath();
        String fileName = queryOutput.getName();
        var source = new java.io.File(filePath);
        var dest = new java.io.File(downloadPath + fileName);
        try (var fis = new FileInputStream(source);
             var fos = new FileOutputStream(dest)) {

            byte[] buffer = new byte[1024];
            int length;

            while ((length = fis.read(buffer)) > 0) {

                fos.write(buffer, 0, length);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Body.error(String.format("获取失败: 结果id %d，文件名: %s，错误信息: %s", outputId, fileName, e.getMessage()));
        }
        return Body.success(String.format("获取成功，结果id: %d，文件名: %s", outputId, fileName));
    }


    public Body<List<Output>> queryFile(Integer applicationId) {

        LambdaQueryWrapper<Output> queryWrapper = Wrappers.<Output>lambdaQuery()
                .eq(Output::getApplicationId, applicationId);
        List<Output> outputs = outputMapper.selectList(queryWrapper);
        Integer fileNum = outputs.size();
        return Body.success(outputs, String.format("查询成功，共查询到%d个文件", fileNum));
    }


    public Body<List<Output>> queryFileByIds(Integer applicationId, List<Integer> outputIds) {

        LambdaQueryWrapper<Output> queryWrapper = Wrappers.<Output>lambdaQuery()
                .eq(Output::getApplicationId, applicationId)
                .in(Output::getUid, outputIds);

        List<Output> outputs = outputMapper.selectList(queryWrapper);
        Integer fileNum = outputs.size();
        return Body.success(outputs, String.format("查询成功，共查询到%d个文件", fileNum));
    }


    public Body<String> deleteFile(Integer applicationId, Integer outputId) {

        // 根据文件id查找结果表
        LambdaQueryWrapper<Output> queryWrapper = Wrappers.<Output>lambdaQuery()
                .eq(Output::getApplicationId, applicationId)
                .eq(Output::getUid, outputId);
        Output queryOutput = outputMapper.selectOne(queryWrapper);
        if(queryOutput == null){return Body.error(String.format("找不到该文件，结果id: %d", outputId));}
        String filePath = queryOutput.getPath();
        String fileName = queryOutput.getName();

        // 禁用外键检查
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");

        // 删除文件及结果表
        try {
            outputMapper.deleteById(outputId);
            java.io.File file = new java.io.File(filePath);
            //路径是个文件且不为空时删除文件
            if(file.isFile() && file.exists()) {
                file.delete();
            }
            // 启用外键检查
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
        } catch (Exception e) {
            // 确保在异常情况下重新启用外键检查
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
            e.printStackTrace();
            return Body.error(String.format("删除失败: 结果id %d，文件名: %s，错误信息: %s", outputId, fileName, e.getMessage()));
        }

        return Body.success(String.format("删除成功，结果id: %d，文件名: %s", outputId, fileName));
    }


    public String getSha256(MultipartFile file) {

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(file.getBytes());
            byte[] digest = md.digest();
            String mySha256 = DatatypeConverter
                    .printHexBinary(digest).toLowerCase();

            return mySha256;
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
