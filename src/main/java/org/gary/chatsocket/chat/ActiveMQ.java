package org.gary.chatsocket.chat;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ActiveMQ {

    private static ConnectionFactory connectionFactory=new ActiveMQConnectionFactory("tcp://127.0.0.1:61616");

    static void produceToQueue(String queueName,String message) throws Exception{
        Connection connection = connectionFactory.createConnection();
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = session.createQueue(queueName);
        MessageProducer producer = session.createProducer(queue);
        TextMessage textMessage = session.createTextMessage(message);
        producer.send(textMessage);
        producer.close();
        session.close();
        connection.close();
    }


    static ResourceReclaim ConsumerFromQueue(String queueName, MessageListener messageListener) throws Exception{
        Connection connection = connectionFactory.createConnection();
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = session.createQueue(queueName);
        MessageConsumer consumer = session.createConsumer(queue);
        consumer.setMessageListener(messageListener);
        return new ResourceReclaim(consumer,session,connection);
    }

    public static void produceToTopic(String topicName,String message) throws Exception{
        Connection connection = connectionFactory.createConnection();
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Topic topic = session.createTopic(topicName);
        MessageProducer producer = session.createProducer(topic);
        TextMessage textMessage = session.createTextMessage(message);
        producer.send(textMessage);
        producer.close();
        session.close();
        connection.close();
    }

    public static void consumerFromTopic(String topicName,MessageListener messageListener) throws Exception{
        Connection connection = connectionFactory.createConnection();
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Topic topic = session.createTopic(topicName);
        MessageConsumer consumer = session.createConsumer(topic);
        consumer.setMessageListener(messageListener);
        //8、程序等待接收用户消息
        System.in.read();
        consumer.close();
        session.close();
        connection.close();
    }
}

class MyMessageListener implements MessageListener {

    private static ThreadPoolExecutor executor =
            new ThreadPoolExecutor(10, 50, 60, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<Runnable>());

    private Socket client;

    MyMessageListener(Socket client) {
        this.client = client;
    }

    @Override
    public void onMessage(Message message) {
        if(message instanceof TextMessage){
            TextMessage textMessage = (TextMessage)message;
            try {
                executor.execute(new WriteTask(client,textMessage.getText()));
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }
}


class ResourceReclaim {

    private MessageConsumer consumer;
    private Session session;
    private Connection connection;

    ResourceReclaim(MessageConsumer consumer, Session session, Connection connection) {
        this.consumer = consumer;
        this.session = session;
        this.connection = connection;
    }

    public void close() throws Exception{
        consumer.close();
        session.close();
        connection.close();
    }
}