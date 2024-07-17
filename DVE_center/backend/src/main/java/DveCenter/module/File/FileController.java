package DveCenter.module.File;

import DveCenter.common.Body;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

// 定义接口路径
@RestController
@RequestMapping("/file")
public class FileController {

    @Autowired
    private FileService fileService;

    // 结果文件存储位置，比如 D:\\, 我这里使用的是本项目的路径
    private static final String BASE_DIR = "C:\\FDU\\IdeaProject\\DVE\\files\\";

    // 定义接口类型和二级路径，完整的接口url是：/file/upload
    @PostMapping("/upload")
    public Body<String> upload(@RequestParam MultipartFile file, @RequestParam("fileId") Integer fileId) {

        return fileService.uploadFile(file, fileId, BASE_DIR);
    }
}