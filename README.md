## gRPC + Nacos
- In Nacos, microservices typically communicate over HTTP. However, with the introduction of Nacos 2.0, the platform has adopted gRPC for heartbeat signals
- Consider transitioning your services to gRPC to benefit from reduced latency, particularly in the serialization and deserialization processes

### Service Producer
- Extend or implement your RPC service in the ProtoBuf file
- Register the service name, IP address, and gRPC port with Nacos

### Service Consumer
- Subscribe to Nacos' InstanceChangedEvent to filter for instance information of the gRPC service providers
- Maintain a map of <ServiceName, CycleList<ManagedChannel>>, updating it in response to each InstanceChangedEvent received
- Business code could easily obtain a usable ManagedChannel, create a stub, and initiate gRPC requests
