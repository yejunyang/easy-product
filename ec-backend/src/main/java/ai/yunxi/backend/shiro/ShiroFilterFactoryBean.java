package ai.yunxi.backend.shiro;

import ai.yunxi.core.domain.entity.TbShiroFilter;
import ai.yunxi.core.service.SystemService;
import org.apache.shiro.config.Ini;
import org.apache.shiro.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class ShiroFilterFactoryBean extends org.apache.shiro.spring.web.ShiroFilterFactoryBean {

    private static final Logger log = LoggerFactory.getLogger(ShiroFilterFactoryBean.class);

    /**
     * 配置中的过滤链
     */
    public static String definitions;

    /**
     * 权限service
     */
    @Autowired
    private SystemService systemService;

    /**
     * 从数据库动态读取权限
     */
    @Override
    public void setFilterChainDefinitions(String definitions) {
        ShiroFilterFactoryBean.definitions = definitions;

        //数据库动态权限
        List<TbShiroFilter> list = systemService.getShiroFilter();
        for (TbShiroFilter tbShiroFilter : list) {
            //字符串拼接权限
            definitions = definitions + tbShiroFilter.getName() + " = " + tbShiroFilter.getPerms() + "\n";
        }

        log.info(definitions);

        //从配置文件加载权限配置
        Ini ini = new Ini();
        ini.load(definitions);
        Ini.Section section = ini.getSection("urls");
        if (CollectionUtils.isEmpty(section)) {
            section = ini.getSection("");
        }

        this.setFilterChainDefinitionMap(section);
    }
}
