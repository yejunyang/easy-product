package ai.yunxi.front.controller;

import ai.yunxi.common.bean.Result;
import ai.yunxi.common.redis.JedisClient;
import ai.yunxi.common.utils.ResultUtil;
import ai.yunxi.core.domain.dto.front.CommonDto;
import ai.yunxi.core.domain.dto.front.Member;
import ai.yunxi.core.domain.dto.front.MemberLoginRegist;
import ai.yunxi.sso.service.LoginService;
import ai.yunxi.sso.service.MemberService;
import ai.yunxi.sso.service.RegisterService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

@RestController
@Api(description = "会员注册登录")
public class MemberController {

    private final static Logger log= LoggerFactory.getLogger(MemberController.class);

    @Autowired
    private LoginService loginService;
    @Autowired
    private RegisterService registerService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private JedisClient jedisClient;

    @RequestMapping(value = "/member/login",method = RequestMethod.POST)
    @ApiOperation(value = "用户登录")
    public Result<Member> login(@RequestBody MemberLoginRegist memberLoginRegist,
                                HttpServletRequest request){
        String challenge=memberLoginRegist.getChallenge();
        String validate=memberLoginRegist.getValidate();
        String seccode=memberLoginRegist.getSeccode();

        //自定义参数,可选择添加
        HashMap<String, String> param = new HashMap<>();
        int gtResult = 0;
        // 省略验证过程

        Member member=new Member();
        if (gtResult == 1) {
            // 验证成功
            member=loginService.userLogin(memberLoginRegist.getUserName(), memberLoginRegist.getUserPwd());
        }
        else {
            // 验证失败
            member.setState(0);
            member.setMessage("验证失败");
        }
        return new ResultUtil<Member>().setData(member);
    }

    @RequestMapping(value = "/member/checkLogin",method = RequestMethod.GET)
    @ApiOperation(value = "判断用户是否登录")
    public Result<Member> checkLogin(@RequestParam(defaultValue = "") String token){
        Member member=loginService.getUserByToken(token);
        return new ResultUtil<Member>().setData(member);
    }

    @RequestMapping(value = "/member/loginOut",method = RequestMethod.GET)
    @ApiOperation(value = "退出登录")
    public Result<Object> logout(@RequestParam(defaultValue = "") String token){
        loginService.logout(token);
        return new ResultUtil<Object>().setData(null);
    }

    @RequestMapping(value = "/member/register",method = RequestMethod.POST)
    @ApiOperation(value = "用户注册")
    public Result<Object> register(@RequestBody MemberLoginRegist memberLoginRegist,
                                   HttpServletRequest request){
        String challenge=memberLoginRegist.getChallenge();
        String validate=memberLoginRegist.getValidate();
        String seccode=memberLoginRegist.getSeccode();

        //自定义参数,可选择添加
        HashMap<String, String> param = new HashMap<>();
        int gtResult = 1;
        // 省略验证过程

        if (gtResult == 1) {
            // 验证成功
            int result=registerService.register(memberLoginRegist.getUserName(), memberLoginRegist.getUserPwd());
            if(result==0){
                return new ResultUtil<Object>().setErrorMsg("该用户名已被注册");
            }else if(result==-1){
                return new ResultUtil<Object>().setErrorMsg("用户名密码不能为空");
            }
            return new ResultUtil<Object>().setData(result);
        }
        else {
            // 验证失败
            return new ResultUtil<Object>().setErrorMsg("验证失败");
        }
    }

    @RequestMapping(value = "/member/imgaeUpload",method = RequestMethod.POST)
    @ApiOperation(value = "用户头像上传")
    public Result<Object> imgaeUpload(@RequestBody CommonDto common){
        String imgPath = memberService.imageUpload(common.getUserId(),common.getToken(),common.getImgData());
        return new ResultUtil<Object>().setData(imgPath);
    }
}
