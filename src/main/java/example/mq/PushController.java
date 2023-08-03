package example.mq;

import com.rabbitmq.client.*;
import com.rabbitmq.client.impl.AMQChannel;
import com.rabbitmq.client.impl.AMQCommand;
import com.rabbitmq.client.impl.AMQConnection;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
@RestController
@RequestMapping("push")
public class PushController implements InitializingBean {

    private Channel channel;

    private Connection conn;

    @Override
    public void afterPropertiesSet() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setAutomaticRecoveryEnabled(false);
        factory.useNio();
        factory.setUsername("gogo");
        factory.setPassword("gogo");
        factory.setVirtualHost("/");
        factory.setHost("127.0.0.1");
        factory.setPort(8001);
        conn = factory.newConnection();
        channel = conn.createChannel();
        channel.addShutdownListener(new ShutdownListener() {
            @Override
            public void shutdownCompleted(ShutdownSignalException e) {
                System.out.println("-------channel close------");

            }
        });
        conn.addShutdownListener(new ShutdownListener() {
            @Override
            public void shutdownCompleted(ShutdownSignalException cause) {
                System.out.println("-------connection close-------");
            }
        });
        conn.addBlockedListener( new  BlockedListener() {
            public  void  handleBlocked (String reason)  throws IOException {
                System.out.println("-------handleBlocked" + reason + "-------");
                throw new RuntimeException();
            }

            public  void  handleUnblocked ()  throws IOException {
                System.out.println("-------handleUnblocked-------");
            }
        });
    }

    @GetMapping("click")
    public String pushData() throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            sb.append("abcdefghijklmnopqrstuvwxyz12345678901234567890");
        }
        for (int i = 0; i < 5; i++) {
            channel.basicPublish("TTTEST","tltest",true,null,(sb.toString()).getBytes(StandardCharsets.UTF_8));
        }
        return "success";
    }

    @GetMapping("close")
    public String close() throws IOException {
        conn.close();
        return "success";
    }

    @GetMapping("notify")
    public String notifyMethod() throws Exception {
        AMQConnection connection = (AMQConnection) conn;
        Field channel0 = connection.getClass().getDeclaredField("_channel0");
        channel0.setAccessible(true);
        AMQChannel channel = (AMQChannel) channel0.get(connection);
        channel.handleCompleteInboundCommand(new AMQCommand(new AMQP.Connection.CloseOk.Builder().build()));
        return "success";
    }
}
