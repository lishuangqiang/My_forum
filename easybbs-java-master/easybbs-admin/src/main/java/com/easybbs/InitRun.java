package com.easybbs;

import com.easybbs.service.SysSettingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class InitRun implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(InitRun.class);

    @Resource
    private SysSettingService sysSettingService;

    @Override
    public void run(ApplicationArguments args) {
        //刷新系统设置缓存
        sysSettingService.refreshCache();

        logger.info("服务启动成功，开始愉快的开发吧");
    }

}
