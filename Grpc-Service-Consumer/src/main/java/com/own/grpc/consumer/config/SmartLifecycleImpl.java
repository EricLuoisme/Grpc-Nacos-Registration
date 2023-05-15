package com.own.grpc.consumer.config;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.cloud.nacos.discovery.NacosWatch;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
public class SmartLifecycleImpl implements SmartLifecycle {

    private final AtomicBoolean runningFlag = new AtomicBoolean(false);

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${server.port")
    private int port;


    @Autowired
    private NacosWatch nacosWatch;

    @Autowired
    private NacosServiceManager serviceManager;

    @Autowired
    private NacosDiscoveryProperties discoveryProperties;


    @Override
    public void start() {
        log.info(">>> [CustomSmartLifecycle] start embed custom lifecycle logic");
        runningFlag.set(true);
    }

    @Override
    public void stop() {
        log.info("<<< [CustomSmartLifecycle] stop embed custom lifecycle logic");
        runningFlag.set(false);
        nacosWatch.stop(); // stop Nacos' watching thread
        try {
            Instance instance = new Instance();
            instance.setIp(Inet4Address.getLocalHost().getHostAddress());
            instance.setPort(port);
            instance.setServiceName(applicationName);
            NamingService namingService = serviceManager.getNamingService(discoveryProperties.getNacosProperties());
            namingService.deregisterInstance(applicationName, instance);
            namingService.shutDown();
        } catch (UnknownHostException | NacosException e) {
            log.error("<<< [CustomSmartLifecycle] stop with error", e);
        }
    }

    @Override
    public boolean isRunning() {
        return runningFlag.get();
    }
}
