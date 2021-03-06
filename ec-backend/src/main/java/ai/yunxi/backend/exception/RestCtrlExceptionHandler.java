package ai.yunxi.backend.exception;

import ai.yunxi.common.bean.Result;
import ai.yunxi.common.exception.CommonException;
import ai.yunxi.common.utils.ResultUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.net.BindException;

@ControllerAdvice
public class RestCtrlExceptionHandler {

    private static Logger log = LoggerFactory.getLogger(RestCtrlExceptionHandler.class);

    @ExceptionHandler(BindException.class)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public Result<Object> bindExceptionHandler(BindException e) {
        String errorMsg = "请求数据校验不合法: ";
        if (e != null) {
            errorMsg = e.getMessage();
            log.warn(errorMsg);
        }
        return new ResultUtil<>().setErrorMsg(errorMsg);
    }

    @ResponseStatus(value = HttpStatus.OK)
    @ExceptionHandler(CommonException.class)
    @ResponseBody
    public Result<Object> handleException(CommonException e) {
        String errorMsg = "Common exception: ";
        if (e != null) {
            errorMsg = e.getMsg();
            log.warn(e.getMessage());
        }
        return new ResultUtil<>().setErrorMsg(errorMsg);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public Result<Object> handleException(Exception e) {
        String errorMsg = "Exception: ";
        if (e != null) {
            log.warn(e.getMessage());
            if (e.getMessage() != null && e.getMessage().contains("Maximum upload size")) {
                errorMsg = "上传文件大小超过5MB限制";
            } else if (e.getMessage().contains("CommonException")) {
                errorMsg = e.getMessage();
                errorMsg = StringUtils.substringAfter(errorMsg, "CommonException:");
                errorMsg = StringUtils.substringBefore(errorMsg, "\n");
            } else {
                errorMsg = e.getMessage();
            }
        }
        return new ResultUtil<>().setErrorMsg(errorMsg);
    }
}
