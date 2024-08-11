package DveCenter.module.file.controller;

import DveBase.common.Body;
import DveBase.entity.MpcTaskOutput;
import DveCenter.entity.MpcOutput;
import DveCenter.module.file.service.FileService;
import DveCenter.module.file.service.MpcFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

// 定义接口路径
@RestController
@RequestMapping("/mpcFile")
public class MpcFileController {

    @Value("${file.upload-base-dir}")
    private String uploadBaseDir;

    @Value("${file.download-base-dir}")
    private String downloadBaseDir;

    @Autowired
    private MpcFileService mpcFileService;

    // 保存mpc文件接口
    @PostMapping("/saveMpc")
    public Body<String> saveMpc(@RequestPart("file") MultipartFile file,
                                @ModelAttribute("mpcInfo") MpcTaskOutput mpcInfo,
                                @RequestParam("applicationId") Long applicationId,
                                @RequestParam(value = "expiredTime", required = false) java.sql.Timestamp expiredTime) {

        if (expiredTime == null) {
            // 设置默认值为当前时间的一周后
            expiredTime = java.sql.Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS));
        }

        return mpcFileService.saveMpcFile(file, mpcInfo, applicationId, uploadBaseDir, expiredTime);
    }

    // 测试savempc
    @PostMapping("/tsaveMpc")
    public Body<String> tsaveMpc(@RequestPart("file") MultipartFile file,
                                 @RequestParam("applicationId") Long applicationId) {

        MpcTaskOutput mpcInfo = new MpcTaskOutput();
        mpcInfo.setUid(5L);
        mpcInfo.setTaskId("abc");
        mpcInfo.setHash("1c5826f1a679f10eb364dfb30baa36c9d1560b7739e3975d2caeaa7c74d0cb25");
        mpcInfo.setPath("C:\train.csv");
        mpcInfo.setUploadDate(null);

        return saveMpc(file, mpcInfo, applicationId, null);
    }

    // application通过路径直接获取center结果管理区mpc文件的接口
    @PostMapping("/fetchMpc")
    public Body<String> fetchMpc(@RequestParam("mpcOutputId") Integer mpcOutputId,
                                 @RequestParam("applicationId") Long applicationId) {

        return mpcFileService.fetchMpc(mpcOutputId, applicationId, downloadBaseDir);
    }

    // application查询center结果管理区所mpc有文件的接口
    @GetMapping("/queryMpc")
    public Body<List<MpcOutput>> queryMpc(@RequestParam("applicationId") Integer applicationId) {

        return mpcFileService.queryMpc(applicationId);
    }

    // application查询center结果管理区某些mpc文件的接口
    @GetMapping("/queryMpcByIds")
    public Body<List<MpcOutput>> queryMpcByIds(@RequestParam("applicationId") Integer applicationId,
                                               @RequestParam("mpcOutputIds") List<Integer> mpcOutputIds) {

        return mpcFileService.queryMpcByIds(applicationId, mpcOutputIds);
    }

    // application删除center结果管理区mpc文件的接口
    @PostMapping("/deleteMpc")
    public Body<String> deleteMpc(@RequestParam("applicationId") Integer applicationId,
                                  @RequestParam("mpcOutputId") Integer mpcOutputId) {

        return mpcFileService.deleteMpc(applicationId, mpcOutputId);
    }
}
