package com.yc.utils;

import com.aliyun.auth.credentials.Credential;
import com.aliyun.auth.credentials.provider.StaticCredentialProvider;
import com.aliyun.sdk.service.dysmsapi20170525.AsyncClient;
import com.aliyun.sdk.service.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.sdk.service.dysmsapi20170525.models.SendSmsResponse;
import com.google.gson.Gson;
import darabonba.core.client.ClientOverrideConfiguration;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.CompletableFuture;



@Slf4j
@Component
public class SendSms {
    @Autowired
    private AliOSSProperties aliOSSProperties;

    @Autowired
    private RedisTemplate redisTemplate;
    public void send(String mobile) throws Exception {
        String verificationCode = RandomCodeGenerator();


        // HttpClient Configuration
        /*HttpClient httpClient = new ApacheAsyncHttpClientBuilder()
                .connectionTimeout(Duration.ofSeconds(10)) // Set the connection timeout time, the default is 10 seconds
                .responseTimeout(Duration.ofSeconds(10)) // Set the response timeout time, the default is 20 seconds
                .maxConnections(128) // Set the connection pool size
                .maxIdleTimeOut(Duration.ofSeconds(50)) // Set the connection pool timeout, the default is 30 seconds
                // Configure the proxy
                .proxy(new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("<your-proxy-hostname>", 9001))
                        .setCredentials("<your-proxy-username>", "<your-proxy-password>"))
                // If it is an https connection, you need to configure the certificate, or ignore the certificate(.ignoreSSL(true))
                .x509TrustManagers(new X509TrustManager[]{})
                .keyManagers(new KeyManager[]{})
                .ignoreSSL(false)
                .build();*/

        // Configure Credentials authentication information, including ak, secret, token
        StaticCredentialProvider provider = StaticCredentialProvider.create(Credential.builder()
                // Please ensure that the environment variables ALIBABA_CLOUD_ACCESS_KEY_ID and ALIBABA_CLOUD_ACCESS_KEY_SECRET are set.
                .accessKeyId(aliOSSProperties.getAccessKeyId())
                .accessKeySecret(aliOSSProperties.getAccessKeySecret())
                //.securityToken(System.getenv("ALIBABA_CLOUD_SECURITY_TOKEN")) // use STS token
                .build());

        // Configure the Client
        AsyncClient client = AsyncClient.builder()
                //.httpClient(httpClient) // Use the configured HttpClient, otherwise use the default HttpClient (Apache HttpClient)
                .credentialsProvider(provider)
                //.serviceConfiguration(Configuration.create()) // Service-level configuration
                // Client-level configuration rewrite, can set Endpoint, Http request parameters, etc.
                .overrideConfiguration(
                        ClientOverrideConfiguration.create()
                                // Endpoint 请参考 https://api.aliyun.com/product/Dysmsapi
                                .setEndpointOverride("dysmsapi.aliyuncs.com")
                        //.setConnectTimeout(Duration.ofSeconds(30))
                )
                .build();

        // Parameter settings for API request
        SendSmsRequest sendSmsRequest = SendSmsRequest.builder()
                .phoneNumbers(mobile)
                .signName("阿里云短信测试")    //signName和templateCode必须要匹配，也就是工作台/短信服务/快速学习和测试/2. 调用测试签名模板API
                .templateCode("SMS_154950909")
                .templateParam("{\"code\":\""+verificationCode+"\"}")//与templateCode要匹配
                // Request-level configuration rewrite, can set Http request parameters, etc.
                // .requestConfiguration(RequestConfiguration.create().setHttpHeaders(new HttpHeaders()))
                .build();

        //短信验证码存入redis
        //按照  手机号    ---   验证码的形式
        //redisTemplate.delete(mobile);
        redisTemplate.opsForValue().set(mobile,verificationCode);

        // Asynchronously get the return value of the API request
        CompletableFuture<SendSmsResponse> response = client.sendSms(sendSmsRequest);
        // Synchronously get the return value of the API request
        SendSmsResponse resp = response.get();
        System.out.println(new Gson().toJson(resp));
        // Asynchronous processing of return values
        /*response.thenAccept(resp -> {
            System.out.println(new Gson().toJson(resp));
        }).exceptionally(throwable -> { // Handling exceptions
            System.out.println(throwable.getMessage());
            return null;
        });*/

        // Finally, close the client
        client.close();
    }

    public String RandomCodeGenerator() {
        // 创建一个 Random 对象
        Random random = new Random();
        // 生成六位数的随机验证码
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            int digit = random.nextInt(10); // 生成 0 到 9 之间的随机数
            sb.append(digit);
        }
        // 输出随机验证码
        String verificationCode = sb.toString();
        System.out.println(verificationCode);
        return verificationCode;
    }
}

