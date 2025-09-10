package com.ecommerce.mappers;

import com.ecommerce.DTOs.CreateUserDTO;
import com.ecommerce.DTOs.LoginRequestDTO;
import com.ecommerce.models.User;


public class AuthMapper {

    private AuthMapper (){
        throw new UnsupportedOperationException("This class should never be instantiated");
    }

    public static User fromDTO(final CreateUserDTO createUserDTO){
        return User.builder()
                .email(createUserDTO.email())
                .firstName(createUserDTO.firstname())
                .lastName(createUserDTO.lastname())
                .phone(createUserDTO.phone())
                .password(createUserDTO.password())
                .build();
    }
    public static LoginRequestDTO fromDto(final CreateUserDTO dto){
        return LoginRequestDTO.builder()
                .password(dto.password())
                .email(dto.email())
                .build();
    }



}
