package com.dpsk.dpsk_quiz_sys_java.controller;

import com.dpsk.dpsk_quiz_sys_java.pojo.ResponseMessage;
import com.dpsk.dpsk_quiz_sys_java.pojo.entity.User;
import com.dpsk.dpsk_quiz_sys_java.pojo.dto.UserDto;
import com.dpsk.dpsk_quiz_sys_java.service.interfaces.IUserService;
import com.dpsk.dpsk_quiz_sys_java.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    IUserService userService;

    @GetMapping("/hello")
    public String hello(){
        return "hello";
    }

    @PostMapping
    public ResponseMessage add(@RequestBody UserDto userDto){
        User userRes = userService.add(userDto);
        return new ResponseMessage<>(200, "success", userRes);
    }

    @GetMapping("/{user_id}")
    public  ResponseMessage getUser(@PathVariable Long user_id){
        User userRes = userService.getUser(user_id);
        return new ResponseMessage<>(200, "success", userRes);
    }

    @PutMapping
    public ResponseMessage<User> edit(@RequestBody UserDto userDto){
        User userRes = userService.edit(userDto);
        return new ResponseMessage<>(200, "success", userRes);
    }

    @DeleteMapping("/{user_id}")
    public ResponseMessage delete(@PathVariable Long user_id){
        userService.delete(user_id);
        return new ResponseMessage<>(200, "success", null);
    }

    @PostMapping("/login")
    public ResponseMessage login(@RequestBody UserDto userDto){
        System.out.println(userDto.toString());
        User loginUser = userService.login(userDto);
        //为了安全性，返回时把密码设空
        loginUser.setPassword("");
        return new ResponseMessage<>(200, "success", loginUser);
    }

    @PostMapping("/register")
    public ResponseMessage register(@RequestBody UserDto userDto){  // 自动接收包含role的UserDto
        userService.add(userDto);  // 传递包含role的dto给Service层
        return new ResponseMessage<>(200, "success", null);
    }
 }