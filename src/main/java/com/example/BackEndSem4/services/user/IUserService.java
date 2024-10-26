package com.example.BackEndSem4.services.user;

import com.example.BackEndSem4.dtos.UserDTO;
import com.example.BackEndSem4.models.User;
import com.example.BackEndSem4.response.user.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IUserService {
    User createUser(UserDTO userDTO) throws  Exception;

    User createUserByAdmin(UserDTO userDTO) throws  Exception;


    String login(String phoneNumber, String password, Long roleId) throws Exception;

    User getUserDetailFromToken(String token) throws Exception;

    User updateUser(Long userId, UserDTO userDTO) throws Exception;

     User updateUserAdmin(Long userId, UserDTO userUpdateDTO) throws Exception;

    void  deleteUser(Long userId) throws Exception;

     Page<User> getUsersAll(String keyword, Pageable pageable);

    List<UserResponse> findAllByRoleName(String roleName);
     User getUserById(Long id) throws Exception;

}
