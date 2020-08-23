package ai.yunxi.search.impl;

import ai.yunxi.common.exception.CommonException;
import ai.yunxi.core.domain.dto.front.SearchItem;
import ai.yunxi.core.domain.dto.front.SearchResult;
import ai.yunxi.search.service.SearchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.matchQuery;

@Service
public class SearchServiceImpl implements SearchService {

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

    /**
     * 使用QueryBuilder
     * termQuery("key", obj) 完全匹配
     * termsQuery("key", obj1, obj2..)   一次匹配多个值
     * matchQuery("key", Obj) 单个匹配, field不支持通配符, 前缀具高级特性
     * multiMatchQuery("text", "field1", "field2"..);  匹配多个字段, field有通配符忒行
     */
    @Override
    public SearchResult search(String key, int page, int size, String sort, int priceGt, int priceLte) {
        try {
            Settings settings = Settings.builder()
                    .put("cluster.name", CLUSTER_NAME).build();
            TransportClient client = new PreBuiltTransportClient(settings)
                    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(HOST), 9300));
            SearchResult searchResult = new SearchResult();

            //设置查询条件
            //单字段搜索 match匹配
            QueryBuilder qb = matchQuery("productName", key);

            //设置分页
            if (page <= 0) {
                page = 1;
            }
            int start = (page - 1) * size;

            //设置高亮显示
            HighlightBuilder hiBuilder = new HighlightBuilder();
            hiBuilder.preTags("<a style=\"color: #e4393c\">");
            hiBuilder.postTags("</a>");
            hiBuilder.field("productName");

            //执行搜索
            SearchResponse searchResponse = null;
            if (priceGt >= 0 && priceLte >= 0 && sort.isEmpty()) {
                searchResponse = client.prepareSearch(INDEX_NAME)
                        .setTypes(INDEX_TYPE)
                        .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                        .setQuery(qb)    // Query
                        .setFrom(start).setSize(size).setExplain(true)    //从第几个开始，显示size个数据
                        .highlighter(hiBuilder)        //设置高亮显示
                        .setPostFilter(QueryBuilders.rangeQuery("salePrice").gt(priceGt).lt(priceLte))    //过滤条件
                        .get();
            } else if (priceGt >= 0 && priceLte >= 0 && sort.equals("1")) {
                searchResponse = client.prepareSearch(INDEX_NAME)
                        .setTypes(INDEX_TYPE)
                        .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                        .setQuery(qb)    // Query
                        .setFrom(start).setSize(size).setExplain(true)    //从第几个开始，显示size个数据
                        .highlighter(hiBuilder)        //设置高亮显示
                        .setPostFilter(QueryBuilders.rangeQuery("salePrice").gt(priceGt).lt(priceLte))    //过滤条件
                        .addSort("salePrice", SortOrder.ASC)
                        .get();
            } else if (priceGt >= 0 && priceLte >= 0 && sort.equals("-1")) {
                searchResponse = client.prepareSearch(INDEX_NAME)
                        .setTypes(INDEX_TYPE)
                        .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                        .setQuery(qb)    // Query
                        .setFrom(start).setSize(size).setExplain(true)    //从第几个开始，显示size个数据
                        .highlighter(hiBuilder)        //设置高亮显示
                        .setPostFilter(QueryBuilders.rangeQuery("salePrice").gt(priceGt).lt(priceLte))    //过滤条件
                        .addSort("salePrice", SortOrder.DESC)
                        .get();
            } else if ((priceGt < 0 || priceLte < 0) && sort.isEmpty()) {
                searchResponse = client.prepareSearch(INDEX_NAME)
                        .setTypes(INDEX_TYPE)
                        .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                        .setQuery(qb)    // Query
                        .setFrom(start).setSize(size).setExplain(true)    //从第几个开始，显示size个数据
                        .highlighter(hiBuilder)        //设置高亮显示
                        .get();
            } else if ((priceGt < 0 || priceLte < 0) && sort.equals("1")) {
                searchResponse = client.prepareSearch(INDEX_NAME)
                        .setTypes(INDEX_TYPE)
                        .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                        .setQuery(qb)    // Query
                        .setFrom(start).setSize(size).setExplain(true)    //从第几个开始，显示size个数据
                        .highlighter(hiBuilder)        //设置高亮显示
                        .addSort("salePrice", SortOrder.ASC)
                        .get();
            } else if ((priceGt < 0 || priceLte < 0) && sort.equals("-1")) {
                searchResponse = client.prepareSearch(INDEX_NAME)
                        .setTypes(INDEX_TYPE)
                        .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                        .setQuery(qb)    // Query
                        .setFrom(start).setSize(size).setExplain(true)    //从第几个开始，显示size个数据
                        .highlighter(hiBuilder)        //设置高亮显示
                        .addSort("salePrice", SortOrder.DESC)
                        .get();
            }

            SearchHits hits = searchResponse.getHits();
            //返回总结果数
            searchResult.setRecordCount(hits.totalHits);
            List<SearchItem> list = new ArrayList<>();
            if (hits.totalHits > 0) {
                for (SearchHit hit : hits) {
                    //总页数
                    int totalPage = (int) (hit.getScore() / size);
                    if ((hit.getScore() % size) != 0) {
                        totalPage++;
                    }
                    //返回结果总页数
                    searchResult.setTotalPages(totalPage);
                    //设置高亮字段
                    ObjectMapper om = new ObjectMapper();
                    SearchItem searchItem = om.readValue(hit.getSourceAsString(), SearchItem.class);
                    String productName = hit.getHighlightFields().get("productName").getFragments()[0].toString();
                    searchItem.setProductName(productName);
                    //返回结果
                    list.add(searchItem);
                }
            }
            searchResult.setItemList(list);
            //client.close();
            return searchResult;
        } catch (Exception e) {
            e.printStackTrace();
            throw new CommonException("查询ES索引库出错");
        }
    }
}
