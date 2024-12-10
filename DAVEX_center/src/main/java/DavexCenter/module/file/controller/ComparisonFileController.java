package DavexCenter.module.file.controller;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import DavexBase.common.Body;
import DavexCenter.entity.ComparisonOutput;
import DavexCenter.module.file.service.ComparisonFileService;

// 定义接口路径
@RestController
@RequestMapping("/comparisonFile")
public class ComparisonFileController {

    @Value("${file.upload-base-dir}")
    private String uploadBaseDir;

    @Value("${file.download-base-dir}")
    private String downloadBaseDir;

    @Autowired
    private ComparisonFileService comparisonFileService;

    // 保存comparison文件接口
    @PostMapping("/saveComparison")
    public Body<String> saveComparison(@RequestPart("file") MultipartFile file,
                                       @RequestParam("hash") String hash,
                                       @RequestParam("applicationId") String applicationId,
                                       @RequestParam("agentId") String agentId,
                                       @RequestParam("fileId") String fileId,
                                       @RequestParam("folderId") String folderId,
                                       @RequestParam("fileName") String fileName,
                                       @RequestParam(value = "expiredTime", required = false) java.sql.Timestamp expiredTime) {

        if (expiredTime == null) {
            // 设置默认值为当前时间的一周后
            expiredTime = java.sql.Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS));
        }

        return comparisonFileService.saveComparison(file, hash, applicationId, agentId, fileId, folderId, fileName, expiredTime);
    }

    // application通过路径直接获取center结果管理区comparison文件的接口
    @PostMapping("/fetchComparison")
    public Body<String> fetchComparison(@RequestParam("outputId") Long outputId,
                                        @RequestParam("applicationId") String applicationId) {

        return comparisonFileService.fetchComparison(outputId, applicationId);
    }

    // application查询center结果管理区所有comparison文件的接口
    @PostMapping("/queryComparison")
    public Body<List<ComparisonOutput>> query(@RequestParam("applicationId") String applicationId) {

        return comparisonFileService.queryComparison(applicationId);
    }

    // application查询center结果管理区某些comparison文件的接口
    @PostMapping("/queryComparisonByIds")
    public Body<List<ComparisonOutput>> queryComparisonByIds(@RequestParam("applicationId") String applicationId,
                                                             @RequestParam("outputIds") List<Long> outputIds) {

        return comparisonFileService.queryComparisonByIds(applicationId, outputIds);
    }

    // application删除center结果管理区comparison文件的接口
    @PostMapping("/deleteComparison")
    public Body<String> deleteComparison(@RequestParam("applicationId") String applicationId,
                                         @RequestParam("outputId") Long outputId) {

        return comparisonFileService.deleteComparison(applicationId, outputId);
    }

    // 读取文件内容
    @PostMapping("/readComparison")
    public Body<String> readComparison(@RequestParam("applicationId") String applicationId,
                                       @RequestParam("outputId") Long outputId) throws IOException {

        return comparisonFileService.readComparison(applicationId, outputId);
    }
}
