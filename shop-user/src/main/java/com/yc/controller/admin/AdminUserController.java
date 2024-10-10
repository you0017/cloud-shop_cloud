package com.yc.controller.admin;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yc.bean.DataModel;
import com.yc.bean.UserInformation;
import com.yc.mapper.UserInformationMapper;
import com.yc.utils.EncryptUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

//@WebServlet("/admin/user.action")
@RestController
@RequestMapping("/user/admin")
public class AdminUserController {
    @Autowired
    private UserInformationMapper userInformationMapper;

    // 编辑更新
    @RequestMapping("/edit")
    public DataModel edit (HttpServletRequest request, HttpServletResponse response) throws IOException {
        DataModel dm = new DataModel();
        String id = request.getParameter("id");
        String accountname = request.getParameter("accountname");
        String name = request.getParameter("name");
        String password = request.getParameter("password");
        String email = request.getParameter("email");
        String role = request.getParameter("role");
        String status = request.getParameter("status");

        password = EncryptUtils.encryptToMD5(EncryptUtils.encryptToMD5(password));

        LambdaUpdateWrapper<UserInformation> updateWrapper = new LambdaUpdateWrapper<>();

        if (accountname != null && !accountname.trim().isEmpty()) {
            updateWrapper.set(UserInformation::getAccountname, accountname.trim());
        }

        if (name != null && !name.trim().isEmpty()) {
            updateWrapper.set(UserInformation::getName, name.trim());
        }

        if (password != null && !password.trim().isEmpty()) {
            updateWrapper.set(UserInformation::getPassword, password.trim());
        }

        if (email != null && !email.trim().isEmpty()) {
            updateWrapper.set(UserInformation::getEmail, email.trim());
        }

        if (role != null && !role.trim().isEmpty()) {
            updateWrapper.set(UserInformation::getRole, role.trim());
        }

        if (status != null && !status.trim().isEmpty()) {
            updateWrapper.set(UserInformation::getStatus, status.trim());
        }

        // 设置更新条件
        if (id != null && !id.trim().isEmpty()) {
            updateWrapper.eq(UserInformation::getId, id.trim());
        }

        // 执行更新
        int result = userInformationMapper.update(null, updateWrapper);

        if ( result>=1 ) {
            dm.setCode(0);
            dm.setMsg("用户编辑更新成功");
        }else {
            dm.setCode(1);
        }
        return dm;
    }


    // 添加
    public void addUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        /*DataModel dm = new DataModel();
        String accountname = request.getParameter("accountname");
        String password = request.getParameter("password");
        password = EncryptUtils.encryptToMD5(EncryptUtils.encryptToMD5(password));
        String name = request.getParameter("name");
        String status = request.getParameter("status");
        String email = request.getParameter("email");
        String role = request.getParameter("role");
        // 注意库里不能有空值
        String sql = "INSERT INTO userinformation (accountname, password, name, status, email,  role, logincount) VALUES ( ?,?,?,?,?,?,0 )";
        int i = db.doUpdate(sql, accountname, password, name, status, email, role);
        if (i>=1){
            dm.setCode(0);
        }else {
            dm.setCode(1);
        }
        writeJson(dm,response);*/
    }

    // 批量删除
    @RequestMapping("/batchDel")
    public DataModel batchDel(HttpServletRequest request, HttpServletResponse response) {
        DataModel ud = new DataModel();
        String idsStr = request.getParameter("idsStr");
        idsStr = idsStr.substring(0, idsStr.length() - 1);
        String[] strArray = idsStr.split(",");


        LambdaUpdateWrapper<UserInformation> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(UserInformation::getId, strArray);
        int i = userInformationMapper.delete(updateWrapper);


        if (i >= 1) {
            ud = allUserData(ud, request);
        } else {
            ud.setMsg("删除失败");
            ud.setCode(1);
        }
        return ud;
    }

    // 删除一个
    @PostMapping("/del")
    public DataModel del(HttpServletRequest request, HttpServletResponse response) throws InvocationTargetException, IllegalAccessException, InstantiationException, IOException {
        DataModel ud = new DataModel();
        String idsStr = request.getParameter("idsStr");

        int i = userInformationMapper.deleteById(idsStr);


        if (i >= 1) {
            ud.setCode(0);
            ud = allUserData(ud, request);
        } else {
            ud.setMsg("更新失败");
            ud.setCode(1);
        }
        return ud;
    }

    //  批量停用
    @RequestMapping("/batchDisabled")
    public DataModel batchDisabled(HttpServletRequest request, HttpServletResponse response) {
        DataModel ud = new DataModel();

        String idsStr = request.getParameter("idsStr");
        idsStr = idsStr.substring(0, idsStr.length() - 1);
        String[] strArray = idsStr.split(",");


        LambdaUpdateWrapper<UserInformation> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.in(UserInformation::getId,strArray).set(UserInformation::getStatus,0);
        int i = userInformationMapper.update(null, lambdaUpdateWrapper);


        if (i >= 1) {
            ud = allUserData(ud, request);
        } else {
            ud.setMsg("更新失败");
            ud.setCode(1);
        }
        return ud;
    }

    // 设置启用多个
    @RequestMapping("/batchEnabled")
    public DataModel batchEnabled(HttpServletRequest request, HttpServletResponse response) {
        DataModel ud = new DataModel();

        String idsStr = request.getParameter("idsStr");
        idsStr = idsStr.substring(0, idsStr.length() - 1);
        String[] strArray = idsStr.split(",");


        LambdaUpdateWrapper<UserInformation> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.in(UserInformation::getId,strArray).set(UserInformation::getStatus,1);
        int i = userInformationMapper.update(null, lambdaUpdateWrapper);


        if (i >= 1) {
            ud = allUserData(ud, request);
        } else {
            ud.setMsg("更新失败");
            ud.setCode(1);
        }
        return ud;
    }

    // 模糊查询
    @RequestMapping("/fuzzyQuery")
    public DataModel fuzzyQuery(HttpServletRequest request, HttpServletResponse response) {
        DataModel ud = new DataModel();
        String startTime = request.getParameter("startTime");
        String endTime = request.getParameter("endTime");
        String accountname = request.getParameter("accountname");// 账号
        String name = request.getParameter("name");  // 名字
        String status = request.getParameter("status");// 状态

        LambdaQueryWrapper<UserInformation> lambdaQueryWrapper = new LambdaQueryWrapper<>();

// 处理 accountname
        if (accountname != null && !accountname.trim().isEmpty()) {
            lambdaQueryWrapper.like(UserInformation::getAccountname, accountname.trim());
        }

// 处理 name
        if (name != null && !name.trim().isEmpty()) {
            lambdaQueryWrapper.like(UserInformation::getName, name.trim());
        }

// 处理 status
        if (status != null && !status.trim().isEmpty()) {
            lambdaQueryWrapper.eq(UserInformation::getStatus, status.trim());
        }

// 处理时间范围
        if (startTime != null && !startTime.isEmpty() && endTime != null && !endTime.isEmpty()) {
            lambdaQueryWrapper.between(UserInformation::getCreationtime, startTime.trim(), endTime.trim());
        } else if (startTime != null && !startTime.isEmpty()) {
            lambdaQueryWrapper.gt(UserInformation::getCreationtime, startTime.trim());
        } else if (endTime != null && !endTime.isEmpty()) {
            lambdaQueryWrapper.lt(UserInformation::getCreationtime, endTime.trim());
        }

// 执行查询
        List<UserInformation> select = userInformationMapper.selectList(lambdaQueryWrapper);

        int total = select.size();
        if ( select!=null && select.size()>0 ) {
            ud.setData(select);
            ud.setCode(0);
            ud.setMsg("成功");
            ud.setCount(total);
        }else {
            ud.setCode(1);
            ud.setMsg("失败");
        }
        return ud;
    }

    // 用户管理初始数据获取
    @RequestMapping("/getAllUserData")
    public DataModel getAllUserData(HttpServletRequest request, HttpServletResponse response) throws IOException, InvocationTargetException, InstantiationException, IllegalAccessException {
        DataModel ud = new DataModel();
//        ud = UserData.allUserData(ud, request);
       ud = initData(ud, request);
//        ud = allUserData(ud, request);
        return ud;
    }

    public DataModel initData(DataModel ud, HttpServletRequest request) {
        String limit1 = request.getParameter("limit");
        String page1 = request.getParameter("page");
        int limit = Integer.parseInt(limit1);
        int page = Integer.parseInt(page1);
        int skip = limit * (page-1);

        // 查总有多少条数据  select count(*) total from UserInformation;
        Long total = userInformationMapper.selectCount(null);

        // 查询指定条数  select * from UserInformation limit 5 offset 3;
        LambdaQueryWrapper<UserInformation> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.last("LIMIT " + limit + " OFFSET " + skip);
        List<UserInformation> limitMaps = userInformationMapper.selectList(lambdaQueryWrapper);

//        List<UserInformation> select = db.select(UserInformation.class, "select * from UserInformation");
        if ( limitMaps!=null && limitMaps.size()>0 ) {
            ud.setData(limitMaps);
            ud.setCode(0);
            ud.setMsg("成功");
            ud.setCount(Math.toIntExact(total));
        }else {
            ud.setCode(1);
            ud.setMsg("失败");
        }
        return ud;
    }

    // 获取所有用户数据的方法
    private DataModel allUserData (DataModel ud, HttpServletRequest request) {
        String limit1 = request.getParameter("limit");
        String page1 = request.getParameter("page");
        int limit = Integer.parseInt(limit1);
        int page = Integer.parseInt(page1);
        int skip = limit * (page-1);
        // 查总有多少条数据J  select count(*) total from UserInformation;

        Long total = userInformationMapper.selectCount(null);

        // 查询指定条数  select * from UserInformation limit 5 offset 3;

        LambdaQueryWrapper<UserInformation> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.last("LIMIT " + limit + " OFFSET " + skip);
        List<UserInformation> limitMaps = userInformationMapper.selectList(lambdaQueryWrapper);

//        List<UserInformation> select = db.select(UserInformation.class, "select * from UserInformation");
        if ( limitMaps!=null && limitMaps.size()>0 ) {
            ud.setData(limitMaps);
            ud.setCode(0);
            ud.setMsg("成功");
            ud.setCount(Math.toIntExact(total));
        }else {
            ud.setCode(1);
            ud.setMsg("失败");
        }
        return ud;
    }

}
