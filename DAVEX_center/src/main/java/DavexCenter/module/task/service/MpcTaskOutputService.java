package DavexCenter.module.task.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import DavexBase.common.Body;
import DavexBase.common.My;
import DavexBase.common.Utils;
import DavexBase.entity.MpcTask;
import DavexBase.entity.MpcTaskOutput;
import DavexBase.mapper.MpcTaskOutputMapper;
import DavexCenter.entity.DownloadTask;
import DavexCenter.mapper.DownloadTaskMapper;
import DavexCenter.module.file.service.FileService;

@Service
public class MpcTaskOutputService {

    @Autowired
    private MpcTaskOutputMapper mpcTaskOutputMapper;

    @Autowired
    private My my;

    @Autowired
    private DownloadTaskMapper downloadTaskMapper;

    @Autowired
    private FileService fileService;

    public void saveOutputFromAgent(MultipartFile file, MpcTaskOutput mpcTaskOutput) throws Exception {
        LambdaQueryWrapper<MpcTaskOutput> queryWrapper = Wrappers.<MpcTaskOutput>lambdaQuery()
                .eq(MpcTaskOutput::getTaskId, mpcTaskOutput.getTaskId());
        if (mpcTaskOutputMapper.selectOne(queryWrapper) != null) {
            throw new Exception("文件已保存");
        }
        if (!Utils.verifyMultipartFileHash(file, mpcTaskOutput.getHash(), "SHA-256")) {
            throw new Exception("文件hash不匹配");
        }
        String fileName = file.getOriginalFilename();
        Path path = Paths.get(my.getBase_path()).resolve("mpctask").resolve(fileName);
        try {
            Files.createDirectories(path.getParent());
            Files.write(path, file.getBytes());
        } catch (IOException e) {
            throw e;
        }
        mpcTaskOutput.setExpiredTime(java.sql.Timestamp
                .from(Instant.now().plus(7, ChronoUnit.DAYS)));
        mpcTaskOutputMapper.insert(mpcTaskOutput);
    }

    public void saveOutputFromInner(MpcTask mpcTask) throws Exception {
        MpcTaskOutput mpcTaskOutput = new MpcTaskOutput();
        Path outputPath = Paths.get(my.getGarnet_path()).resolve("Output")
                .resolve(mpcTask.getUid() + "-P" + mpcTask.getPart() + "-0");
        Path savePath = Paths.get(my.getBase_path()).resolve("mpctask").resolve(mpcTask.getUid());
        mpcTaskOutput.setPath(Paths.get("/home/nhy/DAVEX/base/mpctask").resolve(mpcTask.getUid()).toString());
        mpcTaskOutput.setTaskId(mpcTask.getUid());
        mpcTaskOutput.setExpiredTime(java.sql.Timestamp
                .from(Instant.now().plus(7, ChronoUnit.DAYS)));
        mpcTaskOutput.setName(outputPath.getFileName().toString());
        mpcTaskOutput.setApplicationId(mpcTask.getApplicationId());
        mpcTaskOutput.setUploadDate(Timestamp.valueOf(LocalDateTime.now()));
        try {
            mpcTaskOutput.setHash(Utils.getFileHash(new FileSystemResource(outputPath), "SHA-256"));
            Files.move(outputPath, savePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            throw e;
        }
        mpcTaskOutputMapper.insert(mpcTaskOutput);
    }

    public Body<String> fetchMpc(Long mpcOutputId, Long applicationId) {
        // 根据结果id查找结果表
        LambdaQueryWrapper<MpcTaskOutput> queryWrapper = Wrappers.<MpcTaskOutput>lambdaQuery()
                .eq(MpcTaskOutput::getUid, mpcOutputId)
                .eq(MpcTaskOutput::getApplicationId, applicationId);
        MpcTaskOutput queryMpcOutput = mpcTaskOutputMapper.selectOne(queryWrapper);
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
        String downloadPath = Paths.get(my.getBase_path()).resolve("download").resolve("mpc").toString();
        try {
            fileService.copyFile(filePath, fileName, downloadPath);
        } catch (Exception e) {
            e.printStackTrace();
            return Body.error(String.format("获取失败: 结果id %d，文件名: %s，错误信息: %s", mpcOutputId, fileName, e.getMessage()));
        }
        return Body.success(String.format("获取成功，结果id: %d，文件名: %s", mpcOutputId, fileName));
    }

    public Body<List<MpcTaskOutput>> queryMpc(Long applicationId) {

        LambdaQueryWrapper<MpcTaskOutput> queryWrapper = Wrappers.<MpcTaskOutput>lambdaQuery()
                .eq(MpcTaskOutput::getApplicationId, applicationId);
        List<MpcTaskOutput> outputs = mpcTaskOutputMapper.selectList(queryWrapper);
        Integer fileNum = outputs.size();
        return Body.success(outputs, String.format("查询成功，共查询到%d个文件", fileNum));
    }

    public Body<List<MpcTaskOutput>> queryMpcByIds(Long applicationId, List<Long> mpcOutputIds) {

        LambdaQueryWrapper<MpcTaskOutput> queryWrapper = Wrappers.<MpcTaskOutput>lambdaQuery()
                .eq(MpcTaskOutput::getApplicationId, applicationId)
                .in(MpcTaskOutput::getUid, mpcOutputIds);

        List<MpcTaskOutput> outputs = mpcTaskOutputMapper.selectList(queryWrapper);
        Integer fileNum = outputs.size();
        return Body.success(outputs, String.format("查询成功，共查询到%d个文件", fileNum));
    }

    public Body<String> deleteMpc(Long applicationId, Long mpcOutputId) {
        // 根据文件id查找结果表
        LambdaQueryWrapper<MpcTaskOutput> queryWrapper = Wrappers.<MpcTaskOutput>lambdaQuery()
                .eq(MpcTaskOutput::getApplicationId, applicationId)
                .eq(MpcTaskOutput::getUid, mpcOutputId);
        MpcTaskOutput queryMpcOutput = mpcTaskOutputMapper.selectOne(queryWrapper);
        if (queryMpcOutput == null) {
            return Body.error(String.format("找不到该文件，结果id: %d", mpcOutputId));
        }
        String filePath = queryMpcOutput.getPath();
        String fileName = queryMpcOutput.getName();

        // 删除文件及结果表
        try {
            mpcTaskOutputMapper.deleteById(mpcOutputId);
            fileService.deleteFileFromPath(filePath);
        } catch (Exception e) {
            e.printStackTrace();
            return Body.error(String.format("删除失败: 结果id %d，文件名: %s，错误信息: %s", mpcOutputId, fileName, e.getMessage()));
        }
        return Body.success(String.format("删除成功，结果id: %d，文件名: %s", mpcOutputId, fileName));
    }
}
