package ai.yunxi.sso.service.impl;

import ai.yunxi.common.redis.JedisClient;
import ai.yunxi.core.domain.dto.DtoUtil;
import ai.yunxi.core.domain.dto.front.Member;
import ai.yunxi.core.domain.entity.TbMember;
import ai.yunxi.core.domain.entity.TbMemberExample;
import ai.yunxi.core.domain.mapper.TbMemberMapper;
import ai.yunxi.sso.service.LoginService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private TbMemberMapper tbMemberMapper;
    @Autowired
    private JedisClient jedisClient;
    @Value("${SESSION_EXPIRE}")
    private Integer SESSION_EXPIRE;

    @Override
    public Member userLogin(String username, String password) {
        TbMemberExample example = new TbMemberExample();
        TbMemberExample.Criteria criteria = example.createCriteria();
        criteria.andStateEqualTo(1);
        criteria.andUsernameEqualTo(username);
        List<TbMember> list = tbMemberMapper.selectByExample(example);
        if (list == null || list.size() == 0) {
            Member member = new Member();
            member.setState(0);
            member.setMessage("用户名或密码错误");
            return member;
        }
        TbMember tbMember = list.get(0);
        //md5加密
        if (!DigestUtils.md5DigestAsHex(password.getBytes()).equals(tbMember.getPassword())) {
            Member member = new Member();
            member.setState(0);
            member.setMessage("用户名或密码错误");
            return member;
        }
        String token = UUID.randomUUID().toString();
        Member member = DtoUtil.TbMemer2Member(tbMember);
        member.setToken(token);
        member.setState(1);

        ObjectMapper om = new ObjectMapper();
        try {
            // 用户信息写入redis：key："SESSION:token" value："user"
            jedisClient.set("SESSION:" + token, om.writeValueAsString(member));
            jedisClient.expire("SESSION:" + token, SESSION_EXPIRE);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return member;
    }

    @Override
    public Member getUserByToken(String token) {
        String json = jedisClient.get("SESSION:" + token);
        if (json == null) {
            Member member = new Member();
            member.setState(0);
            member.setMessage("用户登录已过期");
            return member;
        }

        ObjectMapper om = new ObjectMapper();
        //重置过期时间
        jedisClient.expire("SESSION:" + token, SESSION_EXPIRE);
        Member member = null;
        try {
            member = om.readValue(json, Member.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return member;
    }

    @Override
    public int logout(String token) {
        jedisClient.del("SESSION:" + token);
        return 1;
    }
}
