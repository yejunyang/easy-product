package ai.yunxi.core.service;

import ai.yunxi.core.domain.entity.TbExpress;

import java.util.List;

public interface ExpressService {

    /**
     * 获取快递列表
     *
     * @return
     */
    List<TbExpress> getExpressList();

    /**
     * 添加快递
     *
     * @param tbExpress
     * @return
     */
    int addExpress(TbExpress tbExpress);

    /**
     * 更新快递
     *
     * @param tbExpress
     * @return
     */
    int updateExpress(TbExpress tbExpress);

    /**
     * 删除快递
     *
     * @param id
     * @return
     */
    int delExpress(int id);
}
