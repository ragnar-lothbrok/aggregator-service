package com.exclusively.aggregator.logback.kafka;

import java.util.Properties;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import kafka.producer.async.MissingConfigException;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

/**
 * Logback appender for logging data to kafka
 */
public class KafkaLogbackAppender extends AppenderBase<ILoggingEvent> {

	private String topic;
	private String brokerList;
	private String compressionCodec;
	private String producerType;
	private String numMessagesBatch;
	private String maxTimeBuffer;
	private String maxMessagesBuffer;
	private String messageBlockingTime;
	private Producer<String, String> producer;

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public String getBrokerList() {
		return brokerList;
	}

	public void setBrokerList(String brokerList) {
		this.brokerList = brokerList;
	}

	public String getCompressionCodec() {
		return compressionCodec;
	}

	public void setCompressionCodec(String compressionCodec) {
		this.compressionCodec = compressionCodec;
	}

	public String getProducerType() {
		return producerType;
	}

	public void setProducerType(String producerType) {
		this.producerType = producerType;
	}

	public String getNumMessagesBatch() {
		return numMessagesBatch;
	}

	public void setNumMessagesBatch(String numMessagesBatch) {
		this.numMessagesBatch = numMessagesBatch;
	}

	public Producer<String, String> getProducer() {
		return producer;
	}

	public void setProducer(Producer<String, String> producer) {
		this.producer = producer;
	}

	public String getMaxTimeBuffer() {
		return maxTimeBuffer;
	}

	public void setMaxTimeBuffer(String maxTimeBuffer) {
		this.maxTimeBuffer = maxTimeBuffer;
	}

	public String getMaxMessagesBuffer() {
		return maxMessagesBuffer;
	}

	public void setMaxMessagesBuffer(String maxMessagesBuffer) {
		this.maxMessagesBuffer = maxMessagesBuffer;
	}

	public String getMessageBlockingTime() {
		return messageBlockingTime;
	}

	public void setMessageBlockingTime(String messageBlockingTime) {
		this.messageBlockingTime = messageBlockingTime;
	}

	@Override
	public void start() {
		if (this.brokerList == null) {
			throw new MissingConfigException("Broker list not configured");
		}
		if (this.compressionCodec == null) {
			this.compressionCodec = "0";
		}
		if (this.producerType == null) {
			this.producerType = "sync";
		}
		if (this.numMessagesBatch == null) {
			this.numMessagesBatch = "200";
		}
		if (this.maxTimeBuffer == null) {
			this.maxTimeBuffer = "5000";
		}
		if (this.maxMessagesBuffer == null) {
			this.maxMessagesBuffer = "15000";
		}
		if (this.messageBlockingTime == null) {
			this.messageBlockingTime = "-1";
		}
		super.start();
		Properties props = new Properties();
		props.put("metadata.broker.list", this.brokerList);
//		props.put("zk.connect","10.11.16.10:2181,10.11.16.11:2181,10.11.16.12:2181");
		props.put("serializer.class", "kafka.serializer.StringEncoder");
		props.put("producer.type", this.producerType);
		props.put("compression.codec", this.compressionCodec);
		props.put("batch.num.messages", this.numMessagesBatch);
		props.put("queue.buffering.max.ms", this.maxTimeBuffer);
		props.put("queue.buffering.max.messages", this.maxMessagesBuffer);
		props.put("queue.enqueue.timeout.ms", this.messageBlockingTime);
		ProducerConfig config = new ProducerConfig(props);
		this.producer = new Producer<String, String>(config);
	}

	@Override
	public void stop() {
		super.stop();
		this.producer.close();
	}

	@Override
	protected void append(ILoggingEvent event) {
		KeyedMessage<String, String> data = new KeyedMessage<String, String>(this.topic, null, (String) event.getFormattedMessage());
		try {
			this.producer.send(data);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
