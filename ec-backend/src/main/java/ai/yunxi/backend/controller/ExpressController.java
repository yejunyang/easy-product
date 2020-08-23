package ai.yunxi.backend.controller;

import ai.yunxi.common.bean.DataTablesResult;
import ai.yunxi.common.bean.Result;
import ai.yunxi.common.utils.ResultUtil;
import ai.yunxi.core.domain.entity.TbExpress;
import ai.yunxi.core.service.ExpressService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Api(description = "快递")
public class ExpressController {

    @Autowired
    private ExpressService expressService;

    @RequestMapping(value = "/express/list", method = RequestMethod.GET)
    @ApiOperation(value = "获得所有快递")
    public DataTablesResult addressList() {
        DataTablesResult result = new DataTablesResult();
        List<TbExpress> list = expressService.getExpressList();
        result.setData(list);
        result.setSuccess(true);
        return result;
    }

    @RequestMapping(value = "/express/add", method = RequestMethod.POST)
    @ApiOperation(value = "添加快递")
    public Result<Object> addTbExpress(@ModelAttribute TbExpress tbExpress) {
        expressService.addExpress(tbExpress);
        return new ResultUtil<Object>().setData(null);
    }

    @RequestMapping(value = "/express/update", method = RequestMethod.POST)
    @ApiOperation(value = "编辑快递")
    public Result<Object> updateAddress(@ModelAttribute TbExpress tbExpress) {
        expressService.updateExpress(tbExpress);
        return new ResultUtil<Object>().setData(null);
    }

    @RequestMapping(value = "/express/del/{ids}", method = RequestMethod.DELETE)
    @ApiOperation(value = "删除快递")
    public Result<Object> delAddress(@PathVariable int[] ids) {
        for (int id : ids) {
            expressService.delExpress(id);
        }
        return new ResultUtil<Object>().setData(null);
    }
}
