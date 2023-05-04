package com.own.grpc.provider.service;

import com.own.grpc.protos.EmptyRequest;
import com.own.grpc.protos.RemoteRpcServiceGrpc;
import com.own.grpc.protos.RpcEmployee;
import com.own.grpc.protos.RpcEmployees;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

/**
 * Remote Grpc Service Implement
 *
 * @author Roylic
 * 2023/4/12
 */
@Slf4j
@GrpcService
public class RemoteGRpcServiceImpl extends RemoteRpcServiceGrpc.RemoteRpcServiceImplBase {

    @Override
    public void getEmployees(EmptyRequest request, StreamObserver<RpcEmployees> responseObserver) {
        log.debug("Trigger grpc service with request:{}", request);
        responseObserver.onNext(
                RpcEmployees.newBuilder()
                        .addEmployee(
                                RpcEmployee.newBuilder()
                                        .setId(1)
                                        .setName("Emp1")
                                        .setSalary(4000.55F).build())
                        .addEmployee(
                                RpcEmployee.newBuilder()
                                        .setId(2)
                                        .setName("Emp2")
                                        .setSalary(3403.01F).build())
                        .build());
        responseObserver.onCompleted();
    }
}
