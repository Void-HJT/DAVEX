package DavexAgent.module.MQ;

import DavexBase.common.Body;
import DavexBase.entity.RabbitmqConnection;
import DavexBase.mapper.RabbitmqConnectionMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.HashMap;
import java.util.Map;

@Service
public class AgentPublishService {

    // 存储每个队列的连接工厂，确保连接不会重复创建
    private final Map<String, CachingConnectionFactory> connectionFactories;

    @Autowired
    RabbitmqConnectionMapper rabbitMQConnectionMapper;

    @Autowired
    public AgentPublishService() {
        this.connectionFactories = new HashMap<>();
    }

    /**
     * 创建并绑定 Direct 交换机和队列
     */
    public Body<String> createAndBindDirectExchange(String centerId, String exchangeName, String queueName, String routingKey) {
        return createAndBindExchange(centerId, exchangeName, queueName, routingKey, ExchangeType.DIRECT);
    }

    /**
     * 创建并绑定 Topic 交换机和队列
     */
    public Body<String> createAndBindTopicExchange(String centerId, String exchangeName, String queueName, String routingKey) {
        return createAndBindExchange(centerId, exchangeName, queueName, routingKey, ExchangeType.TOPIC);
    }

    /**
     * 创建并绑定 Fanout 交换机和队列
     */
    public Body<String> createAndBindFanoutExchange(String centerId, String exchangeName, String queueName) {
        return createAndBindExchange(centerId, exchangeName, queueName, "", ExchangeType.FANOUT);
    }

    /**
     * 发送消息到 Direct 交换机
     */
    public Body<String> sendMessageToDirectExchange(String centerId, String exchangeName, String routingKey, String message) {
        return sendMessage(centerId, exchangeName, routingKey, message);
    }

    /**
     * 发送消息到 Topic 交换机
     */
    public Body<String> sendMessageToTopicExchange(String centerId, String exchangeName, String routingKey, String message) {
        return sendMessage(centerId, exchangeName, routingKey, message);
    }

    /**
     * 发送消息到 Fanout 交换机
     */
    public Body<String> sendMessageToFanoutExchange(String centerId, String exchangeName, String message) {
        return sendMessage(centerId, exchangeName, "", message);  // Fanout 没有 routingKey
    }

    /**
     * 创建并绑定交换机和队列
     */
    private Body<String> createAndBindExchange(String centerId, String exchangeName, String queueName, String routingKey, ExchangeType exchangeType) {
        // 检查是否已有连接
        CachingConnectionFactory factory = connectionFactories.get(queueName);

        // 如果没有已存在的连接工厂，创建新的
        if (factory == null) {
            RabbitmqConnection connectionInfo = rabbitMQConnectionMapper.selectById(centerId);
            if (connectionInfo == null) {
                return Body.error("No RabbitMQ connection found for centerId: " + centerId);
            }

            try {
                // 创建连接工厂
                factory = new CachingConnectionFactory(connectionInfo.getHost(), connectionInfo.getPort());
                factory.setUsername(connectionInfo.getUsername());
                factory.setPassword(connectionInfo.getPassword());
                factory.setVirtualHost(connectionInfo.getVirtualHost());

                // 将新创建的连接工厂保存到 Map 中
                connectionFactories.put(queueName, factory);

            } catch (Exception e) {
                return Body.error("Failed to create connection factory for center: " + e.getMessage());
            }
        }

        // 使用现有的或新创建的工厂来声明交换机和队列
        try {
            RabbitAdmin rabbitAdmin = new RabbitAdmin(factory);

            // 检查队列是否存在，如果不存在则创建
            if (!checkQueueExists(factory, queueName)) {
                Queue queue = new Queue(queueName, true);  // 持久化队列
                rabbitAdmin.declareQueue(queue);

                // 根据交换机类型创建不同的交换机并绑定队列
                switch (exchangeType) {
                    case DIRECT:
                        DirectExchange directExchange = new DirectExchange(exchangeName);
                        rabbitAdmin.declareExchange(directExchange);
                        Binding directBinding = BindingBuilder.bind(queue).to(directExchange).with(routingKey);
                        rabbitAdmin.declareBinding(directBinding);
                        break;

                    case TOPIC:
                        TopicExchange topicExchange = new TopicExchange(exchangeName);
                        rabbitAdmin.declareExchange(topicExchange);
                        Binding topicBinding = BindingBuilder.bind(queue).to(topicExchange).with(routingKey);
                        rabbitAdmin.declareBinding(topicBinding);
                        break;

                    case FANOUT:
                        FanoutExchange fanoutExchange = new FanoutExchange(exchangeName);
                        rabbitAdmin.declareExchange(fanoutExchange);
                        Binding fanoutBinding = BindingBuilder.bind(queue).to(fanoutExchange);
                        rabbitAdmin.declareBinding(fanoutBinding);
                        break;
                }
            }

            return Body.success("Exchange and queue successfully created and bound.");

        } catch (Exception e) {
            System.err.println("Failed to create and bind exchange: " + e.getMessage());
            return Body.error("Failed to create and bind exchange and queue: " + queueName);
        }
    }

    /**
     * 发送消息
     */
    private Body<String> sendMessage(String centerId, String exchangeName, String routingKey, String message) {
        // 检查是否已有连接工厂
        CachingConnectionFactory factory = connectionFactories.get(exchangeName);

        if (factory == null) {
            RabbitmqConnection connectionInfo = rabbitMQConnectionMapper.selectById(centerId);
            if (connectionInfo == null) {
                return Body.error("No RabbitMQ connection found for centerId: " + centerId);
            }

            try {
                // 创建连接工厂
                factory = new CachingConnectionFactory(connectionInfo.getHost(), connectionInfo.getPort());
                factory.setUsername(connectionInfo.getUsername());
                factory.setPassword(connectionInfo.getPassword());
                factory.setVirtualHost(connectionInfo.getVirtualHost());

                // 保存连接工厂到 Map 中
                connectionFactories.put(exchangeName, factory);
            } catch (Exception e) {
                return Body.error("Failed to create connection factory: " + e.getMessage());
            }
        }

        // 使用连接工厂来发送消息
        try {
            RabbitTemplate rabbitTemplate = new RabbitTemplate(factory);
            rabbitTemplate.convertAndSend(exchangeName, routingKey, message);
            return Body.success("Message successfully sent to exchange: " + exchangeName);
        } catch (Exception e) {
            System.err.println("Failed to send message: " + e.getMessage());
            return Body.error("Failed to send message to exchange: " + exchangeName);
        }
    }

    /**
     * 检查队列是否存在
     */
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
            System.err.println("Queue " + queueName + " does not exist: " + e.getMessage());
            return false;
        }
    }

    /**
     * 销毁指定队列的连接
     */
    public Body<String> destroyConnection(String queueName) {
        CachingConnectionFactory factory = connectionFactories.remove(queueName);
        if (factory != null) {
            factory.destroy();  // 销毁连接，确保在 RabbitMQ Management 中不再显示
            return Body.success("Connection for queue " + queueName + " destroyed.");
        } else {
            return Body.error("No connection found for queue: " + queueName);
        }
    }

    /**
     * 交换机类型枚举
     */
    private enum ExchangeType {
        DIRECT,
        TOPIC,
        FANOUT
    }
}
