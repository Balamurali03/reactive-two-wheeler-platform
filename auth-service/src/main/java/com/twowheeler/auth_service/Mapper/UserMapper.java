package com.twowheeler.auth_service.Mapper;

import org.springframework.stereotype.Component;

import com.twowheeler.auth_service.Dto.Response.UserResponse;
import com.twowheeler.auth_service.Model.UserCredentials;

@Component
public class UserMapper {

    public static UserResponse response(UserCredentials user) {
        return new UserResponse(
            user.getUserId(),
                user.getUsername(),
                user.getRole(),
                user.getCreatedAt()
        );
    }

}
