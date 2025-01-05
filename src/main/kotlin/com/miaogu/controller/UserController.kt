package com.miaogu.controller

import com.miaogu.service.UserService
import com.miaogu.dto.UserDTO
import com.miaogu.response.R
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/user")
class UserController(private val userService: UserService) {
    @PostMapping("/login")
    @ResponseBody
    fun login(@RequestBody userDTO: UserDTO): R<UserDTO> {
        return R.success( userService.login(userDTO.toEntity()), userDTO)
    }

    @PostMapping("/register")
    @ResponseBody
    fun register(@RequestBody userDTO: UserDTO): R<UserDTO>  {

        return R.success(userService.register(userDTO.toEntity()), userDTO)
    }
}
