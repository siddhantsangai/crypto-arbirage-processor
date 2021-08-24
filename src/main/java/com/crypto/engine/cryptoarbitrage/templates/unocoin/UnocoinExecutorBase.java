package com.crypto.engine.cryptoarbitrage.templates.unocoin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class UnocoinExecutorBase {
    private static final Logger log = LoggerFactory.getLogger(UnocoinExecutorBase.class);

    @Autowired
    private TaskExecutor taskExecutor;
    @Autowired
    private ApplicationContext applicationContext;


    @PostConstruct
    public void postStartup() {
        UnocoinDataExtractor unocoinDataExtractor = applicationContext.getBean(UnocoinDataExtractor.class);
        taskExecutor.execute(unocoinDataExtractor);
    }
}

