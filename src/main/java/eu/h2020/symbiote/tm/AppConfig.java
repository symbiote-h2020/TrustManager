package eu.h2020.symbiote.tm;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.client.RestTemplate;

/**
 * Created by mateuszl on 30.09.2016.
 *
 * Note: to be used by components with MongoDB
 */

import com.mongodb.Mongo;
import com.mongodb.MongoClient;

import eu.h2020.symbiote.util.RabbitConstants;

/**
 * @author RuggenthalerC
 * 
 *         Component config beans.
 */
@Configuration
@EnableScheduling
@EnableMongoRepositories
class AppConfig extends AbstractMongoConfiguration {

	@Override
	protected String getDatabaseName() {
		return "symbiote-cloud-tm-database";
	}

	@Bean
	public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
		ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
		threadPoolTaskScheduler.setPoolSize(3);
		return threadPoolTaskScheduler;
	}

	@Bean
	public MessageConverter jsonMessageConverter() {
		return new Jackson2JsonMessageConverter();
	}

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
		return restTemplateBuilder.setConnectTimeout(1 * 1000).setReadTimeout(10 * 1000).build();
	}

	@Bean
	public AmqpTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
		final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
		rabbitTemplate.setMessageConverter(jsonMessageConverter());
		return rabbitTemplate;
	}

	@Bean
	public Queue federationHistoryQueue(@Value("${rabbit.queue.federation.get_federation_history}") String queue) {
		return new Queue(queue);
	}

	@Bean
	public TopicExchange trustTopic(@Value("${" + RabbitConstants.EXCHANGE_TRUST_NAME_PROPERTY + "}") String exchange,
			@Value("${" + RabbitConstants.EXCHANGE_TRUST_DURABLE_PROPERTY + "}") Boolean durable,
			@Value("${" + RabbitConstants.EXCHANGE_TRUST_AUTODELETE_PROPERTY + "}") Boolean autoDelete) {
		return new TopicExchange(exchange, durable, autoDelete);
	}

	@Override
	public Mongo mongo() throws Exception {
		return new MongoClient();
	}
}