package DveCenter.module.task.controller;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import DveBase.common.My;
import DveBase.common.R;
import DveBase.common.Utils;
import DveBase.entity.Mpc;
import DveBase.mapper.MpcMapper;

@RestController
@RequestMapping("/Mpc")
public class MpcController {
    @Autowired
    My my;

    @Autowired
    MpcMapper mpcMapper;

    @GetMapping("/list")
    public R<List<Mpc>> list() {
        LambdaQueryWrapper<Mpc> queryWrapper = Wrappers.<Mpc>lambdaQuery();
        return R.success(mpcMapper.selectList(queryWrapper), "查询成功");
    }

    @GetMapping("/select")
    public R<Mpc> select(@RequestParam String MpcID) {
        LambdaQueryWrapper<Mpc> queryWrapper = Wrappers.<Mpc>lambdaQuery().eq(Mpc::getUid, MpcID);
        return R.success(mpcMapper.selectOne(queryWrapper), "查询成功");
    }

    @GetMapping("/download")
    public ResponseEntity<Resource> download(@RequestParam String MpcID) throws Exception {
        Path filePath = Paths.get(my.getBase_path()).resolve(mpcMapper.selectById(MpcID).getPath());
        Resource resource = new UrlResource(filePath.toUri());
        if (!resource.exists()) {
            throw new FileNotFoundException("File not found: " + filePath);
        }
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filePath.getFileName().toString() + "\"")
                .body(resource);
    }

    @PostMapping("/create")
    public R<Mpc> create(@RequestPart("file") MultipartFile file, @RequestPart Mpc mpc) {
        String fileName = file.getOriginalFilename();
        Path path = Utils.resolveFileNameConflict(Paths.get(my.getBase_path()).resolve("programs").resolve(fileName));
        mpc.setPath(Paths.get("programs").resolve(path.getFileName()).toString());
        try {
            mpcMapper.insert(mpc);
            Files.write(path, file.getBytes());
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
        return R.success(mpc, "创建成功");
    }

}
