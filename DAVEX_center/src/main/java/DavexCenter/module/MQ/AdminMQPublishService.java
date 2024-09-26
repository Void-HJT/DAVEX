package DavexCenter.module.MQ;

import DavexBase.common.Body;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Service
public class AdminMQPublishService {

    private final RabbitTemplate rabbitTemplate;
    private final RabbitAdmin rabbitAdmin;

    @Autowired
    public AdminMQPublishService(CachingConnectionFactory connectionFactory) {
        this.rabbitTemplate = new RabbitTemplate(connectionFactory);
        this.rabbitAdmin = new RabbitAdmin(connectionFactory);
    }

    // 从 application.yml 中读取 RabbitMQ 连接信息
    @Value("${spring.rabbitmq.host}")
    private String rabbitmqHost;

    @Value("${spring.rabbitmq.port}")
    private int rabbitmqPort;

    @Value("${spring.rabbitmq.username}")
    private String rabbitmqUsername;

    @Value("${spring.rabbitmq.password}")
    private String rabbitmqPassword;

    private String apiBaseUrl;

    @PostConstruct
    public void init() {
        this.apiBaseUrl = "http://" + rabbitmqHost + ":15672/";
    }

    /**
     * 检查队列是否存在
     */
    private boolean isQueueExists(String queueName) {
        try {
            String url = apiBaseUrl + "#/queues/%2F/" + queueName;  // %2F 表示虚拟主机 "/"
            RestTemplate restTemplate = new RestTemplate();

            // 配置HTTP基本认证
            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(rabbitmqUsername, rabbitmqPassword);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // 调用 RabbitMQ 管理 API 来检查队列是否存在
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            // 如果请求成功，则队列存在
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            // 如果出现异常，说明队列不存在或者请求出错
            return false;
        }
    }

    /**
     * 检查交换机是否存在
     */
    private boolean isExchangeExists(String exchangeName) {
        try {
            String url = apiBaseUrl + "#/exchanges/%2F/" + exchangeName;  // %2F 表示虚拟主机 "/"
            RestTemplate restTemplate = new RestTemplate();

            // 配置HTTP基本认证
            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(rabbitmqUsername, rabbitmqPassword);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // 调用 RabbitMQ 管理 API 来检查交换机是否存在
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            // 如果请求成功，则交换机存在
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            // 如果出现异常，说明交换机不存在或者请求出错
            return false;
        }
    }

    /**
     * 获取所有交换机
     */
    public Body<List<String>> listExchanges() {
        try {
            String url = apiBaseUrl + "api/exchanges";
            RestTemplate restTemplate = new RestTemplate();

            // 配置HTTP基本认证
            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(rabbitmqUsername, rabbitmqPassword);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // 获取所有交换机信息
            ResponseEntity<Object[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, Object[].class);

            List<String> exchangeNames = new ArrayList<>();
            for (Object exchange : response.getBody()) {
                Map<String, Object> exchangeData = (Map<String, Object>) exchange;
                exchangeNames.add((String) exchangeData.get("name"));
            }

            return Body.success(exchangeNames,"交换机信息");
        } catch (Exception e) {
            return Body.error("Failed to retrieve exchanges: " + e.getMessage());
        }
    }

    /**
     * 获取某个交换机绑定的所有队列
     */
    public Body<List<String>> listExchangeQueues(String exchangeName) {
        try {
            String url = apiBaseUrl + "api/bindings";
            RestTemplate restTemplate = new RestTemplate();

            // 配置HTTP基本认证
            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(rabbitmqUsername, rabbitmqPassword);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // 获取所有绑定信息
            ResponseEntity<Object[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, Object[].class);

            List<String> queueNames = new ArrayList<>();
            for (Object binding : response.getBody()) {
                Map<String, Object> bindingData = (Map<String, Object>) binding;
                String source = (String) bindingData.get("source");
                String destinationType = (String) bindingData.get("destination_type");

                // 过滤与该交换机绑定的队列
                if (exchangeName.equals(source) && "queue".equals(destinationType)) {
                    queueNames.add((String) bindingData.get("destination"));
                }
            }

            return Body.success(queueNames,"队列信息");
        } catch (Exception e) {
            return Body.error("Failed to retrieve queues for exchange: " + e.getMessage());
        }
    }



    /**
     * 新增队列并绑定到 Direct 交换机
     */
    public Body<String> addQueueAndBindToDirect(String queueName, String exchangeName, String routingKey) {
        try {
            if (isQueueExists(queueName)) {
                return Body.error("Queue " + queueName + " already exists.");
            }

            if (!isExchangeExists(exchangeName)) {
                return Body.error("Exchange " + exchangeName + " does not exist.");
            }

            Queue queue = new Queue(queueName, true); // 持久化队列
            rabbitAdmin.declareQueue(queue);

            DirectExchange exchange = new DirectExchange(exchangeName);
            rabbitAdmin.declareExchange(exchange);

            Binding binding = BindingBuilder.bind(queue).to(exchange).with(routingKey);
            rabbitAdmin.declareBinding(binding);

            return Body.success("Queue " + queueName + " successfully created and bound to Direct exchange: " + exchangeName);
        } catch (Exception e) {
            return Body.error("Failed to create and bind queue: " + e.getMessage());
        }
    }

    /**
     * 新增队列并绑定到 Topic 交换机
     */
    public Body<String> addQueueAndBindToTopic(String queueName, String exchangeName, String routingKey) {
        try {
            if (isQueueExists(queueName)) {
                return Body.error("Queue " + queueName + " already exists.");
            }

            if (!isExchangeExists(exchangeName)) {
                return Body.error("Exchange " + exchangeName + " does not exist.");
            }

            Queue queue = new Queue(queueName, true); // 持久化队列
            rabbitAdmin.declareQueue(queue);

            TopicExchange exchange = new TopicExchange(exchangeName);
            rabbitAdmin.declareExchange(exchange);

            Binding binding = BindingBuilder.bind(queue).to(exchange).with(routingKey);
            rabbitAdmin.declareBinding(binding);

            return Body.success("Queue " + queueName + " successfully created and bound to Topic exchange: " + exchangeName);
        } catch (Exception e) {
            return Body.error("Failed to create and bind queue: " + e.getMessage());
        }
    }

    /**
     * 新增队列并绑定到 Fanout 交换机
     */
    public Body<String> addQueueAndBindToFanout(String queueName, String exchangeName) {
        try {
//            if (isQueueExists(queueName)) {
//                return Body.error("Queue " + queueName + " already exists.");
//            }
//
//            if (!isExchangeExists(exchangeName)) {
//                return Body.error("Exchange " + exchangeName + " does not exist.");
//            }

            Queue queue = new Queue(queueName, true); // 持久化队列
            rabbitAdmin.declareQueue(queue);

            FanoutExchange exchange = new FanoutExchange(exchangeName);
            rabbitAdmin.declareExchange(exchange);

            Binding binding = BindingBuilder.bind(queue).to(exchange);
            rabbitAdmin.declareBinding(binding);

            return Body.success("Queue " + queueName + " successfully created and bound to Fanout exchange: " + exchangeName);
        } catch (Exception e) {
            return Body.error("Failed to create and bind queue: " + e.getMessage());
        }
    }

    /**
     * 检查队列是否存在，发送消息前检查
     */
    public Body<String> publishMessageToDirect(String exchange, String routingKey, String message) {
        if (!isQueueExists(routingKey)) {
            return Body.error("Queue " + routingKey + " does not exist.");
        }
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, message);
            return Body.success("Message sent successfully to exchange: " + exchange + " with routing key: " + routingKey);
        } catch (Exception e) {
            return Body.error("Failed to send message: " + e.getMessage());
        }
    }

    public Body<String> publishMessageToTopic(String exchange, String routingKey, String message) {
        if (!isQueueExists(routingKey)) {
            return Body.error("Queue " + routingKey + " does not exist.");
        }
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, message);
            return Body.success("Message sent successfully to exchange: " + exchange + " with routing key: " + routingKey);
        } catch (Exception e) {
            return Body.error("Failed to send message: " + e.getMessage());
        }
    }

    public Body<String> publishMessageToFanout(String exchange, String message) {

        // 声明交换机为fanout类型
        FanoutExchange fanoutExchange = new FanoutExchange(exchange, true, false);
        rabbitAdmin.declareExchange(fanoutExchange);

        if (!isExchangeExists(exchange)) {
            return Body.error("Exchange " + exchange + " does not exist.");
        }
        try {
            rabbitTemplate.convertAndSend(exchange,"",message);
            return Body.success("Message sent successfully to exchange: " + exchange);
        } catch (Exception e) {
            return Body.error("Failed to send message: " + e.getMessage());
        }
    }

}
