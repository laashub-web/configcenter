/* 
 * 作者：钟勋 (e-mail:zhongxunking@163.com)
 */

/*
 * 修订记录:
 * @author 钟勋 2017-09-12 19:10 创建
 */
package org.antframework.configcenter.client.support;

import com.alibaba.fastjson.JSON;
import org.antframework.common.util.facade.AbstractResult;
import org.antframework.configcenter.client.ConfigContext;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 服务端查询器
 */
public class ServerQuerier {
    private static final Logger logger = LoggerFactory.getLogger(ServerQuerier.class);
    // 查询属性url
    private static final String QUERY_PROPERTIES_SUFFIX_URL = "/config/queryProperties";

    // 发送给服务端的请求
    private HttpUriRequest request;
    // 发送http请求的客户端
    private CloseableHttpClient httpClient;

    public ServerQuerier(ConfigContext.InitParams initParams) {
        request = buildRequest(initParams);
        httpClient = HttpClients.createDefault();
    }

    /**
     * 查询属性
     */
    public Map<String, String> queryProperties() {
        try {
            logger.info("调用服务端读取配置，入参：{}", request);
            String resultStr = httpClient.execute(request, new BasicResponseHandler());
            logger.info("调用服务端读取配置，出参：{}", resultStr);
            QueryPropertiesResult result = JSON.parseObject(resultStr, QueryPropertiesResult.class);
            if (result == null) {
                throw new RuntimeException("请求服务端失败");
            }
            if (!result.isSuccess() || result.getProperties() == null) {
                throw new RuntimeException("查询配置失败：" + result.getMessage());
            }
            return result.getProperties();
        } catch (IOException e) {
            return ExceptionUtils.rethrow(e);
        }
    }

    /**
     * 关闭（释放相关资源）
     */
    public void close() {
        try {
            httpClient.close();
        } catch (IOException e) {
            logger.error("关闭httpClient失败：", e);
        }
    }

    // 构建请求
    private HttpUriRequest buildRequest(ConfigContext.InitParams initParams) {
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("appCode", initParams.getAppCode()));
        params.add(new BasicNameValuePair("queriedAppCode", initParams.getQueriedAppCode()));
        params.add(new BasicNameValuePair("profileCode", initParams.getProfileCode()));

        HttpPost httpPost = new HttpPost(initParams.getServerUrl() + QUERY_PROPERTIES_SUFFIX_URL);
        httpPost.setEntity(new UrlEncodedFormEntity(params, Charset.forName("utf-8")));
        return httpPost;
    }

    // 查询应用在特定环境中的配置result
    private static class QueryPropertiesResult extends AbstractResult {
        // 属性（不存在该应用或环境，则返回null）
        private Map<String, String> properties;

        public Map<String, String> getProperties() {
            return properties;
        }

        public void setProperties(Map<String, String> properties) {
            this.properties = properties;
        }
    }
}