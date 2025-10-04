package com.donorservice.client;

import com.userservice.grpc.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class UserGrpcClient {

    @Value("${spring.grpc.client.user-service.address}")
    private String grpcServerAddress;

    private ManagedChannel channel;
    private UserServiceGrpc.UserServiceBlockingStub userStub;

    @PostConstruct
    public void init() {
        String address = grpcServerAddress.replace("static://", "");
        String[] parts = address.split(":");
        String host = parts[0];
        int port = Integer.parseInt(parts[1]);

        this.channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        this.userStub = UserServiceGrpc.newBlockingStub(channel);

        log.info("gRPC channel created to user-service at {}:{}", host, port);
    }

    @PreDestroy
    public void shutdown() {
        if (channel != null && !channel.isShutdown()) {
            channel.shutdown();
            log.info("gRPC channel shut down");
        }
    }

    public UserProfileResponse getUserProfile(UUID userId) {
        try {
            UserProfileRequest request = UserProfileRequest.newBuilder()
                    .setUserId(userId.toString())
                    .build();
            return userStub.getUserProfile(request);
        } catch (Exception e) {
            log.error("Error fetching user profile via gRPC: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch user profile", e);
        }
    }

    public boolean checkFollowStatus(UUID followerId, UUID followingId) {
        try {
            FollowStatusRequest request = FollowStatusRequest.newBuilder()
                    .setFollowerId(followerId.toString())
                    .setFollowingId(followingId.toString())
                    .build();

            FollowStatusResponse response = userStub.checkFollowStatus(request);
            return response.getIsFollowing();
        } catch (Exception e) {
            log.error("Error checking follow status via gRPC: {}", e.getMessage());
            return false;
        }
    }
}
