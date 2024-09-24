package DavexBase.service.MQ;

import DavexBase.entity.Application;
import DavexBase.mapper.ApplicationMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageService {
    @Autowired
    private ApplicationMapper applicationMapper;

    // 使用 Jackson ObjectMapper
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    public MessageService() {
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public void handleMessage(String message) {
        if(message.startsWith("Create")||message.startsWith("Delete")||message.startsWith("Update")) {


            String[] parts = message.split(":", 2);  // 按操作类型和 JSON 部分拆分消息
            String operation = parts[0];
            String jsonPart = parts[1];

            try {

                // 将 JSON 部分反序列化为 Application 对象
                Application application = objectMapper.readValue(jsonPart, Application.class);

                switch (operation) {
                    case "Create":
                        // 数据库添加
                        applicationMapper.insert(application);
                        System.out.println("成功添加" + application);
                        break;
                    case "Delete":
                        // 数据库删除
                        applicationMapper.deleteById(application.getUid());
                        System.out.println("成功删除" + application);
                        break;
                    case "Update":
                        // 数据库更新
                        applicationMapper.updateById(application);
                        System.out.println("成功更新" + application);
                        break;
                    default:
                        System.out.println("Message: " + message);
                        break;
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to handle message: " + e.getMessage(), e);
            }
        }
        else{System.out.println("Message: " + message);}
    }
}
