package com.own.grpc.consumer.config;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.cloud.nacos.discovery.NacosWatch;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@RequiredArgsConstructor
public class SmartLifecycleImpl implements SmartLifecycle {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${server.port}")
    private int port;

    private final AtomicBoolean runningFlag = new AtomicBoolean(false);

    private final NacosWatch nacosWatch;

    private final NacosServiceManager serviceManager;

    private final NacosDiscoveryProperties discoveryProperties;


    @Override
    public void start() {
        log.info(">>> [SmartLifecycleImpl] start lifecycle logic");
        runningFlag.set(true);
    }

    @Override
    public void stop() {
        log.info("<<< [SmartLifecycleImpl] stop lifecycle logic");
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
            log.error("<<< [SmartLifecycleImpl] stop with error", e);
        }
    }

    @Override
    public boolean isRunning() {
        return runningFlag.get();
    }
}
