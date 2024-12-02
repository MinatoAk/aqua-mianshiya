package com.xuanxuan.mianshiya.manager;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.MainResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class ESManager {

    /**
     * 检查 ES 连接状态
     *
     * @return
     */
    public boolean checkElasticsearch() {
        try (RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("localhost", 9200, "http")))) {
            // 发送 INFO 请求，如果成功则说明 ES 成功连接
            MainResponse response = client.info(RequestOptions.DEFAULT);
            return true;
        } catch (IOException e) {
            log.error("Error connecting to Elasticsearch: " + e.getMessage());
            return false;
        }
    }
}
