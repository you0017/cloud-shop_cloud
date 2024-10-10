package com.yc.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yc.context.BaseContext;
import com.yc.mapper.UserInformationMapper;
import com.yc.model.JsonModel;
import com.yc.bean.UserInformation;
import com.yc.service.UserInformationService;
import com.yc.utils.*;
import com.yc.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//@WebServlet("/html/user.action")
//@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 1024 * 1024 * 5, maxRequestSize = 1024 * 1024 * 5 * 5)

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private SendSms sendSms;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private UserInformationService userInformationService;
    @Autowired
    private UserInformationMapper userInformationMapper;

    /**
     * 发送验证码
     */
    @GetMapping("/sendCode")
    public JsonModel sendCode(@RequestParam("mobile") String mobile) throws Exception {
        //aliOSSUtils.upload(null);
        sendSms.send(mobile);
        return JsonModel.ok();
    }


    /**
     * 修改头像
     */
    @PostMapping("/modifyImg")
    public JsonModel modifyImg(@ModelAttribute UserVO userVO) {
        String url = userInformationService.modifyImg(userVO);

        return JsonModel.ok().setDate(url);
    }

    /**
     * 修改
     */
    @PostMapping("/modify")
    public JsonModel modify(@RequestParam("code") String code,@RequestParam("password") String password) {
        int i = userInformationService.modify(code,password);

        if (i==-1){
            return JsonModel.error("验证码错误");
        }

        if (i==0){
            return JsonModel.error("修改失败");
        }

        return JsonModel.ok();
    }

    /**
     * 登出
     * @param req
     * @param resp
     */
    /*protected void logout(HttpServletRequest req,HttpServletResponse resp) throws IOException {
        Jedis redis = RedisHelper.getRedisInstance();
        //删掉登录状态
        redis.del(YcConstants.SHOP_USERID+req.getSession().getId());
        redis.close();
        JsonModel jm = new JsonModel();
        jm.setCode(1);
        writeJson(jm,resp);
    }*/

    /**
     * 查看登录状态
     */
    @PostMapping("/checkLogin")
    public JsonModel checkLogin() {
        String id = BaseContext.getCurrentId();
        if (id==null||id.length()<=0||id.equals("")){
            return JsonModel.error();
        }
        //UserInformation user = userInformationMapper.selectById(id);
        UserInformation user = (UserInformation) redisTemplate.opsForValue().get(id);
        //登录了
        return JsonModel.ok().setDate(user);
    }

    /**
     * 登录
     */
    @PostMapping("/login")
    public JsonModel login(@RequestBody UserVO userVO) {
        String token = userInformationService.login(userVO);

        return JsonModel.ok().setDate(token);
    }

    /**
     * 注册
     */
    @PostMapping("/register")
    public JsonModel register(@ModelAttribute UserVO userVO) {

        UserInformation register = userInformationService.register(userVO);

        return JsonModel.ok();
    }

    /**
     * 根据id查用户，评论用
     */
    @GetMapping("/getById")
    public UserInformation getById(@RequestParam("id") Integer id) {
        UserInformation userInformation = userInformationMapper.selectById(id);

        return userInformation;
    }

    @GetMapping("/selectCount")
    public Long selectCount(){
        return userInformationMapper.selectCount(null);
    }

    @GetMapping("/getIdByAccountName")
    public UserInformation getIdByAccountName(@RequestParam("sender") String sender){
        LambdaQueryWrapper<UserInformation> db = new LambdaQueryWrapper<>();
        db.eq(UserInformation::getAccountname,sender);
        UserInformation userInformation = userInformationMapper.selectOne(db);
        return userInformation;
    }
}
