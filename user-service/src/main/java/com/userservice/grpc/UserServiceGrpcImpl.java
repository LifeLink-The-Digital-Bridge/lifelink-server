package com.userservice.grpc;

import com.userservice.dto.ChangePasswordRequest;
import com.userservice.dto.UserDTOPassword;
import com.userservice.exception.UserNotFoundException;
import com.userservice.service.UserService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.springframework.grpc.server.service.GrpcService;

import java.util.UUID;

@GrpcService
public class UserServiceGrpcImpl extends UserServiceGrpc.UserServiceImplBase {

    public UserServiceGrpcImpl(UserService userService) {
        System.out.println("UserServiceGrpcImpl bean CREATED!");
        this.userService = userService;
    }

    private final UserService userService;


    @Override
    public void getUserByEmail(GetUserByEmailRequest request, StreamObserver<UserResponse> responseObserver) {
        try {
            UserDTOPassword user = userService.getUserByEmail(request.getEmail());
            UserResponse response = mapToGrpcUser(user);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (UserNotFoundException ex) {
            responseObserver.onError(Status.NOT_FOUND.withDescription(ex.getMessage()).asRuntimeException());
        } catch (Exception ex) {
            responseObserver.onError(Status.INTERNAL.withDescription("Internal server error: " + ex.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void getUserByUsername(GetUserByUsernameRequest request, StreamObserver<UserResponse> responseObserver) {
        try {
            UserDTOPassword user = userService.getUserByUsername(request.getUsername());
            UserResponse response = mapToGrpcUser(user);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (UserNotFoundException ex) {
            responseObserver.onError(Status.NOT_FOUND.withDescription(ex.getMessage()).asRuntimeException());
        } catch (Exception ex) {
            responseObserver.onError(Status.INTERNAL.withDescription("Internal server error: " + ex.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void getUserById(GetUserByIdRequest request, StreamObserver<UserResponse> responseObserver) {
        try {
            UserDTOPassword user = userService.getUserById(UUID.fromString(request.getId()));
            UserResponse response = mapToGrpcUser(user);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (UserNotFoundException ex) {
            responseObserver.onError(Status.NOT_FOUND.withDescription(ex.getMessage()).asRuntimeException());
        } catch (Exception ex) {
            responseObserver.onError(Status.INTERNAL.withDescription("Internal server error: " + ex.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void updatePassword(UpdatePasswordRequest request, StreamObserver<UpdatePasswordResponse> responseObserver) {
        try {
            boolean result = userService.updatePassword(
                    new ChangePasswordRequest(
                            request.getEmail(),
                            request.getNewPassword(),
                            request.getRepeatPassword()
                    )
            );
            UpdatePasswordResponse response = UpdatePasswordResponse.newBuilder()
                    .setSuccess(result)
                    .setMessage(result ? "Password updated successfully" : "Password mismatch or user not found")
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (UserNotFoundException ex) {
            responseObserver.onError(Status.NOT_FOUND.withDescription(ex.getMessage()).asRuntimeException());
        } catch (Exception ex) {
            responseObserver.onError(Status.INTERNAL.withDescription("Internal server error: " + ex.getMessage()).asRuntimeException());
        }
    }

    private UserResponse mapToGrpcUser(UserDTOPassword user) {
        return UserResponse.newBuilder()
                .setId(user.getId().toString())
                .setUsername(user.getUsername())
                .setEmail(user.getEmail())
                .setPassword(user.getPassword())
                .addAllRoles(user.getRoles())
                .build();
    }
}
