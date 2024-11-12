package org.kob.matchingsystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig { // RestTemplate是两个进程间发送请求进行通信的工具,
    @Bean
    public RestTemplate getRestTemplate(){ // 当我们需要用到这个的时候，可以在配置类中加上configuration和bean，返回这个实例，在需要用到的类中Autowired即可
        return new RestTemplate();
    }
}
