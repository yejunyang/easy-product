package ai.yunxi.core.domain.mapper;

import ai.yunxi.core.domain.entity.TbPanelContent;
import ai.yunxi.core.domain.entity.TbPanelContentExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TbPanelContentMapper {
    long countByExample(TbPanelContentExample example);

    int deleteByExample(TbPanelContentExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(TbPanelContent record);

    int insertSelective(TbPanelContent record);

    List<TbPanelContent> selectByExample(TbPanelContentExample example);

    TbPanelContent selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") TbPanelContent record, @Param("example") TbPanelContentExample example);

    int updateByExample(@Param("record") TbPanelContent record, @Param("example") TbPanelContentExample example);

    int updateByPrimaryKeySelective(TbPanelContent record);

    int updateByPrimaryKey(TbPanelContent record);
}