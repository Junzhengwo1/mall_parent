package com.kou.gulimall.authserver.controller;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.TypeReference;
import com.kou.gulimall.authserver.feign.MemberFeignService;
import com.kou.gulimall.authserver.feign.SmsFeignService;
import com.kou.gulimall.authserver.vo.UserLoginVo;
import com.kou.gulimall.authserver.vo.UserRegistVo;
import com.kou.gulimall.common.constant.AuthServerConstant;
import com.kou.gulimall.common.exception.BizCodeEnum;
import com.kou.gulimall.common.utils.R;
import com.kou.gulimall.common.vo.MemberRespVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Api(tags = "认证服务")
@RestController
@RequestMapping("/web")
public class RegistAndLoginController {


    @Autowired
    private SmsFeignService smsFeignService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private MemberFeignService memberFeignService;


    /**
     * 1、
     * //todo 发送一个请求直接到一个页面的处理方案：
     * todo 采用springMVC的映射 webConfig
     *
     * @return
     */


    /**2、
     * todo 应用场景的积累
     * @return 结果信息
     */
    @ApiOperation("发送短信验证码")
    @GetMapping("/sms/send-cms-code")
    public R sendCmsCode(@RequestParam("phone") String phone){
        String key = AuthServerConstant.SMS_CODE_CACHE_PREFIX+phone;
        //1、接口防刷
        String codeAndTime = redisTemplate.opsForValue().get(key);
        if(StrUtil.isNotBlank(codeAndTime)){
            long time = Long.parseLong(Objects.requireNonNull(codeAndTime).split("_")[1]);
            if(System.currentTimeMillis() - time < 60000){
                //60秒内不能再发验证码
                return R.error(BizCodeEnum.SMS_CODE_EXPCEPTION.getCode(),BizCodeEnum.SMS_CODE_EXPCEPTION.getMsg());
            }
        }
        //2、验证码的再次校验。redis存 key-phoneNum
        // 生成随机验证码
        String code = UUID.randomUUID().toString().substring(0, 5);
        String redisCacheCode = code+"_"+System.currentTimeMillis();
        //redis缓存验证码10分钟有效，防止同一个手机号在60秒内再次发送验证码
        redisTemplate.opsForValue().set(key,redisCacheCode,10, TimeUnit.MINUTES);
        smsFeignService.sendSmsCode(code,phone);
        return R.ok().put("data",code);
    }


    /**3、
     *
     * todo 其中参数校验应用场景多加回顾
     * @param vo 注册表单数据
     * @param result 参数校验信息
     * @return 结果信息
     */
    @ApiOperation("注册")
    @PostMapping("/regist")
    public R regist(@RequestBody @Valid UserRegistVo vo, BindingResult result){
        if (result.hasErrors()) {
            Map<String, String> map = new HashMap<>();
            //1.获取校验的结果
            result.getFieldErrors().forEach(item -> {
                //获取的错误信息
                String message = item.getDefaultMessage();
                //获取错误的属性的名字
                String field = item.getField();
                map.put(field, message);
            });
            return R.error(BizCodeEnum.REGIST_EXPCEPTION.getCode(),BizCodeEnum.REGIST_EXPCEPTION.getMsg()).put("data",map);
        }else {
            //正真的注册：
            String code = vo.getCode();
            String key = AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone();
            String s = redisTemplate.opsForValue().get(key);
            if(StrUtil.isNotBlank(s)){
                assert s != null;
                String rediscode = s.split("_")[0];
                if(rediscode.equals(code)){
                    //验证码通过 ， 开始正式注册
                    //先删除验证码
                    redisTemplate.delete(key);
                    //远程服务开始注册操作
                    R regist = memberFeignService.regist(vo);
                    //成功
                    if(regist.getCode() == 0){
                        return R.ok();
                    }else {
                        return R.error("注册失败");
                    }
                }else {
                    return R.error("验证码错误");
                }
            }else {
                return R.error("验证码错误");
            }
        }
    }


    /**
     * 登录接口
     * @param vo 登录入参
     * @param result 参数校验结果
     * @return 返回结果
     * todo 值得多次回顾，分布式session的应用场景
     */
    @ApiOperation("登录")
    @PostMapping("/login")
    public R login(@RequestBody @Valid UserLoginVo vo, BindingResult result, HttpSession session){
//        Map<String, String> map = result.getFieldErrors().stream()
//                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
        if (result.hasErrors()) {
            Map<String, String> map = new HashMap<>();
            //1.获取校验的结果
            result.getFieldErrors().forEach(item -> {
                //获取的错误信息
                String message = item.getDefaultMessage();
                //获取错误的属性的名字
                String field = item.getField();
                map.put(field, message);
            });
            return R.error(BizCodeEnum.LOGIN_EXPCEPTION.getCode(),BizCodeEnum.LOGIN_EXPCEPTION.getMsg()).put("data",map);
        }else {
            //远程登录
            R login = memberFeignService.Login(vo);
            if(login.getCode() == 0){
                MemberRespVo info = login.getData("data", new TypeReference<MemberRespVo>() {});
                //存在session中  也就是存在了 redis 中；采取redis存储session
//                String s = JSONObject.toJSONString(info,
//                        SerializerFeature.WriteNullStringAsEmpty,
//                        SerializerFeature.WriteDateUseDateFormat,
//                        SerializerFeature.WriteNullNumberAsZero);
                //session默认作用域是当前域
                //解决 域问题 这里采用的是redisSession
                session.setAttribute(AuthServerConstant.LOGIN_USER,info);
                return R.ok().put("data",info);
            }else {
                return R.error("登录失败");
            }
        }
    }


    @ApiOperation("session获取当前登录用户信息")
    @GetMapping("/getCurrentLoginUser")
    public R getCurrentLoginUser(HttpServletRequest request){
        Object loginUser = request.getSession(true).getAttribute(AuthServerConstant.LOGIN_USER);
        return R.ok().put(AuthServerConstant.LOGIN_USER,loginUser);
    }





}
