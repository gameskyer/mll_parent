package com.xmcc.mll_es.controller;

import com.xmcc.mll_es.entity.EsProduct;
import com.xmcc.mllcommon.result.ResultResponse;
import com.xmcc.mllcommon.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.jackson.type.TypeReference;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import java.text.ParseException;

import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryAction;
import org.elasticsearch.index.reindex.DeleteByQueryRequestBuilder;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

@RestController
@RequestMapping("es")
@Slf4j
public class EsController {
    @Autowired
    private TransportClient transportClient;
    @PostMapping("addProduct")
    public ResultResponse addProduct(@RequestBody EsProduct esProduct) throws IOException, ParseException{
        log.info("添加数据为:{}",esProduct);
        //输入索引与type名称
        IndexResponse indexResponse =   transportClient.prepareIndex("mll", "product").
                //指定json字符串
                setSource(JsonUtil.object2string(esProduct), XContentType.JSON).get();
        return ResultResponse.success(indexResponse);
    }
    @PostMapping("updateProduct")
    public ResultResponse updateProduct(@RequestBody EsProduct esProduct) throws IOException, ParseException {
        log.info("修改的数据为:{}",esProduct );
        //输入索引与type名称
        UpdateResponse response = transportClient.prepareUpdate("mll","product","AWshpCBlZvAUqGXT3xXo").
                setDoc(JsonUtil.object2string(esProduct), XContentType.JSON).get();
        if(response.status() == RestStatus.OK){
            return ResultResponse.success();
        }
        return ResultResponse.fail();

    }
    //基本bool 精确查询
    @GetMapping("getProductList")
    public ResultResponse getProduct(EsProduct esProduct)throws IOException, ParseException{
        log.info("查询数据为：{}",esProduct );
        //通过基本的bool查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //对查询数据进行判断，如果不为空，就添加查询数据
        if(esProduct.getBrand()!=null){
            boolQueryBuilder.filter(QueryBuilders.termQuery("brand",esProduct.getBrand() ));
        }
        //这里先进行一个字段 然后开始查询  setTypes可以加入多个类型
        SearchRequestBuilder searchRequestBuilder = this.transportClient.prepareSearch("mll").setTypes("product")
        //查询条件
        .setQuery(boolQueryBuilder)
        //排序 第一个是排序字段
        .addSort("price", SortOrder.ASC)
        //翻页 从第几条开始，查询多少条
        .setFrom(0)
        .setSize(5);
        log.info("查询到的数据为：{}",searchRequestBuilder.toString() );
        //获得查询需要的数据结果
        SearchResponse searchResponse = searchRequestBuilder.get();
        //判断是否正常
        if (searchResponse.status()!= RestStatus.OK){
            log.warn("es查询出局异常，返回状态为:{}",searchResponse.status());
            return  ResultResponse.fail();
        }
        //获得结果集
        SearchHits hits = searchResponse.getHits();
        ArrayList<EsProduct> products = new ArrayList<>();
        //遍历组装数据
        for(SearchHit hit : hits){
            //字段名  这里先获得两个字段测试   如果以后字段多
            // 直接先把 es查询到的数据 转换为json字符串 然后再转成对象即可 ,这儿先详细的写一下
            //通过这个方法获得每条对象的json字符串 hit.getSourceAsString()
            EsProduct esProduct1 = new EsProduct();
            esProduct1.setBrand(String.valueOf(hit.getSource().get("brand")));
            esProduct1.setPrice((double) hit.getSource().get("price"));
            products.add(esProduct1);
        }
        return ResultResponse.success(products);
    }
    //模糊查询
    @GetMapping("getProductList2")
    //keyword类似搜索网页输入的值
    public ResultResponse getProductList2(String keyWord){
        log.info("查询的数据为：{}",keyWord );
        //通过基本的bool查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(
                //根据关键字查询 只要下面的字段中包含该数据,有的词语分词里面没有就查询不到，就需要前面学习的去自定义
                // 就会被查询到 这儿使用title与sell_point
                QueryBuilders.multiMatchQuery(keyWord,"title","sell_point")
        );
        //setType可以加入多个类型，不指定type与索引，就是查询所有的
        SearchRequestBuilder searchRequestBuilder = this.transportClient.prepareSearch("mll").setTypes("product")
                //查询条件
                .setQuery(boolQueryBuilder)
                //排序 第一个是排序字段 这儿使用价格
                .addSort("price", SortOrder.ASC)
                //翻页 从第几条开始  查询多少条
                .setFrom(0)
                .setSize(5);
        log.info("查询到的数据为：{}",searchRequestBuilder.toString());
        //获得查询需要的数据结果
        SearchResponse searchResponse = searchRequestBuilder.get();
        //判断是否正常
        if(searchResponse.status()!=RestStatus.OK){
            log.warn("es查询数据异常，返回状态为：{}",searchResponse.status());
            return ResultResponse.fail();
        }
        //获得结果集
        SearchHits hits = searchResponse.getHits();
        ArrayList<EsProduct> products = new ArrayList<>();
        //遍历组装数据
        for (SearchHit hit : hits) {
            String sourceAsString = hit.getSourceAsString();
            EsProduct esProduct = JsonUtil.string2object(sourceAsString, new TypeReference<EsProduct>() {
            });
            products.add(esProduct);
        }
        return ResultResponse.success(products);
    }
    //高亮显示及范围查询
    @GetMapping("getProductList3")
    public ResultResponse getProductList3(String keyWord) throws IOException, ParseException{
        log.info("查询的数据为:{}",keyWord );
        //通过基本的bool查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(
                //根据关键字查询 只要下面的字段中包含该数据,有的词语分词里面没有就查询不到，就需要前面学习的去自定义
                // 就会被查询到 这儿使用title与sell_point
                QueryBuilders.multiMatchQuery(
                        keyWord,
                        "title","sell_point"
                )
        );
        //这里先进行一个字段 然后开始查询  setTypes可以加入多个类型  不指定type与索引 就是查询所有的
        SearchRequestBuilder searchRequestBuilder = this.transportClient.prepareSearch("mll").setTypes("product")
                //查询条件
                .setQuery(boolQueryBuilder)
                //排序 第一个是排序字段 这儿使用价格
                .addSort("price", SortOrder.ASC)
                //翻页 从第几条开始  查询多少条
                .setFrom(0)
                .setSize(5);
        //按照价格区间查找
        searchRequestBuilder.setPostFilter(QueryBuilders.rangeQuery("price").from(100).to(100000));
        //高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<span color='red'>");
        highlightBuilder.postTags("</span>");
        //设置高亮字段
        highlightBuilder.field("title");
        //设置高亮条件
        searchRequestBuilder.highlighter(highlightBuilder);
        //执行搜索获得查询需要的数据结果
        SearchResponse searchResponse = searchRequestBuilder.get();
        //判断是否正常
        if(searchResponse.status()!=RestStatus.OK){
            log.warn("es查询数据异常，返回状态为：{}",searchResponse.status());
            return ResultResponse.fail();
        }
        //获得结果集
        SearchHits hits = searchResponse.getHits();
        log.info("查询到数据条数为:{}",hits.totalHits);
        //遍历组装数据
        for (SearchHit hit : hits) {

            for (Text title : hit.getHighlightFields().get("title").getFragments()) {
                //这里把高亮打出来看一看，可以清晰的看到就是标签字符串的拼接 ，拿到字符串了，相信大家能自己组装结果吧
                log.info("高亮字符串结果为:{}",title.string());
            }

        }
        return ResultResponse.success();
    }
    //删除文档
    @DeleteMapping("deletProduct")
    public ResultResponse deleteProduct(String title) throws IOException, ParseException{
        DeleteByQueryRequestBuilder builder = DeleteByQueryAction.INSTANCE
                .newRequestBuilder(transportClient)
                .filter(QueryBuilders.termQuery("title",title )).source("mll");
        BulkByScrollResponse response = builder.get();
        long deleted = response.getDeleted();
        log.info("删除的条数为:{}",deleted );
        return ResultResponse.success();
    }
}
