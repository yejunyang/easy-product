package ai.yunxi.sso.service.impl;

import ai.yunxi.common.exception.CommonException;
import ai.yunxi.core.domain.entity.TbMember;
import ai.yunxi.core.domain.entity.TbMemberExample;
import ai.yunxi.core.domain.mapper.TbMemberMapper;
import ai.yunxi.sso.service.RegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.List;

@Service
public class RegisterServiceImpl implements RegisterService {

    @Autowired
    private TbMemberMapper tbMemberMapper;

    @Override
    public boolean checkData(String param, int type) {
        TbMemberExample example = new TbMemberExample();
        TbMemberExample.Criteria criteria = example.createCriteria();
        criteria.andStateEqualTo(1);
        //1：用户名 2：手机号 3：邮箱
        if (type == 1) {
            criteria.andUsernameEqualTo(param);
        } else if (type == 2) {
            criteria.andPhoneEqualTo(param);
        } else if (type == 3) {
            criteria.andEmailEqualTo(param);
        } else {
            return false;
        }

        List<TbMember> list = tbMemberMapper.selectByExample(example);
        if (list != null && list.size() > 0) {
            return false;
        }
        return true;
    }

    @Override
    public int register(String userName, String userPwd) {
        TbMember tbMember = new TbMember();
        tbMember.setUsername(userName);
        // 用户名密码不能为空
        if (userName.isEmpty() || userPwd.isEmpty()) {
            return -1;
        }
        boolean result = checkData(userName, 1);
        // 该用户名已被注册
        if (!result) {
            return 0;
        }

        // MD5加密
        String md5Pass = DigestUtils.md5DigestAsHex(userPwd.getBytes());
        tbMember.setPassword(md5Pass);
        tbMember.setState(1);
        tbMember.setCreated(new Date());
        tbMember.setUpdated(new Date());
        if (tbMemberMapper.insert(tbMember) != 1) {
            throw new CommonException("注册用户失败");
        }
        return 1;
    }
}
