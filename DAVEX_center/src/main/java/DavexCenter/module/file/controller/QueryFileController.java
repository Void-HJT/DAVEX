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
import DavexCenter.entity.QueryOutput;
import DavexCenter.module.file.service.QueryFileService;

// 定义接口路径
@RestController
@RequestMapping("/queryFile")
public class QueryFileController {

    @Value("${file.upload-base-dir}")
    private String uploadBaseDir;

    @Value("${file.download-base-dir}")
    private String downloadBaseDir;

    @Autowired
    private QueryFileService queryFileService;

    // 保存query文件接口
    @PostMapping("/saveQuery")
    public Body<String> saveQuery(@RequestPart("file") MultipartFile file,
                                  @RequestParam("hash") String hash,
                                  @RequestParam("applicationId") Integer applicationId,
                                  @RequestParam(value = "expiredTime", required = false) java.sql.Timestamp expiredTime) {

        if (expiredTime == null) {
            // 设置默认值为当前时间的一周后
            expiredTime = java.sql.Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS));
        }

        return queryFileService.saveQuery(file, hash, applicationId, uploadBaseDir, expiredTime);
    }

    // application通过路径直接获取center结果管理区query文件的接口
    @PostMapping("/fetchQuery")
    public Body<String> fetchQuery(@RequestParam("outputId") Integer outputId,
                                   @RequestParam("applicationId") Integer applicationId) {

        return queryFileService.fetchQuery(outputId, applicationId, downloadBaseDir);
    }

    // application查询center结果管理区所有query文件的接口
    @PostMapping("/queryQuery")
    public Body<List<QueryOutput>> queryQuery(@RequestParam("applicationId") Integer applicationId) {

        return queryFileService.queryQuery(applicationId);
    }

    // application查询center结果管理区某些query文件的接口
    @PostMapping("/queryQueryByIds")
    public Body<List<QueryOutput>> queryQueryByIds(@RequestParam("applicationId") Integer applicationId,
                                                   @RequestParam("outputIds") List<Integer> outputIds) {

        return queryFileService.queryQueryByIds(applicationId, outputIds);
    }

    // application删除center结果管理区query文件的接口
    @PostMapping("/deleteQuery")
    public Body<String> deleteQuery(@RequestParam("applicationId") Integer applicationId,
                                    @RequestParam("outputId") Integer outputId) {

        return queryFileService.deleteQuery(applicationId, outputId);
    }
}
