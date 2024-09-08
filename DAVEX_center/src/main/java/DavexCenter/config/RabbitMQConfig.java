package DavexCenter.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // 定义交换机
    @Bean
    public TopicExchange applicationExchange() {
        return new TopicExchange("application-exchange");
    }

    // 定义队列
    @Bean
    public Queue applicationQueue() {
        return new Queue("application-queue");
    }

    // 将队列绑定到交换机
    @Bean
    public Binding binding(Queue applicationQueue, TopicExchange applicationExchange) {
        return BindingBuilder.bind(applicationQueue).to(applicationExchange).with("application.#");
    }
}
