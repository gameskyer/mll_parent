package com.xmcc.mll_es.config;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sun.rmi.transport.Transport;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Configuration
public class ElasticsearchConfig {
    @Bean
    public TransportClient client() throws UnknownHostException{
        TransportAddress transportAddress = new InetSocketTransportAddress(InetAddress.getByName("192.168.1.165"),9300);
        //设置集群名称
        Settings settings = Settings.builder().put("cluster.name","xmcc").build();
        TransportClient preBuiltTransportClient = new PreBuiltTransportClient(settings);
        preBuiltTransportClient.addTransportAddress(transportAddress);
        return preBuiltTransportClient;
    }

}
