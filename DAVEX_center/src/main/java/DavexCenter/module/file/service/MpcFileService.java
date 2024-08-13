package DavexCenter.module.file.service;

import DavexBase.common.Body;
import DavexBase.entity.MpcTaskOutput;
import DavexCenter.entity.DownloadTask;
import DavexCenter.entity.MpcOutput;
import DavexCenter.mapper.DownloadTaskMapper;
import DavexCenter.mapper.MpcOutputMapper;
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
public class MpcFileService {

    @Autowired
    private FileService fileService;

    @Autowired
    private MpcOutputMapper mpcOutputMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DownloadTaskMapper downloadTaskMapper;

    public Body<String> saveMpcFile(MultipartFile file, MpcTaskOutput mpcInfo, Long applicationId,
                                    String base, java.sql.Timestamp expiredTime) {

        // 校验sha256
        String fileHash = fileService.getSha256(file);
        if (!fileHash.equals(mpcInfo.getHash())) {return Body.error(String.format("哈希校验失败，任务id: %s",
                mpcInfo.getTaskId()));}

        // 文件信息加入结果表
        // 若已存在，则进行覆盖
        LambdaQueryWrapper<MpcOutput> queryWrapper = Wrappers.<MpcOutput>lambdaQuery()
                .eq(MpcOutput::getTaskId, mpcInfo.getTaskId())
                .eq(MpcOutput::getApplicationId, applicationId);
        MpcOutput queryMpcOutput = mpcOutputMapper.selectOne(queryWrapper);
        if (queryMpcOutput != null) {
            if (fileHash.equals(queryMpcOutput.getHash())) {
                jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
                mpcOutputMapper.deleteById(queryMpcOutput.getUid());
                jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
            }
        }
        MpcOutput newMpcOutput = new MpcOutput();
        newMpcOutput.setTaskId(mpcInfo.getTaskId());
        newMpcOutput.setHash(mpcInfo.getHash());
        newMpcOutput.setPath(base + "/mpc/" + fileHash + "_appid_" + applicationId);
        newMpcOutput.setUploadDate(Timestamp.valueOf(LocalDateTime.now()));
        newMpcOutput.setApplicationId(applicationId);
        newMpcOutput.setExpiredTime(expiredTime);
        newMpcOutput.setName(file.getOriginalFilename());
        mpcOutputMapper.insert(newMpcOutput);
        String filePath = newMpcOutput.getPath();
        String fileName = newMpcOutput.getName();

        // 存储文件到结果管理区
        try {
            fileService.saveFileToPath(file, filePath);
            // 判断文件是否为 CSV 文件
            if (fileName.toLowerCase().endsWith(".csv")) {
                // 将CSV文件转换为JSON文件
                fileService.csvToJson(filePath);
                newMpcOutput.setName(fileName.replaceAll("\\.csv$", ".json"));
                UpdateWrapper<MpcOutput> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("uid", newMpcOutput.getUid());
                mpcOutputMapper.update(newMpcOutput, updateWrapper);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return Body.error(String.format("保存失败: 任务id: %s，错误信息: %s",
                    mpcInfo.getTaskId(), e.getMessage()));
        }
        return Body.success(String.format("保存成功，任务id: %s",
                mpcInfo.getTaskId()));
    }

    public Body<String> fetchMpc(Integer mpcOutputId, Long applicationId, String downloadPath) {
        // 根据结果id查找结果表
        LambdaQueryWrapper<MpcOutput> queryWrapper = Wrappers.<MpcOutput>lambdaQuery()
                .eq(MpcOutput::getUid, mpcOutputId)
                .eq(MpcOutput::getApplicationId, applicationId);
        MpcOutput queryMpcOutput = mpcOutputMapper.selectOne(queryWrapper);
        if (queryMpcOutput == null) {
            return Body.error(String.format("找不到该文件，结果id: %d", mpcOutputId));
        }
        // 判断文件是否过期
        Timestamp expiredTime = queryMpcOutput.getExpiredTime();
        if (expiredTime != null && LocalDateTime.now().isAfter(expiredTime.toLocalDateTime())) {
            return Body.error(String.format("该文件已过期，结果id: %d，文件名: %s，失效时间: %s", mpcOutputId, queryMpcOutput.getName(),
                    queryMpcOutput.getExpiredTime()));
        }

        // 添加下载任务记录到任务表
        DownloadTask newDownloadTask = new DownloadTask();
        newDownloadTask.setApplicationId(applicationId);
        newDownloadTask.setOutputId(queryMpcOutput.getUid());
        newDownloadTask.setDownloadTime(Timestamp.valueOf(LocalDateTime.now()));
        newDownloadTask.setType("mpc");
        downloadTaskMapper.insert(newDownloadTask);

        // 直接通过路径访问文件
        String filePath = queryMpcOutput.getPath();
        String fileName = queryMpcOutput.getName();
        try {
            fileService.copyFile(filePath, fileName, downloadPath);
        } catch (Exception e) {
            e.printStackTrace();
            return Body.error(String.format("获取失败: 结果id %d，文件名: %s，错误信息: %s", mpcOutputId, fileName, e.getMessage()));
        }
        return Body.success(String.format("获取成功，结果id: %d，文件名: %s", mpcOutputId, fileName));
    }

    public Body<List<MpcOutput>> queryMpc(Integer applicationId) {

        LambdaQueryWrapper<MpcOutput> queryWrapper = Wrappers.<MpcOutput>lambdaQuery()
                .eq(MpcOutput::getApplicationId, applicationId);
        List<MpcOutput> outputs = mpcOutputMapper.selectList(queryWrapper);
        Integer fileNum = outputs.size();
        return Body.success(outputs, String.format("查询成功，共查询到%d个文件", fileNum));
    }

    public Body<List<MpcOutput>> queryMpcByIds(Integer applicationId, List<Integer> mpcOutputIds) {

        LambdaQueryWrapper<MpcOutput> queryWrapper = Wrappers.<MpcOutput>lambdaQuery()
                .eq(MpcOutput::getApplicationId, applicationId)
                .in(MpcOutput::getUid, mpcOutputIds);

        List<MpcOutput> outputs = mpcOutputMapper.selectList(queryWrapper);
        Integer fileNum = outputs.size();
        return Body.success(outputs, String.format("查询成功，共查询到%d个文件", fileNum));
    }

    public Body<String> deleteMpc(Integer applicationId, Integer mpcOutputId) {

        // 根据文件id查找结果表
        LambdaQueryWrapper<MpcOutput> queryWrapper = Wrappers.<MpcOutput>lambdaQuery()
                .eq(MpcOutput::getApplicationId, applicationId)
                .eq(MpcOutput::getUid, mpcOutputId);
        MpcOutput queryMpcOutput = mpcOutputMapper.selectOne(queryWrapper);
        if (queryMpcOutput == null) {
            return Body.error(String.format("找不到该文件，结果id: %d", mpcOutputId));
        }
        String filePath = queryMpcOutput.getPath();
        String fileName = queryMpcOutput.getName();

        // 禁用外键检查
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");

        // 删除文件及结果表
        try {
            mpcOutputMapper.deleteById(mpcOutputId);
            fileService.deleteFileFromPath(filePath);
            // 启用外键检查
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
        } catch (Exception e) {
            // 确保在异常情况下重新启用外键检查
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
            e.printStackTrace();
            return Body.error(String.format("删除失败: 结果id %d，文件名: %s，错误信息: %s", mpcOutputId, fileName, e.getMessage()));
        }

        return Body.success(String.format("删除成功，结果id: %d，文件名: %s", mpcOutputId, fileName));
    }
}
