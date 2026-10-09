package edu.br.resource.resourcesystem.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration(proxyBeanMethods = false)
@EnableScheduling
public class MessagingConfiguration {
    public static final String EXCHANGE = "resource.match.events";
    public static final String QUEUE = "resource.match.notifications";
    public static final String ROUTING_KEY = "match.changed";
    @Bean
    Declarables matchMessaging() {
        var exchange = new DirectExchange(EXCHANGE,true,false);
        var deadExchange = new DirectExchange(EXCHANGE+".dead",true,false);
        var queue = QueueBuilder.durable(QUEUE).deadLetterExchange(deadExchange.getName()).deadLetterRoutingKey("failed").build();
        var deadQueue = QueueBuilder.durable(QUEUE+".dead").build();
        return new Declarables(exchange,deadExchange,queue,deadQueue,
                BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY),BindingBuilder.bind(deadQueue).to(deadExchange).with("failed"));
    }
}
