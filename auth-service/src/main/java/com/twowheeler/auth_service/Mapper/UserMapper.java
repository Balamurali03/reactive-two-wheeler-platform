package com.twowheeler.auth_service.Mapper;

import org.springframework.stereotype.Component;

import com.twowheeler.auth_service.Dto.Response.UserResponse;
import com.twowheeler.auth_service.Model.User;

@Component
public class UserMapper {

    public static UserResponse response(User user) {
        return new UserResponse(
            user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhoneNum(),
                user.getRole(),
                user.getStatus(),
                user.getCreatedAt()
        );
    }

}
