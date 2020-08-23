package ai.yunxi.front.interceptor;

import ai.yunxi.common.Constants;
import ai.yunxi.common.annotation.RateLimiter;
import ai.yunxi.common.exception.CommonException;
import ai.yunxi.common.utils.IPUtils;
import ai.yunxi.front.limit.RedisRaterLimiter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

@Component
public class LimitRaterInterceptor extends HandlerInterceptorAdapter {

    @Value("${ec.rateLimit.enable}")
    private boolean rateLimitEnable;

    @Value("${ec.rateLimit.limit}")
    private Integer limit;

    @Value("${ec.rateLimit.timeout}")
    private Integer timeout;

    @Autowired
    private RedisRaterLimiter redisRaterLimiter;

    /**
     * 预处理回调方法，实现处理器的预处理（如登录检查）
     * 第三个参数为响应的处理器，即controller
     * 返回true，表示继续流程，调用下一个拦截器或者处理器
     * 返回false，表示流程中断，通过response产生响应
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {

        // IP限流每秒限10个请求
        String token1 = redisRaterLimiter.acquireTokenFromBucket("EC:" + IPUtils.getIpAddr(request), 10, 1000);
        if (StringUtils.isBlank(token1)) {
            throw new CommonException("你手速怎么这么快，请点慢一点");
        }

        if (rateLimitEnable) {
            String token2 = redisRaterLimiter.acquireTokenFromBucket(Constants.LIMIT_ALL, limit, timeout);
            if (StringUtils.isBlank(token2)) {
                throw new CommonException("当前访问总人数太多，请稍后再试");
            }
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();
        RateLimiter rateLimiter = method.getAnnotation(RateLimiter.class);
        if (rateLimiter != null) {
            int limit = rateLimiter.limit();
            int timeout = rateLimiter.timeout();
            String token3 = redisRaterLimiter.acquireTokenFromBucket(method.getName(), limit, timeout);
            if (StringUtils.isBlank(token3)) {
                throw new CommonException("当前访问人数太多，请稍后再试");
            }
        }
        return true;
    }

    /**
     * 当前请求进行处理之后，也就是Controller方法调用之后执行，
     * 但是它会在DispatcherServlet 进行视图返回渲染之前被调用。
     * 此时我们可以通过modelAndView对模型数据进行处理或对视图进行处理。
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler, ModelAndView modelAndView) throws Exception {

    }

    /**
     * 方法将在整个请求结束之后，也就是在DispatcherServlet渲染了对应的视图之后执行。
     * 这个方法的主要作用是用于进行资源清理工作的。
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) throws Exception {

    }
}
