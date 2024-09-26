package DavexBase.service.MQ;

import DavexBase.common.Body;
import DavexBase.entity.RabbitmqConnection;
import DavexBase.mapper.RabbitmqConnectionMapper;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class SubscribeService {

    private final Map<String, SimpleMessageListenerContainer> subscriptionContainers;
    private final Map<String, CachingConnectionFactory> connectionFactories;  // 用于存储每个队列的连接工厂

    @Autowired
    RabbitmqConnectionMapper rabbitMQConnectionMapper;

    @Autowired
    public SubscribeService() {
        this.subscriptionContainers = new HashMap<>();
        this.connectionFactories = new HashMap<>();
    }

    public Body<String> subscribeToCenter(String centerId, String queueName, String routingKey, Object messageListener) {
        // 检查是否已经存在该队列的订阅
        if (subscriptionContainers.containsKey(queueName)) {
            return Body.error("Already subscribed to queue: " + queueName);
        }

        RabbitmqConnection connectionInfo = rabbitMQConnectionMapper.selectById(centerId);
        if (connectionInfo == null) {
            return Body.error("No RabbitMQ connection found for centerId: " + centerId);
        }

        CachingConnectionFactory factory = null;
        SimpleMessageListenerContainer container = null;

        try {
            // 创建连接工厂
            factory = new CachingConnectionFactory(connectionInfo.getHost(), connectionInfo.getPort());
            factory.setUsername(connectionInfo.getUsername());
            factory.setPassword(connectionInfo.getPassword());
            factory.setVirtualHost(connectionInfo.getVirtualHost());

            // 检查队列是否存在
            if (!checkQueueExists(factory, queueName)) {
                // 如果队列不存在，销毁连接工厂，避免残留连接
                factory.destroy();  // 销毁连接工厂，确保连接关闭
                return Body.error("Queue " + queueName + " does not exist on center " + centerId);
            }

            // 创建订阅容器
            container = new SimpleMessageListenerContainer();
            container.setConnectionFactory(factory);
            container.setQueueNames(queueName);
            container.setMessageListener(new MessageListenerAdapter(messageListener, "handleMessage"));

            // 启动订阅
            container.start();
            subscriptionContainers.put(queueName, container);
            connectionFactories.put(queueName, factory);  // 保存连接工厂
            return Body.success("Subscribed to " + routingKey + " on center " + centerId);

        } catch (Exception e) {
            // 捕获异常并记录日志
            System.err.println("Failed to subscribe to center: " + e.getMessage());
            if (container != null) {
                container.stop();
            }
            if (factory != null) {
                factory.destroy();  // 销毁连接，确保不会在 RabbitMQ Management 中显示
            }
            return Body.error("Subscription failed for queue: " + queueName);
        }
    }

    public Body<String> unsubscribe(String queueName) {
        SimpleMessageListenerContainer container = subscriptionContainers.remove(queueName);
        if (container != null) {
            container.stop();
            CachingConnectionFactory factory = connectionFactories.remove(queueName);
            if (factory != null) {
                factory.destroy();  // 销毁连接，确保在 RabbitMQ Management 中不再显示
            }
            return Body.success("Successfully unsubscribed from queue: " + queueName);
        } else {
            // 如果队列名不在订阅列表中，抛出异常或返回提示
            return Body.error("No active subscription found for queue: " + queueName);
        }
    }

    public boolean checkQueueExists(CachingConnectionFactory factory, String queueName) {
        try {
            // 使用 RabbitTemplate 来被动检查队列是否存在
            RabbitTemplate rabbitTemplate = new RabbitTemplate(factory);
            rabbitTemplate.execute(channel -> {
                channel.queueDeclarePassive(queueName);  // 被动声明队列，检查队列是否存在
                return true;
            });
            return true;
        } catch (Exception e) {
            // 捕获队列不存在的异常
            System.err.println("Queue " + queueName + " does not exist: " + e.getMessage());
            return false;
        }
    }
}
