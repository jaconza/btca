package za.co.jacon.btc.aggregator.config;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Set up the AMQP components in the DiC.
 */
@Configuration
public class AMQPConfig {

    /**
     * Sets up the rabbitmq connection.
     *
     * @param environment the spring environment.
     *
     * @return the connection factory
     */
    @Bean
    public ConnectionFactory connectionFactory(Environment environment) {
        String host = environment.getRequiredProperty("amqp.host");
        String user = environment.getRequiredProperty("amqp.user");
        String pass = environment.getRequiredProperty("amqp.password");
        String vhost = environment.getRequiredProperty("amqp.transactions.vhost");

        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(host);
        connectionFactory.setUsername(user);
        connectionFactory.setPassword(pass);
        connectionFactory.setVirtualHost(vhost);

        return connectionFactory;
    }

    /**
     * Configures the RabbitTemplate.
     *
     * Sets the message converters and connection factory.
     *
     * @param environment the spring environment
     * @param connectionFactory the connection factory
     * @param messageConverter the message converters
     * @return the configured rabbit template
     */
    @Bean
    public RabbitTemplate rabbitTemplate(Environment environment, ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        template.setExchange(environment.getRequiredProperty("amqp.transactions.exchange"));

        return template;
    }

    /**
     * Setup a default "jackson to json" message converter.
     * @return the message converter.
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * Creates an AMQPAdmin object.
     *
     * Auto declares the necessary exchange.
     *
     * @param connectionFactory the connection factory
     *
     * @return the amqp admin
     */
    @Bean
    public AmqpAdmin amqpAdmin(ConnectionFactory connectionFactory, List<Exchange> exchanges) {
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);

        for (Exchange exchange:exchanges) {
            rabbitAdmin.declareExchange(exchange);
        }
        return rabbitAdmin ;
    }

    /**
     * Create the exchange for the btca aggregator.
     *
     * @param environment the spring environment
     *
     * @return the configured exchange
     */
    @Bean
    public Exchange transactionsExchange(final Environment environment) {
        String exchangeName = environment.getRequiredProperty("amqp.transactions.exchange");
        Boolean durable = true;
        Boolean autoDelete = true;

        return new TopicExchange(exchangeName, durable, autoDelete);
    }

}
