package DavexCenter.module.file.controller;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import DavexBase.service.notification.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import DavexBase.common.Body;
import DavexBase.common.R;
import DavexBase.entity.File;
import DavexCenter.common.CustomMultipartFile;
import DavexCenter.entity.Output;
import DavexBase.service.auth.CenterWebClientService;
import DavexCenter.module.file.service.FileService;
import reactor.core.publisher.Flux;

// 定义接口路径
@RestController
@RequestMapping("/file")
public class FileController {

    @Autowired
    private FileService fileService;

    @Value("${file.upload-base-dir}")
    private String uploadBaseDir;

    @Value("${file.download-base-dir}")
    private String downloadBaseDir;

    @Autowired
    private CenterWebClientService centerWebClientService;

    @Autowired
    private NotificationService notificationService;

    // center结果管理区保存agent文件接口
    @PostMapping("/save")
    public Body<String> save(@RequestPart("file") MultipartFile file,
                             @ModelAttribute("fileInfo") File fileInfo,
                             @RequestParam("applicationId") String applicationId,
                             @RequestParam(value = "expiredTime", required = false) java.sql.Timestamp expiredTime) {

        if (expiredTime == null) {
            // 设置默认值为当前时间的一周后
            expiredTime = java.sql.Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS));
        }

        return fileService.saveFile(file, fileInfo, applicationId, expiredTime);
    }

    // 多文件保存
    @PostMapping("/saves")
    public Body<List<String>> saves(@RequestPart("files") List<MultipartFile> files,
                                    @ModelAttribute("fileInfos") List<File> fileInfos,
                                    @RequestParam("applicationId") String applicationId,
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

        return fileService.saveFiles(files, fileInfos, applicationId, expiredTimes);
    }

    // application从center结果管理区通过Http获取文件的接口
    @PostMapping("/fetchByHttp")
    public Body<String> fetchByHttp(@RequestParam("outputId") Long outputId,
                                    @RequestParam("applicationId") String applicationId,
                                    HttpServletResponse response) {

        return fileService.fetchFileByHttp(outputId, applicationId, response);
    }

    // application通过路径直接获取center结果管理区文件的接口
    @PostMapping("/fetch")
    public Body<String> fetch(@RequestParam("outputId") Long outputId,
                              @RequestParam("applicationId") String applicationId) {

        return fileService.fetchFile(outputId, applicationId);
    }

    // application查询center结果管理区所有文件的接口
    @PostMapping("/query")
    public Body<List<Output>> query(@RequestParam("applicationId") String applicationId) {

        return fileService.queryFile(applicationId);
    }

    // application查询center结果管理区某些文件的接口
    @PostMapping("/queryByIds")
    public Body<List<Output>> queryByIds(@RequestParam("applicationId") String applicationId,
                                         @RequestParam("outputIds") List<Long> outputIds) {

        return fileService.queryFileByIds(applicationId, outputIds);
    }

    // application删除center结果管理区文件的接口
    @PostMapping("/delete")
    public Body<String> delete(@RequestParam("applicationId") String applicationId,
                               @RequestParam("outputId") Long outputId) {

        return fileService.deleteFile(applicationId, outputId);
    }

    // center向agent发送文件传输请求并调用save接口接收文件到结果管理区
    @PostMapping("/getFile")
    public CompletableFuture<Body<String>> getFile(@RequestParam("fileId") String fileId,
                                                   @RequestParam("agentId") String agentId,
                                                   @RequestParam("folderId") String folderId,
                                                   @RequestParam("applicationId") String applicationId) throws Exception {
        WebClient webclient = centerWebClientService.center2AgentWebClient(agentId);
        File fileInfo = webclient.post()
                .uri(uriBuilder -> uriBuilder.path("/directory/fileFolder/getFile")
                        .queryParam("fileId", fileId)
                        .queryParam("agentId", agentId).build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Body<File>>() {
                }).block().getData();

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

                // 创建 CustomMultipartFile，这里文件名随意
                CustomMultipartFile multipartFile = new CustomMultipartFile(fileBytes, "1.pdf");

                // 调用 save 方法
                Body<String> result = save(multipartFile, fileInfo, applicationId, null);
                // 记录消息
                String content;
                if (result.getCode() == 1) {
                    content = String.format("文件传输任务完成\n代理: %s\n文件名: %s",
                            agentId, fileInfo.getName());
                } else {
                    content = String.format("文件传输任务失败\n代理: %s\n文件名: %s\n错误信息: %s",
                            agentId, fileInfo.getName(), result.getMessage());
                }
                notificationService.setMessage(applicationId, "文件传输任务结束", content, null, result.getCode(), "fileTransfer", true);
                future.complete(result);
            } catch (IOException e) {
                future.completeExceptionally(e);
                e.printStackTrace();
            }
        });

        return future;
    }

    // 读取文件内容
    @PostMapping("/read")
    public Body<String> read(@RequestParam("applicationId") String applicationId,
                             @RequestParam("outputId") Long outputId) throws IOException {

        return fileService.readFile(applicationId, outputId);
    }
}