package DavexCenter.module.task.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import DavexBase.common.Body;
import DavexBase.common.R;
import DavexBase.entity.MpcTaskOutput;
import DavexCenter.module.task.service.MpcTaskOutputService;

@RestController
@RequestMapping("/MpcTasksOutput")
public class MpcTaskOutputController {

    @Autowired
    private MpcTaskOutputService mpcTaskOutputService;

    @GetMapping("/select")
    public String getMethodName(@RequestParam String param) {
        return new String();
    }

    @PostMapping("/save")
    public R<?> saveOutput(@RequestPart("file") MultipartFile file,
            @RequestPart("metadata") MpcTaskOutput mpcTaskOutput) {
        try {
            mpcTaskOutputService.saveOutputFromAgent(file, mpcTaskOutput);
        } catch (Exception e) {
            e.printStackTrace();
            return R.error(e.getMessage());
        }
        return R.success("保存成功");
    }

    @PostMapping("/fetch")
    public Body<String> fetchMpc(@RequestParam("mpcOutputId") Long mpcOutputId,
            @RequestParam("applicationId") String applicationId) {

        return mpcTaskOutputService.fetchMpc(mpcOutputId, applicationId);
    }

    @PostMapping("/query")
    public Body<List<MpcTaskOutput>> queryMpc(@RequestParam("applicationId") String applicationId) {

        return mpcTaskOutputService.queryMpc(applicationId);
    }

    @PostMapping("/queryByIds")
    public Body<List<MpcTaskOutput>> queryMpcByIds(@RequestParam("applicationId") String applicationId,
            @RequestParam("mpcOutputIds") List<Long> mpcOutputIds) {

        return mpcTaskOutputService.queryMpcByIds(applicationId, mpcOutputIds);
    }

    @PostMapping("/delete")
    public Body<String> deleteMpc(@RequestParam("applicationId") String applicationId,
            @RequestParam("mpcOutputId") Long mpcOutputId) {

        return mpcTaskOutputService.deleteMpc(applicationId, mpcOutputId);
    }

}
