package com.own.grpc.consumer.controller;

import com.own.grpc.consumer.stub.GrpcStubHolder;
import com.own.grpc.protos.EmptyRequest;
import com.own.grpc.protos.RemoteRpcServiceGrpc;
import com.own.grpc.protos.RpcEmployees;
import io.grpc.stub.AbstractStub;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/grpc")
public class EmployeeController {

    @Autowired
    private GrpcStubHolder stubHolder;

    @GetMapping("/employee")
    public String getEmployees() {
        Optional<? super AbstractStub<?>> opStub = stubHolder.grabFirstStubByServiceName("grpc-service-provider");
        if (opStub.isPresent()) {
            RemoteRpcServiceGrpc.RemoteRpcServiceBlockingStub stub = (RemoteRpcServiceGrpc.RemoteRpcServiceBlockingStub) opStub.get();
            RpcEmployees employees = stub.getEmployees(EmptyRequest.newBuilder().build());
            return employees.toString();
        }
        return "Currently Service Could not support";
    }
}
