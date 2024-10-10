package com.yc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yc.bean.UserInformation;
import com.yc.context.BaseContext;
import com.yc.mapper.UserInformationMapper;
import com.yc.model.JsonModel;
import com.yc.service.UserInformationService;
import com.yc.utils.AliOSSUtils;
import com.yc.utils.EncryptUtils;
import com.yc.utils.JmsMessageProducer;
import com.yc.utils.JwtTokenUtil;
import com.yc.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class UserInformationServiceImpl extends ServiceImpl<UserInformationMapper, UserInformation> implements UserInformationService {
    @Autowired
    private AliOSSUtils aliOSSUtils;
    @Autowired
    private UserInformationMapper userInformationMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private JmsMessageProducer jmsMessageProducer;

    @Override
    public String modifyImg(UserVO userVO) {
        String url;
        try {
            url = aliOSSUtils.upload(userVO.getImage());
        } catch (Exception e) {
            throw new RuntimeException("头像修改失败");
        }
        LambdaUpdateWrapper<UserInformation> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserInformation::getId, BaseContext.getCurrentId());
        updateWrapper.set(UserInformation::getImage, url);
        userInformationMapper.update(null, updateWrapper);
        return url;
    }

    @Override
    public int modify(String code, String password) {
        String userId = BaseContext.getCurrentId();
        /*LambdaQueryWrapper<UserInformation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserInformation::getId,userId);*/
        UserInformation user = (UserInformation) redisTemplate.opsForValue().get(userId);

        //找到对应验证码
        String redisCode = (String) redisTemplate.opsForValue().get(user.getMobile());

        if (redisCode==null||!redisCode.equals(code)){
            return -1;
        }

        //更新时间
        LocalDateTime localDateTime = LocalDateTime.now();
        user.setUpdatetime(localDateTime.toString());

        if (password!=null&&!password.equals("")){
            user.setPassword(EncryptUtils.encryptToMD5(EncryptUtils.encryptToMD5(password)));
        }


        LambdaUpdateWrapper<UserInformation> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserInformation::getId,userId);
        int i = userInformationMapper.update(user, updateWrapper);

        return i;
    }

    @Override
    public UserInformation register(UserVO userVO) {
        if (redisTemplate.opsForValue().get(userVO.getTel())==null||!redisTemplate.opsForValue().get(userVO.getTel()).equals(userVO.getCode())){
            //return JsonModel.error("验证码错误");
            throw new RuntimeException("验证码错误");
        }


        String url = null;
        try {
            url = aliOSSUtils.upload(userVO.getImage());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("头像上传有问题");
        }

        if (userVO.getUsername()==null||userVO.getUsername().equals("")||userVO.getName()==null||userVO.getName().equals("")||userVO.getEmail().equals("")||userVO.getEmail()==null||userVO.getTel().equals("")||userVO.getTel()==null||userVO.getPassword().equals("")||userVO.getPassword()==null){
            //return JsonModel.error("请填写完整");
            throw new RuntimeException("请填写完整");
        }



        //看看用户是否存在
        LambdaQueryWrapper<UserInformation> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserInformation::getAccountname,userVO.getUsername());
        List<UserInformation> user = userInformationMapper.selectList(lambdaQueryWrapper);
        if (user.size()>0){
            //return JsonModel.error("用户已存在");
            throw new RuntimeException("用户已存在");
        }

        //加密
        String password = EncryptUtils.encryptToMD5(EncryptUtils.encryptToMD5(userVO.getPassword()));

        UserInformation userInformation = new UserInformation();
        userInformation.setAccountname(userVO.getUsername());
        userInformation.setPassword(password);
        userInformation.setName(userVO.getName());
        userInformation.setStatus("1");
        userInformation.setEmail(userVO.getEmail());
        userInformation.setLogincount(0);
        userInformation.setRole(String.valueOf(1));
        userInformation.setMobile(userVO.getTel());
        userInformation.setImage(url);

        LocalDateTime now = LocalDateTime.now();
        userInformation.setCreationtime(String.valueOf(now));
        userInformation.setUpdatetime(String.valueOf(now));

        LambdaUpdateWrapper<UserInformation> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.set(UserInformation::getAccountname,userInformation.getAccountname())
                .set(UserInformation::getPassword,userInformation.getPassword())
                .set(UserInformation::getName,userInformation.getName())
                .set(UserInformation::getStatus,userInformation.getStatus())
                .set(UserInformation::getEmail,userInformation.getEmail())
                .set(UserInformation::getLogincount,userInformation.getLogincount())
                .set(UserInformation::getCreationtime,userInformation.getCreationtime())
                .set(UserInformation::getUpdatetime,userInformation.getUpdatetime())
                .set(UserInformation::getRole,userInformation.getRole())
                .set(UserInformation::getMobile,userInformation.getMobile())
                .set(UserInformation::getImage,userInformation.getImage());

        int i = userInformationMapper.insert(userInformation);

        if (i==0){
            //新增失败
            //return JsonModel.error("注册失败");
            throw new RuntimeException("注册失败");
        }

        //新增成功,发送邮箱
        //jmsMessageProducer.registerMessage(userInformation);

        //删除验证码
        redisTemplate.delete(userVO.getTel());

        return userInformation;
    }

    @Override
    public String login(UserVO userVO) {
        LambdaQueryWrapper<UserInformation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserInformation::getAccountname,userVO.getUsername());

        List<UserInformation> user = userInformationMapper.selectList(queryWrapper);

        //看注册了没有
        if (user==null||user.size()<=0){
            //return JsonModel.error("用户未注册");
            throw new RuntimeException("用户未注册");
        }

        //看是否处于封禁
        UserInformation userInformation = user.get(0);
        if (userInformation.getStatus().equals(0)){
            //return JsonModel.error("用户被封禁");
            throw new RuntimeException("用户被封禁");
        }

        //看密码是否正确
        if (!userInformation.getPassword().equals(EncryptUtils.encryptToMD5(EncryptUtils.encryptToMD5(userVO.getPassword())))){
            //return JsonModel.error("密码错误");
            throw new RuntimeException("密码错误");
        }

        //登录成功
        //登录次数+1
        userInformation.setLogincount(userInformation.getLogincount()+1);
        LambdaUpdateWrapper<UserInformation> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserInformation::getId,userInformation.getId())
                .set(UserInformation::getLogincount,userInformation.getLogincount());
        userInformationMapper.update(userInformation,updateWrapper);

        //生成token
        Map<String, Object> map = new HashMap<>();
        map.put("id",userInformation.getId());
        map.put("username",userInformation.getAccountname());
        String token = jwtTokenUtil.encodeJWT(map);

        //把用户信息存进redis，减少数据库访问
        redisTemplate.opsForValue().set(String.valueOf(userInformation.getId()),userInformation);

        return token;
    }
}
