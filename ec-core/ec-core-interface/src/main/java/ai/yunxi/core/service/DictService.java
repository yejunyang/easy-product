package ai.yunxi.core.service;

import ai.yunxi.core.domain.entity.TbDict;

import java.util.List;

public interface DictService {

    /**
     * 获取扩展词库列表
     *
     * @return
     */
    List<TbDict> getDictList();

    /**
     * 获取停用词库列表
     *
     * @return
     */
    List<TbDict> getStopList();

    /**
     * 添加
     *
     * @param tbDict
     * @return
     */
    int addDict(TbDict tbDict);

    /**
     * 更新
     *
     * @param tbDict
     * @return
     */
    int updateDict(TbDict tbDict);

    /**
     * 删除
     *
     * @param id
     * @return
     */
    int delDict(int id);
}
