package com.yc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yc.bean.UserInformation;
import com.yc.vo.UserVO;

public interface UserInformationService extends IService<UserInformation> {
    public String modifyImg(UserVO userVO);

    public int modify(String code, String password);

    public UserInformation register(UserVO userVO);

    public String login(UserVO userVO);
}
