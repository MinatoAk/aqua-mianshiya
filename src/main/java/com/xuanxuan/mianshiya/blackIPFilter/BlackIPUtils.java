package com.xuanxuan.mianshiya.blackIPFilter;

import cn.hutool.bloomfilter.BitMapBloomFilter;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

import java.util.List;
import java.util.Map;

/**
 * 黑名单过滤器工具类
 */

@Slf4j
public class BlackIPUtils {

    public static BitMapBloomFilter bloomFilter;

    public static boolean isBlackIp(String ip) {
        return bloomFilter.contains(ip);
    }

    /**
     * 重建黑名单
     */
    public static void rebuildBlackIp(String configInfo) {
        if (StrUtil.isBlank(configInfo)) {
            configInfo = "{}";
        }

        // 1) 解析 Nacos 配置文件
        Yaml yaml = new Yaml();
        Map map = yaml.loadAs(configInfo, Map.class);
        List<String> blackIps = (List<String>) map.get("blackIpList");

        // 2) 重建黑名单
        synchronized (BlackIPUtils.class) {
            if (CollUtil.isNotEmpty(blackIps)) {
                bloomFilter = new BitMapBloomFilter(958506);
                for (String blackIp : blackIps) {
                    bloomFilter.add(blackIp);
                }

            } else {
                bloomFilter = new BitMapBloomFilter(100);
            }
        }
    }
}
