package DavexCenter.module.file.controller;

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
import DavexCenter.entity.FlOutput;
import DavexCenter.module.file.service.FlFileService;

// 定义接口路径
@RestController
@RequestMapping("/flFile")
public class FlFileController {

    @Value("${file.upload-base-dir}")
    private String uploadBaseDir;

    @Value("${file.download-base-dir}")
    private String downloadBaseDir;

    @Autowired
    private FlFileService flFileService;

    // 保存联邦学习结果文件接口
    @PostMapping("/saveFl")
    public Body<String> saveFl(@RequestPart("file") MultipartFile file,
                                       @RequestParam("hash") String hash,
                                       @RequestParam("applicationId") String applicationId,
                                       @RequestParam(value = "expiredTime", required = false) java.sql.Timestamp expiredTime) {

        if (expiredTime == null) {
            // 设置默认值为当前时间的一周后
            expiredTime = java.sql.Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS));
        }

        return flFileService.saveFl(file, hash, applicationId, uploadBaseDir, expiredTime);
    }

    // application通过路径直接获取center结果管理区联邦学习结果文件的接口
    @PostMapping("/fetchFl")
    public Body<String> fetchFl(@RequestParam("outputId") Long outputId,
                                        @RequestParam("applicationId") String applicationId) {

        return flFileService.fetchFl(outputId, applicationId, downloadBaseDir);
    }

    // application查询center结果管理区所有联邦学习结果文件的接口
    @PostMapping("/queryFl")
    public Body<List<FlOutput>> query(@RequestParam("applicationId") String applicationId) {

        return flFileService.queryFl(applicationId);
    }

    // application查询center结果管理区某些联邦学习结果文件的接口
    @PostMapping("/queryFlByIds")
    public Body<List<FlOutput>> queryFlByIds(@RequestParam("applicationId") String applicationId,
                                                             @RequestParam("outputIds") List<Long> outputIds) {

        return flFileService.queryFlByIds(applicationId, outputIds);
    }

    // application删除center结果管理区联邦学习结果文件的接口
    @PostMapping("/deleteFl")
    public Body<String> deleteFl(@RequestParam("applicationId") String applicationId,
                                         @RequestParam("outputId") Long outputId) {

        return flFileService.deleteFl(applicationId, outputId);
    }
}
