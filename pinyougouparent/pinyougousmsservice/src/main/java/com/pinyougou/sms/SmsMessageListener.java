package com.pinyougou.sms;

import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;

/**
 * Created by 37269 on 2018/8/9.
 */
public class SmsMessageListener implements MessageListener {

    @Autowired
    private SmsUtil smsUtil;

    @Value("${templateCode_smscode}")
    private String templateCode_smscode;//短信模板编号

    @Value("${templateParam_smscode}")
    private String templateParam_smscode;//短信参数

    @Override
    public void onMessage(Message message) {
        MapMessage mapMessage=(MapMessage)message;
        try {
            String mobile= (String)mapMessage.getString("mobile");
            String smscode= (String)mapMessage.getString("smscode");
            System.out.println("从MQ中提取出消息--- 手机号："+mobile+"  验证码："+smscode);

            String param=  templateParam_smscode.replace("[value]",smscode);
            SendSmsResponse smsResponse = smsUtil.sendSms(mobile, templateCode_smscode, param);
            System.out.println(smsResponse.getCode()+":"+ smsResponse.getMessage());

        } catch (JMSException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
        }
    }
}
