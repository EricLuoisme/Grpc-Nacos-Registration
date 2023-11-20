package com.own.grpc.consumer.stub.listener;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.client.naming.event.InstancesChangeEvent;
import com.alibaba.nacos.common.notify.Event;
import com.alibaba.nacos.common.notify.listener.Subscriber;
import com.own.grpc.consumer.config.GrpcCaredConfig;
import com.own.grpc.protos.RemoteRpcServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.AbstractStub;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class AllInstanceEventListener extends Subscriber<InstancesChangeEvent> {


    // <serviceName, <ip, stub>>
    private final Map<String, Map<String, ? super AbstractStub<?>>> grpcStubIpMap;

    // <serviceName>
    private final Set<String> caredServiceSet;


    public AllInstanceEventListener(Map<String, Map<String, ? super AbstractStub<?>>> grpcStubIpMap,
                                    GrpcCaredConfig config) {
        this.grpcStubIpMap = grpcStubIpMap;
        this.caredServiceSet = new HashSet<>(config.getGrpcServerNames());
    }

    @Override
    public Class<? extends Event> subscribeType() {
        return InstancesChangeEvent.class;
    }

    @Override
    public void onEvent(InstancesChangeEvent instancesChangeEvent) {
        log.info("Received instance change event with name: {}", instancesChangeEvent.getServiceName());
        if (caredServiceSet.contains(instancesChangeEvent.getServiceName())) {
            this.updateGrpcStubs(instancesChangeEvent.getServiceName(), instancesChangeEvent.getHosts(), false);
        }
    }

    public void updateGrpcStubs(String serviceName, List<Instance> serviceHosts, boolean isInit) {

        if (serviceHosts.isEmpty() && isInit) {
            log.debug("<<< skip empty stub construction on starts");
            // still need to add an empty map
            grpcStubIpMap.put(serviceName, new HashMap<>());
            return;
        }

        // get difference set
        log.debug(">>> try update stub for service: {}", serviceName);
        Set<String> newInstanceIps = serviceHosts.parallelStream()
                .filter(Instance::isHealthy)
                .map(Instance::getIp)
                .collect(Collectors.toSet());
        // clear for empty
        if (newInstanceIps.isEmpty()) {
            log.debug("<<< no more available stubs for service: {}", serviceName);
            grpcStubIpMap.getOrDefault(serviceName, new HashMap<>()).clear();
            return;
        }
        // remove non-active services
        Map<String, ? super AbstractStub<?>> serviceIpStubMap = grpcStubIpMap.getOrDefault(serviceName, new HashMap<>());
        Set<String> oldInstanceIps = serviceIpStubMap.keySet();
        oldInstanceIps.removeAll(newInstanceIps);
        oldInstanceIps.parallelStream().forEach(serviceIpStubMap::remove);

        // add new active services
        serviceHosts.parallelStream()
                .filter(instance -> !serviceIpStubMap.containsKey(instance.getIp()))
                .forEach(instance -> {
                    // construct the grpc stub TODO still need to know what grpc service we really calling
                    String ip = instance.getIp();
                    int port = Integer.parseInt(instance.getMetadata().get("gRPC_port"));
                    ManagedChannel channel = ManagedChannelBuilder.forAddress(ip, port).usePlaintext().build();
                    serviceIpStubMap.put(ip, RemoteRpcServiceGrpc.newBlockingStub(channel));
                });

        // add back into map
        grpcStubIpMap.put(serviceName, serviceIpStubMap);
        log.debug("<<< update with {} stubs for service: {}", serviceIpStubMap.size(), serviceName);
    }

}
