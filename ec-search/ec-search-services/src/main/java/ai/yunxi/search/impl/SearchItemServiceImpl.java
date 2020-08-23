package ai.yunxi.search.impl;

import ai.yunxi.common.exception.CommonException;
import ai.yunxi.core.domain.dto.front.SearchItem;
import ai.yunxi.search.mapper.ItemMapper;
import ai.yunxi.search.service.SearchItemService;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.util.List;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

@Service
public class SearchItemServiceImpl implements SearchItemService {
    private final static Logger log = LoggerFactory.getLogger(SearchItemServiceImpl.class);

    @Autowired
    private ItemMapper itemMapper;

    @Value("${elasticsearch.host}")
    private String HOST;

    @Value("${elasticsearch.port}")
    private int PORT;

    @Value("${elasticsearch.clusterName}")
    private String CLUSTER_NAME;

    @Value("${index.name}")
    private String INDEX_NAME;

    @Value("${index.type}")
    private String INDEX_TYPE;

    @Override
    public int importAllItems() {
        try {
            Settings settings = Settings.builder()
                    .put("cluster.name", CLUSTER_NAME).build();
            TransportClient client = new PreBuiltTransportClient(settings)
                    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(HOST), PORT));

            //批量添加
            BulkRequestBuilder bulkRequest = client.prepareBulk();

            //查询商品列表
            List<SearchItem> itemList = itemMapper.getItemList();

            //遍历商品列表
            for (SearchItem searchItem : itemList) {
                String image = searchItem.getProductImageBig();
                if (image != null && !"".equals(image)) {
                    String[] strings = image.split(",");
                    image = strings[0];
                } else {
                    image = "";
                }
                searchItem.setProductImageBig(image);
                bulkRequest.add(client.prepareIndex(INDEX_NAME, INDEX_TYPE, String.valueOf(searchItem.getProductId()))
                        .setSource(jsonBuilder().startObject()
                                .field("productId", searchItem.getProductId())
                                .field("salePrice", searchItem.getSalePrice())
                                .field("productName", searchItem.getProductName())
                                .field("subTitle", searchItem.getSubTitle())
                                .field("productImageBig", searchItem.getProductImageBig())
                                .field("categoryName", searchItem.getCategoryName())
                                .field("cid", searchItem.getCid())
                                .endObject()
                        )
                );
            }
            BulkResponse bulkResponse = bulkRequest.get();
            log.info("更新索引成功");

            client.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new CommonException("导入ES索引库出错，请再次尝试");
        }

        return 1;
    }
}
