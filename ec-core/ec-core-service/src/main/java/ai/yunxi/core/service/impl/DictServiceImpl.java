package ai.yunxi.core.service.impl;

import ai.yunxi.core.service.DictService;
import ai.yunxi.common.Constants;
import ai.yunxi.core.domain.entity.TbDict;
import ai.yunxi.core.domain.entity.TbDictExample;
import ai.yunxi.core.domain.mapper.TbDictMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DictServiceImpl implements DictService {

    @Autowired
    private TbDictMapper tbDictMapper;

    @Override
    public List<TbDict> getDictList() {
        TbDictExample example = new TbDictExample();
        TbDictExample.Criteria criteria = example.createCriteria();
        //条件查询
       criteria.andTypeEqualTo(Constants.DICT_EXT);
        List<TbDict> list = tbDictMapper.selectByExample(example);
        return list;
    }

    @Override
    public List<TbDict> getStopList() {
        TbDictExample example = new TbDictExample();
        TbDictExample.Criteria criteria = example.createCriteria();
        //条件查询
        criteria.andTypeEqualTo(Constants.DICT_STOP);
        List<TbDict> list = tbDictMapper.selectByExample(example);
        return list;
    }

    @Override
    public int addDict(TbDict tbDict) {
        tbDictMapper.insert(tbDict);
        return 1;
    }

    @Override
    public int updateDict(TbDict tbDict) {
        tbDictMapper.updateByPrimaryKey(tbDict);
        return 1;
    }

    @Override
    public int delDict(int id) {
        tbDictMapper.deleteByPrimaryKey(id);
        return 1;
    }
}
