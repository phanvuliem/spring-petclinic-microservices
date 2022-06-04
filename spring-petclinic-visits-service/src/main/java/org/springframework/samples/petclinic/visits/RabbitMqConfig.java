package org.springframework.samples.petclinic.visits;

import org.slf4j.MDC;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author vlp
 */
@Configuration
@RequiredArgsConstructor
public class RabbitMqConfig implements InitializingBean {
    public static final String TOPIC_EXCHANGE_NAME = "pet-clinic-exchange";
    public static final String QUEUE_NAME = "visit-info";
    public static final String ROUTING_KEY_PREFIX = QUEUE_NAME + ".";
    public static final String PET_ID_CTX = "PET_ID";

    private final RabbitTemplate rabbitTemplate;

    @Bean
    public Queue queue() {
        return new Queue(QUEUE_NAME, false);
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(TOPIC_EXCHANGE_NAME);
    }

    @Bean
    public Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY_PREFIX + "#");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        rabbitTemplate.addBeforePublishPostProcessors(m -> {
            m.getMessageProperties().getHeaders().put("traceId", RabbitMqConfig.ROUTING_KEY_PREFIX + MDC.get(PET_ID_CTX));
            return m;
        });
    }
}
