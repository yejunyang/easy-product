package ai.yunxi.backend.controller;

import ai.yunxi.common.bean.DataTablesResult;
import ai.yunxi.common.bean.Result;
import ai.yunxi.common.utils.ResultUtil;
import ai.yunxi.content.service.ContentService;
import ai.yunxi.core.domain.entity.TbPanelContent;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Api(description = "板块内容管理")
public class ContentController {

    final static Logger log = LoggerFactory.getLogger(ContentController.class);

    @Autowired
    private ContentService contentService;

    @RequestMapping(value = "/content/list/{panelId}", method = RequestMethod.GET)
    @ApiOperation(value = "通过panelId获得板块内容列表")
    public DataTablesResult getContentByCid(@PathVariable int panelId) {
        DataTablesResult result = contentService.getPanelContentListByPanelId(panelId);
        return result;
    }

    @RequestMapping(value = "/content/add", method = RequestMethod.POST)
    @ApiOperation(value = "添加板块内容")
    public Result<Object> addContent(@ModelAttribute TbPanelContent tbPanelContent) {
        contentService.addPanelContent(tbPanelContent);
        return new ResultUtil<Object>().setData(null);
    }

    @RequestMapping(value = "/content/update", method = RequestMethod.POST)
    @ApiOperation(value = "编辑板块内容")
    public Result<Object> updateContent(@ModelAttribute TbPanelContent tbPanelContent) {
        contentService.updateContent(tbPanelContent);
        return new ResultUtil<Object>().setData(null);
    }

    @RequestMapping(value = "/content/del/{ids}", method = RequestMethod.DELETE)
    @ApiOperation(value = "删除板块内容")
    public Result<Object> addContent(@PathVariable int[] ids) {
        for (int id : ids) {
            contentService.deletePanelContent(id);
        }

        return new ResultUtil<Object>().setData(null);
    }
}
