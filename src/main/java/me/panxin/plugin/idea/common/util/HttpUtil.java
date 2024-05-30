package me.panxin.plugin.idea.common.util;

import com.google.common.collect.Maps;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.proxy.CommonProxy;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.net.Proxy.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * http工具类
 *
 * @author panxin
 * @date 2024/05/30
 */
public class HttpUtil {
    private static final Logger LOGGER = Logger.getInstance(HttpUtil.class);
    private static final int CONNECT_TIMEOUT = 1000;
    private static final int SOCKET_TIMEOUT = 1000;

    private HttpUtil() {}

    /**
     * get请求
     *
     * @param url url
     * @return {@link String}
     */
    public static String get(String url) {
        return get(url, CONNECT_TIMEOUT, SOCKET_TIMEOUT);
    }

    /**
     * get请求
     *
     * @param url url
     * @param connectTimeout 连接超时
     * @param socketTimeout 读超时
     * @return {@link String}
     */
    public static String get(String url, int connectTimeout, int socketTimeout) {
        if (StringUtils.isBlank(url)) {
            return null;
        }
        String result = null;
        CloseableHttpClient httpclient = null;
        CloseableHttpResponse response = null;
        try {
            // 代理
            HttpHost httpHost = getProxy(url);
            Builder build = RequestConfig.custom().setSocketTimeout(socketTimeout).setConnectTimeout(connectTimeout);
            if (httpHost != null) {
                build.setProxy(httpHost);
            }

            httpclient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(url);
            httpGet.setConfig(build.build());
            response = httpclient.execute(httpGet);
            result = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            LOGGER.warn("请求" + url + "异常", e);
        } finally {
            HttpClientUtils.closeQuietly(response);
            HttpClientUtils.closeQuietly(httpclient);
        }
        return result;
    }

    /**
     * get请求
     *
     * @param url url
     * @param params 参数
     * @return {@link String}
     */
    public static String get(String url, Map<String, Object> params) {
        if (StringUtils.isBlank(url)) {
            return null;
        }
        String paramStr = params.entrySet().stream()
            .map(e -> e.getKey() + "=" + encode(String.valueOf(e.getValue()))).collect(Collectors.joining("&"));
        url = url.contains("?") ? url + "&" + paramStr : url + "?" + paramStr;
        return get(url);
    }

    /**
     * 编码
     *
     * @param word word
     * @return {@link String}
     */
    public static String encode(String word) {
        try {
            return URLEncoder.encode(word, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            LOGGER.warn("url转义失败,word=" + word, e);
            return StringUtils.EMPTY;
        }
    }

    /**
     * post请求
     *
     * @param url url
     * @param headers headers
     * @param body 内容
     * @return {@link String}
     */
    public static String post(String url, Map<String, String> headers, String body) {
        if (StringUtils.isBlank(url)) {
            return null;
        }
        String result = null;
        CloseableHttpClient httpclient = null;
        CloseableHttpResponse response = null;
        try {
            // 代理
            HttpHost httpHost = getProxy(url);

            Builder builder = RequestConfig.custom().setSocketTimeout(SOCKET_TIMEOUT).setConnectTimeout(
                CONNECT_TIMEOUT);
            if (httpHost != null) {
                builder.setProxy(httpHost);
            }
            httpclient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(url);
            if (headers != null) {
                for (Entry<String, String> e : headers.entrySet()) {
                    httpPost.addHeader(e.getKey(), e.getValue());
                }
            }
            httpPost.setConfig(builder.build());
            httpPost.setEntity(new StringEntity(body, StandardCharsets.UTF_8));
            response = httpclient.execute(httpPost);
            result = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.warn("请求" + url + "异常", e);
        } finally {
            HttpClientUtils.closeQuietly(response);
            HttpClientUtils.closeQuietly(httpclient);
        }
        return result;
    }

    private static HttpHost getProxy(String url) throws MalformedURLException {
        List<Proxy> proxies = CommonProxy.getInstance().select(new URL(url));
        // 代理
        HttpHost httpHost = null;
        if (proxies != null && !proxies.isEmpty()) {
            for (Proxy proxy : proxies) {
                if (proxy == null || Type.DIRECT.equals(proxy.type())) {
                    continue;
                }
                InetSocketAddress address = (InetSocketAddress)proxy.address();
                httpHost = new HttpHost(address.getHostName(), address.getPort());
            }
        }
        return httpHost;
    }

    /**
     * post请求（json）
     *
     * @param url url
     * @param headers headers
     * @param body 内容
     * @return {@link String}
     */
    public static String postJson(String url, Map<String, String> headers, String body) {
        if (headers == null) {
            headers = Maps.newHashMap();
        }
        headers.put("Content-Type", "application/json;charset=utf-8");
        return post(url, headers, body);
    }

}
