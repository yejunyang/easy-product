package ai.yunxi.backend.controller;

import ai.yunxi.common.Constants;
import ai.yunxi.common.bean.DataTablesResult;
import ai.yunxi.common.bean.Result;
import ai.yunxi.common.redis.JedisClient;
import ai.yunxi.common.utils.ResultUtil;
import ai.yunxi.core.domain.entity.TbDict;
import ai.yunxi.core.service.DictService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@Api(description = "词典库")
public class DictController {

    @Autowired
    private DictService dictService;

    @Autowired
    private JedisClient jedisClient;

    @RequestMapping(value = "/getDictList", method = RequestMethod.GET)
    @ApiOperation(value = "获得所有扩展词库")
    public String getDictExtList(HttpServletResponse response) {
        String result = "";
        String v = jedisClient.get(Constants.EXT_KEY);
        if (StringUtils.isNotBlank(v)) {
            return v;
        }
        List<TbDict> list = dictService.getDictList();
        for (TbDict tbDict : list) {
            result += tbDict.getDict() + "\n";
        }
        if (StringUtils.isNotBlank(result)) {
            jedisClient.set(Constants.EXT_KEY, result);
        }
        response.addHeader(Constants.LAST_MODIFIED, jedisClient.get(Constants.LAST_MODIFIED));
        response.addHeader(Constants.ETAG, jedisClient.get(Constants.ETAG));
        return result;
    }

    @RequestMapping(value = "/getStopDictList", method = RequestMethod.GET)
    @ApiOperation(value = "获得所有扩展词库")
    public String getStopDictList(HttpServletResponse response) {
        String result = "";
        String v = jedisClient.get(Constants.STOP_KEY);
        if (StringUtils.isNotBlank(v)) {
            return v;
        }
        List<TbDict> list = dictService.getStopList();
        for (TbDict tbDict : list) {
            result += tbDict.getDict() + "\n";
        }
        if (StringUtils.isNotBlank(result)) {
            jedisClient.set(Constants.STOP_KEY, result);
        }
        response.addHeader(Constants.LAST_MODIFIED, jedisClient.get(Constants.LAST_MODIFIED));
        response.addHeader(Constants.ETAG, jedisClient.get(Constants.ETAG));
        return result;
    }

    @RequestMapping(value = "/dict/list", method = RequestMethod.GET)
    @ApiOperation(value = "获得所有扩展词库")
    public DataTablesResult getDictList() {
        DataTablesResult result = new DataTablesResult();
        List<TbDict> list = dictService.getDictList();
        result.setData(list);
        result.setSuccess(true);
        return result;
    }

    @RequestMapping(value = "/dict/stop/list", method = RequestMethod.GET)
    @ApiOperation(value = "获得所有停用词库")
    public DataTablesResult getStopList() {
        DataTablesResult result = new DataTablesResult();
        List<TbDict> list = dictService.getStopList();
        result.setData(list);
        result.setSuccess(true);
        return result;
    }

    @RequestMapping(value = "/dict/add", method = RequestMethod.POST)
    @ApiOperation(value = "添加词典")
    public Result<Object> addDict(@ModelAttribute TbDict tbDict) {
        dictService.addDict(tbDict);
        update();
        return new ResultUtil<Object>().setData(null);
    }

    @RequestMapping(value = "/dict/update", method = RequestMethod.POST)
    @ApiOperation(value = "编辑词典")
    public Result<Object> updateDict(@ModelAttribute TbDict tbDict) {
        dictService.updateDict(tbDict);
        update();
        return new ResultUtil<Object>().setData(null);
    }

    @RequestMapping(value = "/dict/del/{ids}", method = RequestMethod.DELETE)
    @ApiOperation(value = "删除词典")
    public Result<Object> delDict(@PathVariable int[] ids) {
        for (int id : ids) {
            dictService.delDict(id);
        }
        update();
        return new ResultUtil<Object>().setData(null);
    }

    public void update() {
        //更新词典标识
        jedisClient.set(Constants.LAST_MODIFIED, String.valueOf(System.currentTimeMillis()));
        jedisClient.set(Constants.ETAG, String.valueOf(System.currentTimeMillis()));
        //更新缓存
        jedisClient.del(Constants.EXT_KEY);
        jedisClient.del(Constants.STOP_KEY);
    }
}
