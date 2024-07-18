package DveCenter.module.File;

import DveCenter.common.Body;
import DveCenter.entity.Output;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

// 定义接口路径
@RestController
@RequestMapping("/file")
public class FileController {

    @Autowired
    private FileService fileService;

    // 结果文件存储位置，比如 D:\\, 我这里使用的是本项目的路径
    private static final String BASE_DIR = "C:\\FDU\\IdeaProject\\DVE\\files\\";

    // agent文件上传接口
    @PostMapping("/upload")
    public Body<String> upload(@RequestParam MultipartFile file,
                               @RequestParam("fileId") Integer fileId,
                               @RequestParam(value = "expiredTime", required = false) java.sql.Timestamp expiredTime) {

        if (expiredTime == null) {
            // 设置默认值为当前时间的一周后
            expiredTime = java.sql.Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS));
        }

        return fileService.uploadFile(file, fileId, BASE_DIR, expiredTime);
    }


    // application文件下载接口
    @PostMapping("/download")
    public Body<String> download(@RequestParam("fileId") Integer fileId,
                                 @RequestParam("applicationId") Integer applicationId,
                                 HttpServletResponse response) {

        return fileService.downloadFile(fileId, applicationId, response);
    }


    // application文件查询接口
    @GetMapping("/query")
    public Body<List<Output>> query() {

        return fileService.queryFile();
    }
}