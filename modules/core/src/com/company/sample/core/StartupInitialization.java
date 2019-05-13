package com.company.sample.core;

import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.core.sys.events.AppContextStartedEvent;
import com.haulmont.cuba.security.app.Authenticated;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component(StartupInitialization.NAME)
public class StartupInitialization {

    public static final String NAME = "sample_StartupInitialization";

    @Inject
    protected ProcModelDeployer procModelDeployer;

    @EventListener(AppContextStartedEvent.class)
    @Authenticated
    public void deployProcessModels() {
        if ("true".equals(AppContext.getProperty("sample.deployProcessModelsOnStartup"))) {
            procModelDeployer.deployAllModels();
        }
    }
}