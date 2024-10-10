package DavexAgent.module.task.controller;

import java.io.FileNotFoundException;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import DavexBase.common.My;
import DavexBase.common.R;
import DavexBase.entity.Mpc;
import DavexBase.mapper.MpcMapper;

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
    public R<Mpc> select(@RequestParam Long MpcID) {
        LambdaQueryWrapper<Mpc> queryWrapper = Wrappers.<Mpc>lambdaQuery().eq(Mpc::getUid, MpcID);
        return R.success(mpcMapper.selectOne(queryWrapper), "查询成功");
    }

    @PostMapping("/create")
    public R<Mpc> create(@RequestBody Mpc mpc) {
        try {
            mpcMapper.insert(mpc);
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
        return R.success(mpc, "创建成功");
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

}
