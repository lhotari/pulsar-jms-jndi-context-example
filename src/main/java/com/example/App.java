package com.example;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import javax.jms.ConnectionFactory;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSProducer;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.naming.Context;
import javax.naming.InitialContext;

/**
 * Hello world for Pulsar JMS using InitialContextFactory.
 *
 * Start Pulsar with before running this example:
 * docker run --rm -it -p 8080:8080 -p 6650:6650 apachepulsar/pulsar:latest /pulsar/bin/pulsar standalone -nss -nfw
 */
public class App {
    public static void main(String[] args) throws Exception {
        String topic = "persistent://public/default/example-topic";

        Properties properties = new Properties();
        properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, "com.datastax.oss.pulsar.jms.jndi.PulsarInitialContextFactory");
        properties.setProperty(Context.PROVIDER_URL, "pulsar://localhost:6650");
        properties.setProperty("webServiceUrl", "http://localhost:8080");
        properties.setProperty("autoCloseConnectionFactory", "true");
        properties.setProperty("jms.systemNamespace", "public/default");
        Context jndiContext = new InitialContext(properties);

        ConnectionFactory factory = (ConnectionFactory) jndiContext.lookup("ConnectionFactory");

        try (JMSContext context = factory.createContext()) {
            Queue queue = context.createQueue(topic);

            int numberOfMessages = 10;

            CountDownLatch latch = new CountDownLatch(numberOfMessages);

            // Listen for messages...
            JMSConsumer consumer = context.createConsumer(queue);
            consumer.setMessageListener(new MessageListener() {
                @Override
                public void onMessage(Message message) {
                    try {
                        System.out.println("Received: " + message.getBody(String.class));
                    } catch (Exception err) {
                        err.printStackTrace();
                    } finally {
                        latch.countDown();
                    }
                }
            });

            JMSProducer producer = context.createProducer();
            for (int i = 0; i < numberOfMessages; i++) {
                String message = "Hello world! " + i;
                System.out.println("Sending: " + message);
                producer.send(queue, message);
            }

            // wait for messages to be received
            latch.await();

            System.out.println("All messages received.");
        } finally {
            jndiContext.close();
        }
    }
}

