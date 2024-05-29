package com.easybbs;

import com.easybbs.entity.config.AppConfig;
import com.easybbs.entity.constants.Constants;
import com.easybbs.spring.ApplicationContextProvider;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.servlet.MultipartConfigElement;


@EnableAsync
@SpringBootApplication(scanBasePackages = {"com.easybbs"})
@MapperScan(basePackages = {"com.easybbs.mappers"})
@EnableTransactionManagement
@EnableScheduling
public class EasybbsWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(EasybbsWebApplication.class, args);
    }

    @Bean
    @DependsOn({"applicationContextProvider"})
    MultipartConfigElement multipartConfigElement() {
        AppConfig appConfig = (AppConfig) ApplicationContextProvider.getBean("appConfig");
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setLocation(appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + Constants.FILE_FOLDER_TEMP);
        return factory.createMultipartConfig();
    }
}
