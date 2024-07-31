package DveCenter.module.File;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import DveAgent.common.Body;
import DveCenter.entity.Output;

// 定义接口路径
@RestController
@RequestMapping("/file")
public class FileController {

    @Autowired
    private FileService fileService;

    // 结果文件存储位置，比如 D:\\, 我这里使用的是本项目的路径
    private static final String UPLOAD_BASE_DIR = "C:\\FDU\\IdeaProject\\DVE\\DVE_center\\Files\\";

    // agent文件上传接口
    @PostMapping("/upload")
    public Body<String> upload(@RequestParam MultipartFile file,
            @RequestParam("fileId") Long fileId,
            @RequestParam(value = "expiredTime", required = false) java.sql.Timestamp expiredTime) {

        if (expiredTime == null) {
            // 设置默认值为当前时间的一周后
            expiredTime = java.sql.Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS));
        }

        return fileService.uploadFile(file, fileId, UPLOAD_BASE_DIR, expiredTime);
    }

    @PostMapping("/uploads")
    public Body<List<String>> uploads(@RequestParam("files") List<MultipartFile> files,
            @RequestParam("fileIds") List<Long> fileIds,
            @RequestParam(value = "expiredTimes", required = false) List<java.sql.Timestamp> expiredTimes) {

        // 如果没有提供失效时间，则设置默认值为当前时间的一周后
        if (expiredTimes == null) {
            expiredTimes = files.stream()
                    .map(f -> java.sql.Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS)))
                    .collect(Collectors.toList());
        }

        // 确保失效时间的数量与文件数量匹配
        if (expiredTimes.size() != files.size()) {
            return Body.error("失效时间数量和文件数量不匹配");
        }

        return fileService.uploadFiles(files, fileIds, UPLOAD_BASE_DIR, expiredTimes);
    }

    // application文件下载接口
    @PostMapping("/download")
    public Body<String> download(@RequestParam("fileId") Long fileId,
            @RequestParam("applicationId") Long applicationId,
            HttpServletResponse response) {

        return fileService.downloadFile(fileId, applicationId, response);
    }

    // application文件查询接口
    @GetMapping("/query")
    public Body<List<Output>> query() {

        return fileService.queryFile();
    }

    // application文件删除接口
    @PostMapping("/delete")
    public Body<String> delete(@RequestParam("fileId") Integer fileId) {

        return fileService.deleteFile(fileId);
    }
}