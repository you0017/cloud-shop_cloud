package com.yc.vo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserVO {

    private String username;
    private String password;
    private String email;
    private String code;
    private MultipartFile image;
    private String tel;
    private String name;
}
