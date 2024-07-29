package DveCenter.module.File;


import DveAgent.common.Body;
import DveAgent.module.auth.service.AgentWebClientService;
import DveCenter.common.CustomMultipartFile;
import DveCenter.entity.Output;
import DveCenter.module.auth.service.CenterWebClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

// 定义接口路径
@RestController
@RequestMapping("/file")
public class FileController {

    @Autowired
    private FileService fileService;

    // 结果文件存储位置，比如 D:\\, 我这里使用的是本项目的路径
    private static final String UPLOAD_BASE_DIR = "C:\\FDU\\IdeaProject\\DVE\\DVE_center\\Files\\";

    // application文件保存位置
    private static final String DOWNLOAD_BASE_DIR = "C:\\FDU\\IdeaProject\\ApplicationFiles\\";

    @Autowired
    private CenterWebClientService centerWebClientService;

    // center结果管理区保存agent文件接口
    @PostMapping("/save")
    public Body<String> save(@RequestParam MultipartFile file,
                               @RequestParam("fileId") Integer fileId,
                               @RequestParam("agentId") Integer agentId,
                               @RequestParam(value = "expiredTime", required = false) java.sql.Timestamp expiredTime) {

        if (expiredTime == null) {
            // 设置默认值为当前时间的一周后
            expiredTime = java.sql.Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS));
        }

        return fileService.saveFile(file, fileId, agentId, UPLOAD_BASE_DIR, expiredTime);
    }


    // 多文件保存
    @PostMapping("/saves")
    public Body<List<String>> saves(@RequestParam("files") List<MultipartFile> files,
                                     @RequestParam("fileIds") List<Integer> fileIds,
                                      @RequestParam("agentIds") List<Integer> agentIds,
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

        return fileService.saveFiles(files, fileIds, agentIds, UPLOAD_BASE_DIR, expiredTimes);
    }


    // application从center结果管理区获取文件的接口
    @PostMapping("/fetch")
    public Body<String> fetch(@RequestParam("outputId") Integer outputId,
                                 @RequestParam("applicationId") Integer applicationId,
                                 HttpServletResponse response) {

        return fileService.fetchFile(outputId, applicationId, response);
    }


    // application通过路径直接获取center结果管理区文件的接口
    @PostMapping("/fetchbypath")
    public Body<String> fetchbypath(@RequestParam("outputId") Integer outputId,
                                       @RequestParam("applicationId") Integer applicationId,
                                       String downloadPath) {

        return fileService.fetchFileByPath(outputId, applicationId, downloadPath);
    }


    // application查询center结果管理区所有文件的接口
    @GetMapping("/query")
    public Body<List<Output>> query() {

        return fileService.queryFile();
    }


    // application查询center结果管理区某些文件的接口
    @GetMapping("/querybyids")
    public Body<List<Output>> querybyids(@RequestParam("outputIds") List<Integer> outputIds) {

        return fileService.queryFileByIds(outputIds);
    }


    // application删除center结果管理区文件的接口
    @PostMapping("/delete")
    public Body<String> delete(@RequestParam("outputId") Integer outputId) {

        return fileService.deleteFile(outputId);
    }


    // center向agent发送文件传输请求并调用save接口接收文件到结果管理区
    @PostMapping("/quest")
    public CompletableFuture<Body<String>> quest(@RequestParam("fileId") Integer fileId,
                                      @RequestParam("agentId") Integer agentId,
                                      @RequestParam("folderId") Integer folderId)throws Exception {
        WebClient webclient=centerWebClientService.center2AgentWebClient(agentId);
        Flux<byte[]> fileFlux=webclient.post().uri(uriBuilder -> uriBuilder.path("/directory/sendFile")
                .queryParam("fileId",fileId)
                .queryParam("agentId",agentId)
                .queryParam("folderId",folderId).build()).accept(MediaType.APPLICATION_OCTET_STREAM).retrieve()
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
                Body<String> result = save(multipartFile, fileId, agentId, null);
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
    public CompletableFuture<Body<String>> tquest(@RequestParam("fileId") Integer fileId,
                                                 @RequestParam("agentId") Integer agentId,
                                                 @RequestParam("folderId") Integer folderId) throws Exception {
        // 这里创建一个 CompletableFuture 对象来处理异步结果
        CompletableFuture<Body<String>> future = new CompletableFuture<>();

        // 修改为从本地文件读取数据而不是从 WebClient 获取
        File localFile = new File("D:\\1.pdf"); // 这里是你的本地文件路径
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
                    Body<String> result = save(multipartFile, fileId, agentId, null);
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

}