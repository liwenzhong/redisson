/**
 * Copyright 2016 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.concurrent.ExecutorService;

import org.redisson.client.codec.Codec;
import org.redisson.codec.CodecProvider;
import org.redisson.codec.DefaultCodecProvider;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.liveobject.provider.DefaultResolverProvider;
import org.redisson.liveobject.provider.ResolverProvider;

import io.netty.channel.EventLoopGroup;

/**
 * Redisson configuration
 *
 * @author Nikita Koksharov
 *
 */
public class Config {

    private SentinelServersConfig sentinelServersConfig;

    private MasterSlaveServersConfig masterSlaveServersConfig;

    private SingleServerConfig singleServerConfig;

    private ClusterServersConfig clusterServersConfig;

    private ElasticacheServersConfig elasticacheServersConfig;

    /**
     * Threads amount shared between all redis node clients
     */
    private int threads = 0; // 0 = current_processors_amount * 2
    
    private int nettyThreads = 0; // 0 = current_processors_amount * 2

    /**
     * Redis key/value codec. JsonJacksonCodec used by default
     */
    private Codec codec;
    
    /**
     * For codec registry and look up. DefaultCodecProvider used by default
     */
    private CodecProvider codecProvider = new DefaultCodecProvider();
    
    /**
     * For resolver registry and look up. DefaultResolverProvider used by default
     */
    private ResolverProvider resolverProvider = new DefaultResolverProvider();
    
    private ExecutorService executor;
    
    /**
     * Config option for enabling Redisson Reference feature.
     * Default value is TRUE
     */
    private boolean redissonReferenceEnabled = true;
    
    private boolean useLinuxNativeEpoll;

    private EventLoopGroup eventLoopGroup;

    public Config() {
    }

    public Config(Config oldConf) {
        setUseLinuxNativeEpoll(oldConf.isUseLinuxNativeEpoll());
        setExecutor(oldConf.getExecutor());

        if (oldConf.getCodec() == null) {
            // use it by default
            oldConf.setCodec(new JsonJacksonCodec());
        }

        setNettyThreads(oldConf.getNettyThreads());
        setThreads(oldConf.getThreads());
        setCodec(oldConf.getCodec());
        setCodecProvider(oldConf.getCodecProvider());
        setResolverProvider(oldConf.getResolverProvider());
        setRedissonReferenceEnabled(oldConf.redissonReferenceEnabled);
        setEventLoopGroup(oldConf.getEventLoopGroup());
        if (oldConf.getSingleServerConfig() != null) {
            setSingleServerConfig(new SingleServerConfig(oldConf.getSingleServerConfig()));
        }
        if (oldConf.getMasterSlaveServersConfig() != null) {
            setMasterSlaveServersConfig(new MasterSlaveServersConfig(oldConf.getMasterSlaveServersConfig()));
        }
        if (oldConf.getSentinelServersConfig() != null) {
            setSentinelServersConfig(new SentinelServersConfig(oldConf.getSentinelServersConfig()));
        }
        if (oldConf.getClusterServersConfig() != null) {
            setClusterServersConfig(new ClusterServersConfig(oldConf.getClusterServersConfig()));
        }
        if (oldConf.getElasticacheServersConfig() != null) {
            setElasticacheServersConfig(new ElasticacheServersConfig(oldConf.getElasticacheServersConfig()));
        }

    }

    /**
     * Redis key/value codec. Default is json-codec
     *
     * @see org.redisson.client.codec.Codec
     * 
     * @param codec object
     * @return config
     */
    public Config setCodec(Codec codec) {
        this.codec = codec;
        return this;
    }

    public Codec getCodec() {
        return codec;
    }
    
    /**
     * For codec registry and look up. DefaultCodecProvider used by default.
     * 
     * @param codecProvider object 
     * @return config
     * @see org.redisson.codec.CodecProvider
     */
    public Config setCodecProvider(CodecProvider codecProvider) {
        this.codecProvider = codecProvider;
        return this;
    }

    /**
     * Returns the CodecProvider instance
     * 
     * @return CodecProvider
     */
    public CodecProvider getCodecProvider() {
        return codecProvider;
    }
    
    /**
     * For resolver registry and look up. DefaultResolverProvider used by default.
     * 
     * @param resolverProvider object
     * @return this
     */
    public Config setResolverProvider(ResolverProvider resolverProvider) {
        this.resolverProvider = resolverProvider;
        return this;
    }

    /**
     * Returns the ResolverProvider instance
     * 
     * @return resolverProvider
     */
    public ResolverProvider getResolverProvider() {
        return resolverProvider;
    }

    /**
     * Config option indicate whether Redisson Reference feature is enabled.
     * <p>
     * Default value is <code>true</code>
     * 
     * @return <code>true</code> if Redisson Reference feature enabled
     */
    public boolean isRedissonReferenceEnabled() {
        return redissonReferenceEnabled;
    }

    /**
     * Config option for enabling Redisson Reference feature
     * <p>
     * Default value is <code>true</code>
     * 
     * @param redissonReferenceEnabled flag
     */
    public void setRedissonReferenceEnabled(boolean redissonReferenceEnabled) {
        this.redissonReferenceEnabled = redissonReferenceEnabled;
    }
    
    /**
     * Init cluster servers configuration
     *
     * @return config
     */
    public ClusterServersConfig useClusterServers() {
        return useClusterServers(new ClusterServersConfig());
    }

    ClusterServersConfig useClusterServers(ClusterServersConfig config) {
        checkMasterSlaveServersConfig();
        checkSentinelServersConfig();
        checkSingleServerConfig();
        checkElasticacheServersConfig();

        if (clusterServersConfig == null) {
            clusterServersConfig = config;
        }
        return clusterServersConfig;
    }

    ClusterServersConfig getClusterServersConfig() {
        return clusterServersConfig;
    }

    void setClusterServersConfig(ClusterServersConfig clusterServersConfig) {
        this.clusterServersConfig = clusterServersConfig;
    }

    /**
     * Init AWS Elasticache servers configuration.
     *
     * @return ElasticacheServersConfig
     */
    public ElasticacheServersConfig useElasticacheServers() {
        return useElasticacheServers(new ElasticacheServersConfig());
    }

    ElasticacheServersConfig useElasticacheServers(ElasticacheServersConfig config) {
        checkClusterServersConfig();
        checkMasterSlaveServersConfig();
        checkSentinelServersConfig();
        checkSingleServerConfig();

        if (elasticacheServersConfig == null) {
            elasticacheServersConfig = new ElasticacheServersConfig();
        }
        return elasticacheServersConfig;
    }

    ElasticacheServersConfig getElasticacheServersConfig() {
        return elasticacheServersConfig;
    }

    void setElasticacheServersConfig(ElasticacheServersConfig elasticacheServersConfig) {
        this.elasticacheServersConfig = elasticacheServersConfig;
    }

    /**
     * Init single server configuration.
     *
     * @return SingleServerConfig
     */
    public SingleServerConfig useSingleServer() {
        return useSingleServer(new SingleServerConfig());
    }

    SingleServerConfig useSingleServer(SingleServerConfig config) {
        checkClusterServersConfig();
        checkMasterSlaveServersConfig();
        checkSentinelServersConfig();
        checkElasticacheServersConfig();

        if (singleServerConfig == null) {
            singleServerConfig = config;
        }
        return singleServerConfig;
    }

    SingleServerConfig getSingleServerConfig() {
        return singleServerConfig;
    }

    void setSingleServerConfig(SingleServerConfig singleConnectionConfig) {
        this.singleServerConfig = singleConnectionConfig;
    }

    /**
     * Init sentinel servers configuration.
     *
     * @return SentinelServersConfig
     */
    public SentinelServersConfig useSentinelServers() {
        return useSentinelServers(new SentinelServersConfig());
    }

    SentinelServersConfig useSentinelServers(SentinelServersConfig sentinelServersConfig) {
        checkClusterServersConfig();
        checkSingleServerConfig();
        checkMasterSlaveServersConfig();
        checkElasticacheServersConfig();

        if (this.sentinelServersConfig == null) {
            this.sentinelServersConfig = sentinelServersConfig;
        }
        return this.sentinelServersConfig;
    }

    SentinelServersConfig getSentinelServersConfig() {
        return sentinelServersConfig;
    }

    void setSentinelServersConfig(SentinelServersConfig sentinelConnectionConfig) {
        this.sentinelServersConfig = sentinelConnectionConfig;
    }

    /**
     * Init master/slave servers configuration.
     *
     * @return MasterSlaveServersConfig
     */
    public MasterSlaveServersConfig useMasterSlaveServers() {
        return useMasterSlaveServers(new MasterSlaveServersConfig());
    }

    MasterSlaveServersConfig useMasterSlaveServers(MasterSlaveServersConfig config) {
        checkClusterServersConfig();
        checkSingleServerConfig();
        checkSentinelServersConfig();
        checkElasticacheServersConfig();

        if (masterSlaveServersConfig == null) {
            masterSlaveServersConfig = config;
        }
        return masterSlaveServersConfig;
    }

    MasterSlaveServersConfig getMasterSlaveServersConfig() {
        return masterSlaveServersConfig;
    }

    void setMasterSlaveServersConfig(MasterSlaveServersConfig masterSlaveConnectionConfig) {
        this.masterSlaveServersConfig = masterSlaveConnectionConfig;
    }

    public boolean isClusterConfig() {
        return clusterServersConfig != null;
    }

    public int getThreads() {
        return threads;
    }

    /**
     * Threads amount shared across all listeners of <code>RTopic</code> object, 
     * invocation handlers of <code>RRemoteService</code> object  
     * and <code>RExecutorService</code> tasks.
     * <p>
     * Default is <code>0</code>.
     * <p>
     * <code>0</code> means <code>current_processors_amount * 2</code>
     *
     * @param threads amount
     * @return config
     */
    public Config setThreads(int threads) {
        this.threads = threads;
        return this;
    }

    private void checkClusterServersConfig() {
        if (clusterServersConfig != null) {
            throw new IllegalStateException("cluster servers config already used!");
        }
    }

    private void checkSentinelServersConfig() {
        if (sentinelServersConfig != null) {
            throw new IllegalStateException("sentinel servers config already used!");
        }
    }

    private void checkMasterSlaveServersConfig() {
        if (masterSlaveServersConfig != null) {
            throw new IllegalStateException("master/slave servers already used!");
        }
    }

    private void checkSingleServerConfig() {
        if (singleServerConfig != null) {
            throw new IllegalStateException("single server config already used!");
        }
    }

    private void checkElasticacheServersConfig() {
        if (elasticacheServersConfig != null) {
            throw new IllegalStateException("elasticache replication group servers config already used!");
        }
    }

    /**
     * Activates an unix socket if servers binded to loopback interface.
     * Also used for epoll transport activation.
     * <b>netty-transport-native-epoll</b> library should be in classpath
     *
     * @param useLinuxNativeEpoll flag
     * @return config
     */
    public Config setUseLinuxNativeEpoll(boolean useLinuxNativeEpoll) {
        this.useLinuxNativeEpoll = useLinuxNativeEpoll;
        return this;
    }

    public boolean isUseLinuxNativeEpoll() {
        return useLinuxNativeEpoll;
    }

    /**
     * Threads amount shared between all redis clients used by Redisson.
     * <p>
     * Default is <code>0</code>.
     * <p>
     * <code>0</code> means <code>current_processors_amount * 2</code>
     *
     * @param nettyThreads amount
     * @return config
     */
    public Config setNettyThreads(int nettyThreads) {
        this.nettyThreads = nettyThreads;
        return this;
    }
    
    public int getNettyThreads() {
        return nettyThreads;
    }
    
    /**
     * Use external ExecutorService. ExecutorService processes 
     * all listeners of <code>RTopic</code>, 
     * <code>RRemoteService</code> invocation handlers  
     * and <code>RExecutorService</code> tasks. 
     * 
     * @param executor object
     * @return config
     */
    public Config setExecutor(ExecutorService executor) {
        this.executor = executor;
        return this;
    }
    
    public ExecutorService getExecutor() {
        return executor;
    }

    /**
     * Use external EventLoopGroup. EventLoopGroup processes all
     * Netty connection tied with Redis servers. Each EventLoopGroup creates
     * own threads and each Redisson client creates own EventLoopGroup by default.
     * So if there are multiple Redisson instances in same JVM
     * it would be useful to share one EventLoopGroup among them.
     * <p>
     * Only {@link io.netty.channel.epoll.EpollEventLoopGroup} or
     * {@link io.netty.channel.nio.NioEventLoopGroup} can be used.
     *
     * @param eventLoopGroup object
     * @return config
     */
    public Config setEventLoopGroup(EventLoopGroup eventLoopGroup) {
        this.eventLoopGroup = eventLoopGroup;
        return this;
    }

    public EventLoopGroup getEventLoopGroup() {
        return eventLoopGroup;
    }

    /**
     * Read config object stored in JSON format from <code>String</code>
     *
     * @param content of config
     * @return config
     * @throws IOException error
     */
    public static Config fromJSON(String content) throws IOException {
        ConfigSupport support = new ConfigSupport();
        return support.fromJSON(content, Config.class);
    }

    /**
     * Read config object stored in JSON format from <code>InputStream</code>
     *
     * @param inputStream object
     * @return config
     * @throws IOException error
     */
    public static Config fromJSON(InputStream inputStream) throws IOException {
        ConfigSupport support = new ConfigSupport();
        return support.fromJSON(inputStream, Config.class);
    }

    /**
     * Read config object stored in JSON format from <code>File</code>
     *
     * @param file object
     * @return config
     * @throws IOException error
     */
    public static Config fromJSON(File file) throws IOException {
        ConfigSupport support = new ConfigSupport();
        return support.fromJSON(file, Config.class);
    }

    /**
     * Read config object stored in JSON format from <code>URL</code>
     *
     * @param url object
     * @return config
     * @throws IOException error
     */
    public static Config fromJSON(URL url) throws IOException {
        ConfigSupport support = new ConfigSupport();
        return support.fromJSON(url, Config.class);
    }

    /**
     * Read config object stored in JSON format from <code>Reader</code>
     *
     * @param reader object
     * @return config
     * @throws IOException error
     */
    public static Config fromJSON(Reader reader) throws IOException {
        ConfigSupport support = new ConfigSupport();
        return support.fromJSON(reader, Config.class);
    }

    /**
     * Convert current configuration to JSON format
     *
     * @return config in json format
     * @throws IOException error
     */
    public String toJSON() throws IOException {
        ConfigSupport support = new ConfigSupport();
        return support.toJSON(this);
    }

    /**
     * Read config object stored in YAML format from <code>String</code>
     *
     * @param content of config
     * @return config
     * @throws IOException error
     */
    public static Config fromYAML(String content) throws IOException {
        ConfigSupport support = new ConfigSupport();
        return support.fromYAML(content, Config.class);
    }

    /**
     * Read config object stored in YAML format from <code>InputStream</code>
     *
     * @param inputStream object
     * @return config
     * @throws IOException error
     */
    public static Config fromYAML(InputStream inputStream) throws IOException {
        ConfigSupport support = new ConfigSupport();
        return support.fromYAML(inputStream, Config.class);
    }

    /**
     * Read config object stored in YAML format from <code>File</code>
     *
     * @param file object
     * @return config
     * @throws IOException error
     */
    public static Config fromYAML(File file) throws IOException {
        ConfigSupport support = new ConfigSupport();
        return support.fromYAML(file, Config.class);
    }

    /**
     * Read config object stored in YAML format from <code>URL</code>
     *
     * @param url object
     * @return config
     * @throws IOException error
     */
    public static Config fromYAML(URL url) throws IOException {
        ConfigSupport support = new ConfigSupport();
        return support.fromYAML(url, Config.class);
    }

    /**
     * Read config object stored in YAML format from <code>Reader</code>
     *
     * @param reader object
     * @return config
     * @throws IOException error
     */
    public static Config fromYAML(Reader reader) throws IOException {
        ConfigSupport support = new ConfigSupport();
        return support.fromYAML(reader, Config.class);
    }

    /**
     * Convert current configuration to YAML format
     *
     * @return config in yaml format
     * @throws IOException error
     */
    public String toYAML() throws IOException {
        ConfigSupport support = new ConfigSupport();
        return support.toYAML(this);
    }

}
