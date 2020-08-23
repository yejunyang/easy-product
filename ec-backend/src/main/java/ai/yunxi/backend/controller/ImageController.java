package ai.yunxi.backend.controller;

import ai.yunxi.common.bean.Result;
import ai.yunxi.common.utils.ResultUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

@RestController
@Api(description = "图片上传统一接口")
public class ImageController {

    @RequestMapping(value = "/image/imageUpload",method = RequestMethod.POST)
    @ApiOperation(value = "WebUploader图片上传")
    public Result<Object> uploadFile(@RequestParam("file") MultipartFile files,
                                     HttpServletRequest request){
        String imagePath=null;
        // 文件保存路径

        return new ResultUtil<Object>().setData(imagePath);
    }
}
