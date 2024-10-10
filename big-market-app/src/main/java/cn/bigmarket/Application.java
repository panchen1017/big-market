package cn.bigmarket;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@Configurable
@EnableScheduling//通过@EnableScheduling注解开启对计划任务的支持
public class Application {

    public static void main(String[] args){
        SpringApplication.run(Application.class);
    }

}
