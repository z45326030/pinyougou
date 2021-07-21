package cn.itcast.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;



@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:applicationContext-jms-producer.xml")
public class TestJms {

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private javax.jms.Destination queueTextDestination;

    @Test
    public void sendQueueText(){
        jmsTemplate.convertAndSend(queueTextDestination,"This is a p2p message.");
    }

    @Autowired
    private javax.jms.Destination topicTextDestination;

    @Test
    public void sendTopicText(){
        jmsTemplate.convertAndSend(topicTextDestination,"这是一个发布订阅消息");
    }

}
