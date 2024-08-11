package DveCenter.module.file.service;

import DveBase.common.Body;
import DveCenter.entity.DownloadTask;
import DveCenter.entity.Output;
import DveCenter.entity.QueryOutput;
import DveCenter.mapper.DownloadTaskMapper;
import DveCenter.mapper.QueryOutputMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class QueryFileService {

    @Autowired
    private QueryOutputMapper queryOutputMapper;

    @Autowired
    private DownloadTaskMapper downloadTaskMapper;

    @Autowired
    private FileService fileService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Body<String> saveQueryFile(MultipartFile file, String hash, Long applicationId,
                                      String base, java.sql.Timestamp expiredTime) {

        // 校验sha256
        String fileHash = fileService.getSha256(file);
        if (!fileHash.equals(hash)) {return Body.error(String.format("哈希校验失败，文件名: %s",
                file.getOriginalFilename()));}

        // 文件信息加入结果表
        // 若已存在，则进行覆盖
        LambdaQueryWrapper<QueryOutput> queryWrapper = Wrappers.<QueryOutput>lambdaQuery()
                .eq(QueryOutput::getName, file.getOriginalFilename())
                .eq(QueryOutput::getApplicationId, applicationId);
        QueryOutput queryQueryOutput = queryOutputMapper.selectOne(queryWrapper);
        if (queryQueryOutput != null) {
            if (fileHash.equals(queryQueryOutput.getHash())) {
                jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
                queryOutputMapper.deleteById(queryQueryOutput.getUid());
                jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
            }
        }
        QueryOutput newQueryOutput = new QueryOutput();
        newQueryOutput.setHash(hash);
        newQueryOutput.setPath(base + "/query/" + fileHash + "_appid_" + applicationId);
        newQueryOutput.setUploadDate(Timestamp.valueOf(LocalDateTime.now()));
        newQueryOutput.setApplicationId(applicationId);
        newQueryOutput.setExpiredTime(expiredTime);
        newQueryOutput.setName(file.getOriginalFilename());
        queryOutputMapper.insert(newQueryOutput);
        String filePath = newQueryOutput.getPath();
        String fileName = newQueryOutput.getName();

        // 存储文件到结果管理区
        try {
            fileService.saveFileToPath(file, filePath);
            // 判断文件是否为 CSV 文件
            if (fileName.toLowerCase().endsWith(".csv")) {
                // 将CSV文件转换为JSON文件
                fileService.csvToJson(filePath);
                newQueryOutput.setName(fileName.replaceAll("\\.csv$", ".json"));
                UpdateWrapper<QueryOutput> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("uid", newQueryOutput.getUid());
                queryOutputMapper.update(newQueryOutput, updateWrapper);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return Body.error(String.format("保存失败: 文件名: %s，错误信息: %s",
                    fileName, e.getMessage()));
        }
        return Body.success(String.format("保存成功，文件名: %s",
                fileName));
    }

    public Body<String> fetchQuery(Integer outputId, Long applicationId, String downloadPath) {

        // 根据结果id查找结果表
        LambdaQueryWrapper<QueryOutput> queryWrapper = Wrappers.<QueryOutput>lambdaQuery()
                .eq(QueryOutput::getUid, outputId)
                .eq(QueryOutput::getApplicationId, applicationId);
        QueryOutput queryQueryOutput = queryOutputMapper.selectOne(queryWrapper);
        if (queryQueryOutput == null) {
            return Body.error(String.format("找不到该文件，结果id: %d", outputId));
        }
        // 判断文件是否过期
        Timestamp expiredTime = queryQueryOutput.getExpiredTime();
        if (expiredTime != null && LocalDateTime.now().isAfter(expiredTime.toLocalDateTime())) {
            return Body.error(String.format("该文件已过期，结果id: %d，文件名: %s，失效时间: %s", outputId, queryQueryOutput.getName(),
                    queryQueryOutput.getExpiredTime()));
        }

        // 添加下载任务记录到任务表
        DownloadTask newDownloadTask = new DownloadTask();
        newDownloadTask.setApplicationId(applicationId);
        newDownloadTask.setOutputId(queryQueryOutput.getUid());
        newDownloadTask.setDownloadTime(Timestamp.valueOf(LocalDateTime.now()));
        newDownloadTask.setType("query");
        downloadTaskMapper.insert(newDownloadTask);

        // 直接通过路径访问文件
        String filePath = queryQueryOutput.getPath();
        String fileName = queryQueryOutput.getName();
        try {
            fileService.copyFile(filePath, fileName, downloadPath);
        } catch (Exception e) {
            e.printStackTrace();
            return Body.error(String.format("获取失败: 结果id %d，文件名: %s，错误信息: %s", outputId, fileName, e.getMessage()));
        }
        return Body.success(String.format("获取成功，结果id: %d，文件名: %s", outputId, fileName));
    }

    public Body<List<QueryOutput>> queryQuery(Integer applicationId) {

        LambdaQueryWrapper<QueryOutput> queryWrapper = Wrappers.<QueryOutput>lambdaQuery()
                .eq(QueryOutput::getApplicationId, applicationId);
        List<QueryOutput> outputs = queryOutputMapper.selectList(queryWrapper);
        Integer fileNum = outputs.size();
        return Body.success(outputs, String.format("查询成功，共查询到%d个文件", fileNum));
    }

    public Body<List<QueryOutput>> queryQueryByIds(Integer applicationId, List<Integer> outputIds) {

        LambdaQueryWrapper<QueryOutput> queryWrapper = Wrappers.<QueryOutput>lambdaQuery()
                .eq(QueryOutput::getApplicationId, applicationId)
                .in(QueryOutput::getUid, outputIds);

        List<QueryOutput> outputs = queryOutputMapper.selectList(queryWrapper);
        Integer fileNum = outputs.size();
        return Body.success(outputs, String.format("查询成功，共查询到%d个文件", fileNum));
    }

    public Body<String> deleteQuery(Integer applicationId, Integer outputId) {

        // 根据文件id查找结果表
        LambdaQueryWrapper<QueryOutput> queryWrapper = Wrappers.<QueryOutput>lambdaQuery()
                .eq(QueryOutput::getApplicationId, applicationId)
                .eq(QueryOutput::getUid, outputId);
        QueryOutput queryQueryOutput = queryOutputMapper.selectOne(queryWrapper);
        if (queryQueryOutput == null) {
            return Body.error(String.format("找不到该文件，结果id: %d", outputId));
        }
        String filePath = queryQueryOutput.getPath();
        String fileName = queryQueryOutput.getName();

        // 禁用外键检查
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");

        // 删除文件及结果表
        try {
            queryOutputMapper.deleteById(outputId);
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
