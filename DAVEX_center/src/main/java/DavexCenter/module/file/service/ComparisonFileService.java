package DavexCenter.module.file.service;

import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import DavexBase.common.Body;
import DavexCenter.entity.ComparisonOutput;
import DavexCenter.entity.DownloadTask;
import DavexCenter.mapper.ComparisonOutputMapper;
import DavexCenter.mapper.DownloadTaskMapper;

@Service
public class ComparisonFileService {

    @Autowired
    private ComparisonOutputMapper comparisonOutputMapper;

    @Autowired
    private DownloadTaskMapper downloadTaskMapper;

    @Autowired
    private FileService fileService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Body<String> saveComparison(MultipartFile file, String hash, Long applicationId, Long agentId, Long fileId, Long folderId, String fileName,
                                           String base, java.sql.Timestamp expiredTime) {

        // 校验sha256
        String fileHash = fileService.getSha256(file);
        if (!fileHash.equals(hash)) {return Body.error(String.format("哈希校验失败，文件名: %s",
                file.getOriginalFilename()));}

        // 文件信息加入结果表
        // 若已存在，则进行覆盖
        LambdaQueryWrapper<ComparisonOutput> queryWrapper = Wrappers.<ComparisonOutput>lambdaQuery()
                .eq(ComparisonOutput::getName, file.getOriginalFilename())
                .eq(ComparisonOutput::getApplicationId, applicationId)
                .eq(ComparisonOutput::getHash, fileHash);
        ComparisonOutput queryComparisonOutput = comparisonOutputMapper.selectOne(queryWrapper);
        if (queryComparisonOutput != null) {
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
            comparisonOutputMapper.deleteById(queryComparisonOutput.getUid());
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
        }

        ComparisonOutput newComparisonOutput = new ComparisonOutput();
        newComparisonOutput.setHash(hash);
        newComparisonOutput.setPath(Paths.get(base).resolve("comparison").resolve(fileHash + "_appid_" + applicationId).toString());
        newComparisonOutput.setUploadDate(Timestamp.valueOf(LocalDateTime.now()));
        newComparisonOutput.setApplicationId(applicationId);
        newComparisonOutput.setExpiredTime(expiredTime);
        newComparisonOutput.setName(file.getOriginalFilename());
        newComparisonOutput.setAgentId(agentId);
        newComparisonOutput.setFileId(fileId);
        newComparisonOutput.setFolderId(folderId);
        newComparisonOutput.setDestName(fileName);
        comparisonOutputMapper.insert(newComparisonOutput);
        String filePath = newComparisonOutput.getPath();
        String fileName = newComparisonOutput.getName();

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

    public Body<String> fetchComparison(Long outputId, Long applicationId, String downloadPath) {

        // 根据结果id查找结果表
        LambdaQueryWrapper<ComparisonOutput> queryWrapper = Wrappers.<ComparisonOutput>lambdaQuery()
                .eq(ComparisonOutput::getUid, outputId)
                .eq(ComparisonOutput::getApplicationId, applicationId);
        ComparisonOutput queryComparisonOutput = comparisonOutputMapper.selectOne(queryWrapper);
        if (queryComparisonOutput == null) {
            return Body.error(String.format("找不到该文件，结果id: %d", outputId));
        }
        // 判断文件是否过期
        Timestamp expiredTime = queryComparisonOutput.getExpiredTime();
        if (expiredTime != null && LocalDateTime.now().isAfter(expiredTime.toLocalDateTime())) {
            return Body.error(String.format("该文件已过期，结果id: %d，文件名: %s，失效时间: %s", outputId, queryComparisonOutput.getName(),
                    queryComparisonOutput.getExpiredTime()));
        }

        // 添加下载任务记录到任务表
        DownloadTask newDownloadTask = new DownloadTask();
        newDownloadTask.setApplicationId(applicationId);
        newDownloadTask.setOutputId(queryComparisonOutput.getUid());
        newDownloadTask.setDownloadTime(Timestamp.valueOf(LocalDateTime.now()));
        newDownloadTask.setType("comparison");
        downloadTaskMapper.insert(newDownloadTask);

        // 直接通过路径访问文件
        String filePath = queryComparisonOutput.getPath();
        String fileName = queryComparisonOutput.getName();
        try {
            fileService.copyFile(filePath, fileName, downloadPath);
        } catch (Exception e) {
            e.printStackTrace();
            return Body.error(String.format("获取失败: 结果id %d，文件名: %s，错误信息: %s", outputId, fileName, e.getMessage()));
        }
        return Body.success(String.format("获取成功，结果id: %d，文件名: %s", outputId, fileName));
    }

    public Body<List<ComparisonOutput>> queryComparison(Long applicationId) {

        LambdaQueryWrapper<ComparisonOutput> queryWrapper = Wrappers.<ComparisonOutput>lambdaQuery()
                .eq(ComparisonOutput::getApplicationId, applicationId);
        List<ComparisonOutput> outputs = comparisonOutputMapper.selectList(queryWrapper);
        Integer fileNum = outputs.size();
        return Body.success(outputs, String.format("查询成功，共查询到%d个文件", fileNum));
    }

    public Body<List<ComparisonOutput>> queryComparisonByIds(Long applicationId, List<Long> outputIds) {

        LambdaQueryWrapper<ComparisonOutput> queryWrapper = Wrappers.<ComparisonOutput>lambdaQuery()
                .eq(ComparisonOutput::getApplicationId, applicationId)
                .in(ComparisonOutput::getUid, outputIds);

        List<ComparisonOutput> outputs = comparisonOutputMapper.selectList(queryWrapper);
        Integer fileNum = outputs.size();
        return Body.success(outputs, String.format("查询成功，共查询到%d个文件", fileNum));
    }

    public Body<String> deleteComparison(Long applicationId, Long outputId) {

        // 根据文件id查找结果表
        LambdaQueryWrapper<ComparisonOutput> queryWrapper = Wrappers.<ComparisonOutput>lambdaQuery()
                .eq(ComparisonOutput::getApplicationId, applicationId)
                .eq(ComparisonOutput::getUid, outputId);
        ComparisonOutput queryComparisonOutput = comparisonOutputMapper.selectOne(queryWrapper);
        if (queryComparisonOutput == null) {
            return Body.error(String.format("找不到该文件，结果id: %d", outputId));
        }
        String filePath = queryComparisonOutput.getPath();
        String fileName = queryComparisonOutput.getName();

        // 禁用外键检查
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");

        // 删除文件及结果表
        try {
            comparisonOutputMapper.deleteById(outputId);
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
}
