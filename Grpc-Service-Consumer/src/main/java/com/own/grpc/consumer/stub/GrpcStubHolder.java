package com.own.grpc.consumer.stub;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.common.notify.NotifyCenter;
import com.own.grpc.consumer.config.GrpcCaredConfig;
import com.own.grpc.consumer.stub.listener.AllInstanceEventListener;
import io.grpc.stub.AbstractStub;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GrpcStubHolder {


    private Map<String, Map<String, ? super AbstractStub<?>>> grpcStubIpMap;

    public GrpcStubHolder(GrpcCaredConfig config, NacosServiceManager serviceManager, NacosDiscoveryProperties discoveryProperties) {
        // init
        grpcStubIpMap = new ConcurrentHashMap<>();
        NamingService namingService = serviceManager.getNamingService(discoveryProperties.getNacosProperties());
        AllInstanceEventListener eventListener = new AllInstanceEventListener(grpcStubIpMap, config);
        config.getGrpcServerNames().forEach(serviceName -> {
            try {
                eventListener.updateGrpcStubs(serviceName, namingService.getAllInstances(serviceName), true);
            } catch (NacosException e) {
                e.printStackTrace();
            }
        });
        // register
        NotifyCenter.registerSubscriber(eventListener);
    }

    /**
     * Grab a stub with service name
     */
    public <T> Optional<? super AbstractStub<?>> grabFirstStubByServiceName(String serviceName) {
        Map<String, ? super AbstractStub<?>> serviceStubMap = grpcStubIpMap.get(serviceName);
        if (null != serviceStubMap) {
            for (Map.Entry<String, ? super AbstractStub<?>> entry : serviceStubMap.entrySet()) {
                return Optional.ofNullable(entry.getValue());
            }
        }
        return Optional.empty();
    }

}
