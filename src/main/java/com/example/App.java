package com.example;

import java.util.Map;
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


        // Configure JNDI properties
        Properties properties = new Properties();
        properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, "com.datastax.oss.pulsar.jms.jndi.PulsarInitialContextFactory");
        properties.setProperty("autoCloseConnectionFactory", "true");
        properties.setProperty("jms.systemNamespace", "public/default");

        // Set the service URL, webservice URL and authentication token so that they can be overridden by environment variables
        Map<String, String> env = System.getenv();
        properties.setProperty(Context.PROVIDER_URL, env.getOrDefault("PULSAR_SERVICE_URL", "pulsar://localhost:6650"));
        properties.setProperty("webServiceUrl", env.getOrDefault("PULSAR_WEBSERVICE_URL", "http://localhost:8080"));
        String authToken = env.get("PULSAR_AUTH_TOKEN");
        if (authToken != null) {
            // since pulsar-jms-all is used, the AuthenticationToken class is in the shaded package
            properties.setProperty("authPlugin", "com.datastax.oss.pulsar.jms.shaded.org.apache.pulsar.client.impl.auth.AuthenticationToken");
            properties.setProperty("authParams", authToken);
        }

        // Create the JNDI context
        Context jndiContext = new InitialContext(properties);

        // Lookup the JMS connection factory
        ConnectionFactory factory = (ConnectionFactory) jndiContext.lookup("ConnectionFactory");

        // Create a JMS context
        try (JMSContext context = factory.createContext()) {
            Queue queue = context.createQueue(topic);

            int numberOfMessages = 10;
            CountDownLatch latch = new CountDownLatch(numberOfMessages);

            // Receive messages with consumer and message listener
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

            // Send messages
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

