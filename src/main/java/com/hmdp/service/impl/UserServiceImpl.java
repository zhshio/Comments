package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RegexUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;

import static com.hmdp.utils.SystemConstants.USER_NICK_NAME_PREFIX;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Override
    public Result sendCode(String phone, HttpSession session) {
        // 校验手机号
        if (null == phone || RegexUtils.isPhoneInvalid(phone)) {
            // 如果不符合, 返回错误信息
            return Result.fail("手机号格式错误!");
        }

        // 符合, 生成验证码
        String code = RandomUtil.randomNumbers(6);
        // 保存验证码到session
        session.setAttribute("code", code);
        // 发送验证码
        log.debug("发送验证码成功, 验证码{}", code);
        // 返回ok
        return Result.ok();
    }

    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        String phone = loginForm.getPhone();
        String code = loginForm.getCode();
        // 验证手机号, 不一致, 报错
        if (null == phone && RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail("手机格式错误");
        }
        // 验证验证码
        String cacheCode = (String) session.getAttribute("code");
        if (null == cacheCode || !cacheCode.equals(code)) {
            // 不一致, 报错
            return Result.fail("验证码错误");
        }
        // 判断用户是否存在
        User user = query().eq("phone", phone).one();
        // 不存在, 创建新用户保存
        if (null == user) {
            user = createUserWithPhone(phone);
        }
        // 保存用户信息到session
        session.setAttribute("user", BeanUtil.copyProperties(user, UserDTO.class));
        return Result.ok();
    }

    private User createUserWithPhone(String phone) {
        User user = new User();
        user.setPhone(phone);
        // 随机用户名
        user.setNickName(USER_NICK_NAME_PREFIX + RandomUtil.randomString(10));
        return user;
    }
}
