package ai.yunxi.sso.service.impl;

import ai.yunxi.common.exception.CommonException;
import ai.yunxi.common.redis.JedisClient;
import ai.yunxi.core.domain.dto.front.Member;
import ai.yunxi.core.domain.entity.TbMember;
import ai.yunxi.core.domain.mapper.TbMemberMapper;
import ai.yunxi.sso.service.LoginService;
import ai.yunxi.sso.service.MemberService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MemberServiceImpl implements MemberService {

    @Autowired
    private LoginService loginService;
    @Autowired
    private TbMemberMapper tbMemberMapper;
    @Autowired
    private JedisClient jedisClient;
    @Value("${SESSION_EXPIRE}")
    private Integer SESSION_EXPIRE;

    @Override
    public String imageUpload(Long userId, String token, String imgData) {
        //过滤data:URL
        String base64 = "";
        String imgPath = "";

        TbMember tbMember = tbMemberMapper.selectByPrimaryKey(userId);
        if (tbMember == null) {
            throw new CommonException("通过id获取用户失败");
        }
        tbMember.setFile(imgPath);
        if (tbMemberMapper.updateByPrimaryKey(tbMember) != 1) {
            throw new CommonException("更新用户头像失败");
        }

        ObjectMapper om = new ObjectMapper();
        //更新缓存
        Member member = loginService.getUserByToken(token);
        member.setFile(imgPath);
        try {
            jedisClient.set("SESSION:" + token, om.writeValueAsString(member));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return imgPath;
    }
}
