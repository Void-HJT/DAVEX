package DveCenter.module.file;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import DveBase.entity.Mpc;
import DveBase.entity.MpcTaskOutput;
import DveBase.mapper.MpcTaskOutputMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import DveBase.common.Body;
import DveBase.common.R;
import DveBase.entity.File;
import DveCenter.common.CustomMultipartFile;
import DveCenter.entity.Output;
import DveCenter.module.auth.service.CenterWebClientService;
import reactor.core.publisher.Flux;

// 定义接口路径
@RestController
@RequestMapping("/file")
public class FileController {

    @Autowired
    private FileService fileService;

    // 结果文件存储位置，比如 D:\\
//    private static final String UPLOAD_BASE_DIR = "C:\\FDU\\IdeaProject\\Files\\";
//    private static final String UPLOAD_BASE_DIR = "/home/zkx/DAVE/Files/";

    // application文件保存位置
//    private static final String DOWNLOAD_BASE_DIR = "C:\\FDU\\IdeaProject\\ApplicationFiles\\";
//    private static final String DOWLLOAD_BASE_DIR = "/home/zkx/DAVE/ApplicationFiles/";

    @Value("${file.upload-base-dir}")
    private String uploadBaseDir;

    @Value("${file.download-base-dir}")
    private String downloadBaseDir;

    @Autowired
    private CenterWebClientService centerWebClientService;

    // center结果管理区保存agent文件接口
    @PostMapping("/save")
    public Body<String> save(@RequestParam MultipartFile file,
                             @ModelAttribute File fileInfo,
                             @RequestParam("applicationId") Integer applicationId,
                               @RequestParam(value = "expiredTime", required = false) java.sql.Timestamp expiredTime) {

        if (expiredTime == null) {
            // 设置默认值为当前时间的一周后
            expiredTime = java.sql.Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS));
        }

        return fileService.saveFile(file, fileInfo, applicationId, uploadBaseDir, expiredTime);
    }

    // 多文件保存
    @PostMapping("/saves")
    public Body<List<String>> saves(@RequestParam("files") List<MultipartFile> files,
                                    @ModelAttribute List<File> fileInfos,
                                    @RequestParam("applicationId") Integer applicationId,
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

        return fileService.saveFiles(files, fileInfos, applicationId, uploadBaseDir, expiredTimes);
    }

    // application从center结果管理区获取文件的接口
    @PostMapping("/fetch")
    public Body<String> fetch(@RequestParam("outputId") Integer outputId,
                                 @RequestParam("applicationId") Long applicationId,
                                 HttpServletResponse response) {

        return fileService.fetchFile(outputId, applicationId, response);
    }

    // application通过路径直接获取center结果管理区文件的接口
    @PostMapping("/fetchbypath")
    public Body<String> fetchbypath(@RequestParam("outputId") Integer outputId,
                                       @RequestParam("applicationId") Long applicationId,
                                    @RequestParam String downloadPath) {

        return fileService.fetchFileByPath(outputId, applicationId, downloadPath);
    }

    // application查询center结果管理区所有文件的接口
    @GetMapping("/query")
    public Body<List<Output>> query(@RequestParam("applicationId") Integer applicationId) {

        return fileService.queryFile(applicationId);
    }

    // application查询center结果管理区某些文件的接口
    @GetMapping("/querybyids")
    public Body<List<Output>> querybyids(@RequestParam("applicationId") Integer applicationId,
                                         @RequestParam("outputIds") List<Integer> outputIds) {

        return fileService.queryFileByIds(applicationId, outputIds);
    }

    // application删除center结果管理区文件的接口
    @PostMapping("/delete")
    public Body<String> delete(@RequestParam("applicationId") Integer applicationId,
                               @RequestParam("outputId") Integer outputId) {

        return fileService.deleteFile(applicationId, outputId);
    }

    // center向agent发送文件传输请求并调用save接口接收文件到结果管理区
    @PostMapping("/quest")
    public CompletableFuture<Body<String>> quest(@RequestParam("fileId") Integer fileId,
                                      @RequestParam("agentId") Integer agentId,
                                      @RequestParam("folderId") Integer folderId,
                                                 @RequestParam("applicationId") Integer applicationId) throws Exception {
        WebClient webclient = centerWebClientService.center2AgentWebClient(agentId);
        File fileInfo = webclient.post()
                .uri(uriBuilder -> uriBuilder.path("/directory/fileFolder/getFile")
                        .queryParam("fileId", fileId)
                        .queryParam("agentId", agentId).build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<R<File>>() {
                }).block().getBody().getData();

        Flux<byte[]> fileFlux = webclient.post().uri(uriBuilder -> uriBuilder.path("/directory/fileFolder/sendFile")
                .queryParam("fileId", fileId)
                .queryParam("agentId", agentId)
                .queryParam("folderId", folderId).build()).accept(MediaType.APPLICATION_OCTET_STREAM).retrieve()
                .bodyToFlux(byte[].class);

        // 这里创建一个 CompletableFuture 对象来处理异步结果
        CompletableFuture<Body<String>> future = new CompletableFuture<>();

        fileFlux.collectList().subscribe(bytesList -> {
//            try (FileOutputStream fos = new FileOutputStream(new File("D:\\1.pdf"))) {
//                for (byte[] bytes : bytesList) {
//                    fos.write(bytes);
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            try {
                // 将字节数组列表合并为一个完整的字节数组
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                for (byte[] bytes : bytesList) {
                    byteArrayOutputStream.write(bytes);
                }
                byte[] fileBytes = byteArrayOutputStream.toByteArray();

                // 创建 CustomMultipartFile
                CustomMultipartFile multipartFile = new CustomMultipartFile(fileBytes, "1.pdf");

                // 调用 save 方法
                Body<String> result = save(multipartFile, fileInfo, applicationId, null);
                future.complete(result);
            } catch (IOException e) {
                future.completeExceptionally(e);
                e.printStackTrace();
            }
        });

        return future;
    }

    // 测试quest接口
    @PostMapping("/tquest")
    public CompletableFuture<Body<String>> tquest(@RequestParam("applicationId") Integer applicationId) throws Exception {
        // 这里创建一个 CompletableFuture 对象来处理异步结果
        CompletableFuture<Body<String>> future = new CompletableFuture<>();

        // 修改为从本地文件读取数据而不是从 WebClient 获取
        File fileInfo = new File();
        fileInfo.setUid(5L);
        fileInfo.setAgentId(5L);
        fileInfo.setFolderId(10L);
        fileInfo.setName("安全多方学习_从安全计算到安全学习_韩伟力.pdf");
        fileInfo.setType("pdf");
        fileInfo.setCreateDate(Timestamp.valueOf("2024-07-21 09:39:09"));
        fileInfo.setLastUpdate(Timestamp.valueOf("2024-07-21 09:39:09"));
        fileInfo.setSize(3087316L);
        fileInfo.setHash("759771dabfb5ceddc7339a3d2d72ad208e963ec029e2819bf8d1f099b6d17971");
        java.io.File localFile = new java.io.File("/home/zkx/DAVE/1.pdf");
        try (FileInputStream fis = new FileInputStream(localFile)) {
            // 将文件内容读取到字节数组中
            byte[] fileBytes = fis.readAllBytes();

            // 将字节数组分成多个字节数组以模拟 Flux<byte[]> 的行为
            // 这里只用一个字节数组模拟，如果需要更复杂的逻辑，可以自行调整
            Flux<byte[]> fileFlux = Flux.just(fileBytes);

            fileFlux.collectList().subscribe(bytesList -> {
                try {
                    // 将字节数组列表合并为一个完整的字节数组
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    for (byte[] bytes : bytesList) {
                        byteArrayOutputStream.write(bytes);
                    }
                    byte[] fileBytesArray = byteArrayOutputStream.toByteArray();

                    // 创建 CustomMultipartFile
                    CustomMultipartFile multipartFile = new CustomMultipartFile(fileBytesArray, "1.pdf");

                    // 调用 save 方法
                    Body<String> result = save(multipartFile, fileInfo, applicationId, null);
                    future.complete(result);
                } catch (IOException e) {
                    future.completeExceptionally(e);
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            future.completeExceptionally(e);
            e.printStackTrace();
        }

        return future;
    }

    // 保存mpc文件接口
    @PostMapping("/savempc")
    public Body<String> savempc(@RequestPart MultipartFile file,
                                @ModelAttribute MpcTaskOutput mpcInfo,
                                @RequestParam("applicationId") Long applicationId,
                                @RequestParam(value = "expiredTime", required = false) java.sql.Timestamp expiredTime) {

        if (expiredTime == null) {
            // 设置默认值为当前时间的一周后
            expiredTime = java.sql.Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS));
        }

        return fileService.saveMpcFile(file, mpcInfo, applicationId, uploadBaseDir, expiredTime);
    }

    // 测试savempc
    @PostMapping("/tsavempc")
    public Body<String> tsavempc(@RequestPart MultipartFile file,
                                 @RequestParam("applicationId") Long applicationId) {

        MpcTaskOutput mpcInfo = new MpcTaskOutput();
        mpcInfo.setUid(5L);
        mpcInfo.setTaskId("abc");
        mpcInfo.setHash("1c5826f1a679f10eb364dfb30baa36c9d1560b7739e3975d2caeaa7c74d0cb25");
        mpcInfo.setPath("C:\train.csv");
        mpcInfo.setUploadDate(null);
        mpcInfo.setName("train.csv");

        return savempc(file, mpcInfo, applicationId, null);
    }
}