package ai.yunxi.backend.controller;

import ai.yunxi.common.bean.DataTablesResult;
import ai.yunxi.common.bean.Result;
import ai.yunxi.common.utils.ResultUtil;
import ai.yunxi.core.domain.dto.ItemDto;
import ai.yunxi.core.domain.entity.TbItem;
import ai.yunxi.core.service.ItemService;
import ai.yunxi.search.service.SearchItemService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Api(description = "商品列表信息")
public class ItemController {

    private final static Logger log = LoggerFactory.getLogger(ItemController.class);

    @Autowired
    private ItemService itemService;
    @Autowired
    private SearchItemService searchItemService;

    @RequestMapping(value = "/item/{itemId}", method = RequestMethod.GET)
    @ApiOperation(value = "通过ID获取商品")
    public Result<ItemDto> getItemById(@PathVariable Long itemId) {
        ItemDto itemDto = itemService.getItemById(itemId);
        return new ResultUtil<ItemDto>().setData(itemDto);
    }

    @RequestMapping(value = "/item/list", method = RequestMethod.GET)
    @ApiOperation(value = "分页搜索排序获取商品列表")
    public DataTablesResult getItemList(int draw, int start, int length, int cid, @RequestParam("search[value]") String search,
                                        @RequestParam("order[0][column]") int orderCol, @RequestParam("order[0][dir]") String orderDir,
                                        String searchItem, String minDate, String maxDate) {
        //获取客户端需要排序的列
        String[] cols = {"checkbox", "id", "image", "title", "sell_point", "price", "created", "updated", "status"};
        String orderColumn = cols[orderCol];
        if (orderColumn == null) {
            orderColumn = "created";
        }
        //获取排序方式 默认为desc(asc)
        if (orderDir == null) {
            orderDir = "desc";
        }
        DataTablesResult result = itemService.getItemList(draw, start, length, cid, search, orderColumn, orderDir);
        return result;
    }

    @RequestMapping(value = "/item/listSearch", method = RequestMethod.GET)
    @ApiOperation(value = "多条件分页搜索排序获取商品列表")
    public DataTablesResult getItemSearchList(int draw, int start, int length, int cid, String searchKey, String minDate, String maxDate,
                                              @RequestParam("search[value]") String search, @RequestParam("order[0][column]") int orderCol,
                                              @RequestParam("order[0][dir]") String orderDir) {
        //获取客户端需要排序的列
        String[] cols = {"checkbox", "id", "image", "title", "sell_point", "price", "created", "updated", "status"};
        //默认排序列
        String orderColumn = cols[orderCol];
        if (orderColumn == null) {
            orderColumn = "created";
        }
        //获取排序方式 默认为desc(asc)
        if (orderDir == null) {
            orderDir = "desc";
        }
        if (!search.isEmpty()) {
            searchKey = search;
        }
        DataTablesResult result = itemService.getItemSearchList(draw, start, length, cid, searchKey, minDate, maxDate, orderColumn, orderDir);
        return result;
    }

    @RequestMapping(value = "/item/count", method = RequestMethod.GET)
    @ApiOperation(value = "获得商品总数目")
    public DataTablesResult getAllItemCount() {
        DataTablesResult result = itemService.getAllItemCount();
        return result;
    }

    @RequestMapping(value = "/item/stop/{id}", method = RequestMethod.PUT)
    @ApiOperation(value = "下架商品")
    public Result<TbItem> stopItem(@PathVariable Long id) {
        TbItem tbItem = itemService.alertItemState(id, 0);
        return new ResultUtil<TbItem>().setData(tbItem);
    }

    @RequestMapping(value = "/item/start/{id}", method = RequestMethod.PUT)
    @ApiOperation(value = "发布商品")
    public Result<TbItem> startItem(@PathVariable Long id) {
        TbItem tbItem = itemService.alertItemState(id, 1);
        return new ResultUtil<TbItem>().setData(tbItem);
    }

    @RequestMapping(value = "/item/del/{ids}", method = RequestMethod.DELETE)
    @ApiOperation(value = "删除商品")
    public Result<TbItem> deleteItem(@PathVariable Long[] ids) {
        for (Long id : ids) {
            itemService.deleteItem(id);
        }
        return new ResultUtil<TbItem>().setData(null);
    }

    @RequestMapping(value = "/item/add", method = RequestMethod.POST)
    @ApiOperation(value = "添加商品")
    public Result<TbItem> addItem(ItemDto itemDto) {
        TbItem tbItem = itemService.addItem(itemDto);
        return new ResultUtil<TbItem>().setData(tbItem);
    }

    @RequestMapping(value = "/item/update/{id}", method = RequestMethod.POST)
    @ApiOperation(value = "编辑商品")
    public Result<TbItem> updateItem(@PathVariable Long id, ItemDto itemDto) {
        TbItem tbItem = itemService.updateItem(id, itemDto);
        return new ResultUtil<TbItem>().setData(tbItem);
    }

    @RequestMapping(value = "/item/importIndex", method = RequestMethod.GET)
    @ApiOperation(value = "导入商品索引至ES")
    public Result<Object> importIndex() {
        searchItemService.importAllItems();
        return new ResultUtil<Object>().setData(null);
    }
}