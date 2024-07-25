package DveAgent.module.directory;

import DveAgent.common.Body;
import DveAgent.entity.Application;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController // @RestController的作用等同于@Controller + @ResponseBody。
// 相当于@Controller+@ResponseBody两个注解的结合，返回json数据不需要在方法前面加@ResponseBody注解了，但使用@RestController这个注解，就不能返回jsp,html页面，视图解析器无法解析jsp,html页面
@RequestMapping("/file")
//函数签名：
//        参数：applicationID、fileID、访问方式
//        返回 boolean
//        查询一个app能否以指定方式访问一个文件

public class FileController {

    @Autowired
    FileService fileService;
    @PostMapping("/getFileByRuleOrNot")
    public Body getFileByRuleOrNot(@RequestParam("agentId") Long agentId,
                                            @RequestParam("applicationId") Long applicationID,
                                            @RequestParam("fileId") Long fileId,
                                            @RequestParam("method") String method){

        return fileService.getFileByRuleOrNot(agentId,applicationID,fileId,method);

    }

}
