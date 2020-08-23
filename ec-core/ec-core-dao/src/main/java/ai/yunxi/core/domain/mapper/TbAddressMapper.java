package ai.yunxi.core.domain.mapper;

import ai.yunxi.core.domain.entity.TbAddress;
import ai.yunxi.core.domain.entity.TbAddressExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TbAddressMapper {
    long countByExample(TbAddressExample example);

    int deleteByExample(TbAddressExample example);

    int deleteByPrimaryKey(Long addressId);

    int insert(TbAddress record);

    int insertSelective(TbAddress record);

    List<TbAddress> selectByExample(TbAddressExample example);

    TbAddress selectByPrimaryKey(Long addressId);

    int updateByExampleSelective(@Param("record") TbAddress record, @Param("example") TbAddressExample example);

    int updateByExample(@Param("record") TbAddress record, @Param("example") TbAddressExample example);

    int updateByPrimaryKeySelective(TbAddress record);

    int updateByPrimaryKey(TbAddress record);
}