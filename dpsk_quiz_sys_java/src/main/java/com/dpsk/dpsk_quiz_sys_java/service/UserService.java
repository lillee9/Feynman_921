package com.dpsk.dpsk_quiz_sys_java.service;

import com.dpsk.dpsk_quiz_sys_java.exception.CustomException;
import com.dpsk.dpsk_quiz_sys_java.pojo.entity.User;
import com.dpsk.dpsk_quiz_sys_java.pojo.dto.UserDto;
import com.dpsk.dpsk_quiz_sys_java.repository.UserRepository;
import com.dpsk.dpsk_quiz_sys_java.service.interfaces.IUserService;
import com.dpsk.dpsk_quiz_sys_java.utils.JwtTokenUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService implements IUserService{
    @Value("${jwt.secret}")
    private String jwtSecret;

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User add(UserDto userDto) {
        if (userDto.getUsername() == null || userDto.getUsername().trim().isEmpty()) {
            throw new RuntimeException("用户名不能为空");
        }
        // 检查用户名是否已存在
        Optional<User> existingUser = userRepository.findByUsername(userDto.getUsername());
        if (existingUser.isPresent()) {
            throw new RuntimeException("用户名已存在，请更换");
        }

        User user = new User();
        if (userDto.getPassword() == null) {
            userDto.setPassword("123456");
        }

        BeanUtils.copyProperties(userDto, user);
        return userRepository.save(user);
    }


    @Override
    public User getUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(()->{
            throw new IllegalArgumentException("参数错误，用户不存在");
        });
    }

    @Override
    public User edit(UserDto userDto) {
        User user = new User();
        BeanUtils.copyProperties(userDto,user);
        return userRepository.save(user);
    }

    @Override
    public void delete(Long userId) {
        userRepository.deleteById(userId);
    }



        @Override
        public User login(UserDto userDto) {
            if (userDto.getUsername() == null || userDto.getUsername().trim().isEmpty()) {
                throw new CustomException("用户名不能为空");
            }

            if (userDto.getPassword() == null || userDto.getPassword().trim().isEmpty()) {
                throw new CustomException("密码不能为空");
            }

            // 查询数据库中是否存在该用户名和密码
            Optional<User> userOpt = userRepository.findByUsernameAndPassword(
                    userDto.getUsername(),
                    userDto.getPassword()
            );

            if (userOpt.isPresent()) {
                User u = userOpt.get();
                String token = JwtTokenUtils.genToken(u.getUser_id(),jwtSecret);
                u.setToken(token);
                return u;
            } else {
                throw new CustomException("用户名或密码错误");
            }
        }

    @Override
    public String getUsernameById(Long userId) {
        // 根据用户ID查询用户信息
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("用户不存在，ID: " + userId));
        // 返回用户名
        return user.getUsername();
    }

    public Optional<User> findById(Long id){
        return userRepository.findById(id);
    }

}
