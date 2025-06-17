package DavexCenter.module.file.service;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import DavexBase.common.My;
import DavexCenter.entity.ComparisonOutput;
import DavexCenter.entity.Output;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import DavexBase.common.Body;
import DavexCenter.entity.FlOutput;
import DavexCenter.entity.DownloadTask;
import DavexCenter.mapper.FlOutputMapper;
import DavexCenter.mapper.DownloadTaskMapper;

@Service
public class FlFileService {

    @Autowired
    private FlOutputMapper flOutputMapper;

    @Autowired
    private DownloadTaskMapper downloadTaskMapper;

    @Autowired
    private FileService fileService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private My my;

    public Body<String> saveFl(MultipartFile file, String hash, String applicationId,
                                           java.sql.Timestamp expiredTime) {

        // 校验sha256
        String fileHash = fileService.getSha256(file);
        if (!fileHash.equals(hash)) {return Body.error(String.format("哈希校验失败，文件名: %s",
                file.getOriginalFilename()));}

        // 文件信息加入结果表
        // 若已存在，则进行覆盖
        LambdaQueryWrapper<FlOutput> queryWrapper = Wrappers.<FlOutput>lambdaQuery()
                .eq(FlOutput::getName, file.getOriginalFilename())
                .eq(FlOutput::getApplicationId, applicationId)
                .eq(FlOutput::getHash, fileHash);
        FlOutput queryFlOutput = flOutputMapper.selectOne(queryWrapper);
        if (queryFlOutput != null) {
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
            flOutputMapper.deleteById(queryFlOutput.getUid());
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
        }

        FlOutput newFlOutput = new FlOutput();
        newFlOutput.setHash(hash);
        newFlOutput.setPath(Paths.get(my.getBase_path()).resolve("result").resolve("fl")
                .resolve(fileHash + "_appid_" + applicationId).toString());
        newFlOutput.setUploadDate(Timestamp.valueOf(LocalDateTime.now()));
        newFlOutput.setApplicationId(applicationId);
        newFlOutput.setExpiredTime(expiredTime);
        newFlOutput.setName(file.getOriginalFilename());
        flOutputMapper.insert(newFlOutput);
        String filePath = newFlOutput.getPath();
        String fileName = newFlOutput.getName();

        // 存储文件到结果管理区
        try {
            fileService.saveFileToPath(file, filePath);
        } catch (IOException e) {
            e.printStackTrace();
            return Body.error(String.format("保存失败: 文件名: %s，错误信息: %s",
                    fileName, e.getMessage()));
        }
        return Body.success(String.format("保存成功，文件名: %s",
                fileName));
    }

    public Body<String> fetchFl(Long outputId, String applicationId) {

        // 根据结果id查找结果表
        LambdaQueryWrapper<FlOutput> queryWrapper = Wrappers.<FlOutput>lambdaQuery()
                .eq(FlOutput::getUid, outputId)
                .eq(FlOutput::getApplicationId, applicationId);
        FlOutput queryFlOutput = flOutputMapper.selectOne(queryWrapper);
        if (queryFlOutput == null) {
            return Body.error(String.format("找不到该文件，结果id: %d", outputId));
        }
        // 判断文件是否过期
        Timestamp expiredTime = queryFlOutput.getExpiredTime();
        if (expiredTime != null && LocalDateTime.now().isAfter(expiredTime.toLocalDateTime())) {
            return Body.error(String.format("该文件已过期，结果id: %d，文件名: %s，失效时间: %s", outputId, queryFlOutput.getName(),
                    queryFlOutput.getExpiredTime()));
        }

        // 添加下载任务记录到任务表
        String filePath = queryFlOutput.getPath();
        String fileName = queryFlOutput.getName();
        Path downloadPath = Paths.get(my.getBase_path()).resolve("download").resolve(applicationId).resolve("fl");
        DownloadTask newDownloadTask = new DownloadTask();
        newDownloadTask.setApplicationId(applicationId);
        newDownloadTask.setOutputId(queryFlOutput.getUid());
        newDownloadTask.setDownloadTime(Timestamp.valueOf(LocalDateTime.now()));
        newDownloadTask.setType("fl");
        newDownloadTask.setPath(downloadPath.resolve(fileName).toString());
        downloadTaskMapper.insert(newDownloadTask);

        // 直接通过路径访问文件
        try {
            fileService.copyFile(filePath, fileName, downloadPath.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return Body.error(String.format("获取失败: 结果id %d，文件名: %s，错误信息: %s", outputId, fileName, e.getMessage()));
        }
        return Body.success(String.format("获取成功，结果id: %d，文件名: %s，保存路径: %s", outputId, fileName, newDownloadTask.getPath()));
    }

    public Body<List<FlOutput>> queryFl(String applicationId) {

        LambdaQueryWrapper<FlOutput> queryWrapper = Wrappers.<FlOutput>lambdaQuery()
                .eq(FlOutput::getApplicationId, applicationId)
                .orderByDesc(FlOutput::getUploadDate);
        List<FlOutput> outputs = flOutputMapper.selectList(queryWrapper);
        Integer fileNum = outputs.size();
        return Body.success(outputs, String.format("查询成功，共查询到%d个文件", fileNum));
    }

    public Body<List<FlOutput>> queryFlByIds(String applicationId, List<Long> outputIds) {

        LambdaQueryWrapper<FlOutput> queryWrapper = Wrappers.<FlOutput>lambdaQuery()
                .eq(FlOutput::getApplicationId, applicationId)
                .in(FlOutput::getUid, outputIds)
                .orderByDesc(FlOutput::getUploadDate);

        List<FlOutput> outputs = flOutputMapper.selectList(queryWrapper);
        Integer fileNum = outputs.size();
        return Body.success(outputs, String.format("查询成功，共查询到%d个文件", fileNum));
    }

    public Body<String> deleteFl(String applicationId, Long outputId) {

        // 根据文件id查找结果表
        LambdaQueryWrapper<FlOutput> queryWrapper = Wrappers.<FlOutput>lambdaQuery()
                .eq(FlOutput::getApplicationId, applicationId)
                .eq(FlOutput::getUid, outputId);
        FlOutput queryFlOutput = flOutputMapper.selectOne(queryWrapper);
        if (queryFlOutput == null) {
            return Body.error(String.format("找不到该文件，结果id: %d", outputId));
        }
        String filePath = queryFlOutput.getPath();
        String fileName = queryFlOutput.getName();

        // 禁用外键检查
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");

        // 删除文件及结果表
        try {
            flOutputMapper.deleteById(outputId);
            fileService.deleteFileFromPath(filePath);
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

    public Body<String> readFl(String applicationId, Long outputId) throws IOException {
        LambdaQueryWrapper<FlOutput> queryWrapper = Wrappers.<FlOutput>lambdaQuery()
                .eq(FlOutput::getApplicationId, applicationId)
                .eq(FlOutput::getUid, outputId);
        FlOutput queryOutput = flOutputMapper.selectOne(queryWrapper);
        String filePath = queryOutput.getPath();
        String fileName = queryOutput.getName();

        return fileService.readFileContent(filePath, fileName);
    }
}
