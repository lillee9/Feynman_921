package com.dpsk.dpsk_quiz_sys_java.service.interfaces;

import com.dpsk.dpsk_quiz_sys_java.pojo.entity.User;
import com.dpsk.dpsk_quiz_sys_java.pojo.dto.UserDto;

public interface IUserService {
    User add(UserDto userDto);

    User getUser(Long userId);

    User edit(UserDto userDto);

    void delete(Long userId);

    User login(UserDto userDto);


    String getUsernameById(Long userId);
}