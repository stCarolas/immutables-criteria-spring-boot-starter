package io.github.stcarolas.criteria;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoDatabase;

import org.bson.codecs.configuration.CodecRegistry;
import org.immutables.criteria.backend.Backend;
import org.immutables.criteria.mongo.MongoBackend;
import org.immutables.criteria.mongo.MongoSetup;
import org.immutables.criteria.mongo.bson4jackson.BsonModule;
import org.immutables.criteria.mongo.bson4jackson.IdAnnotationModule;
import org.immutables.criteria.mongo.bson4jackson.JacksonCodecs;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(MongoProperties.class)
public class ImmutablesCriteriaAutoConfiguration {

    @Bean
    @Qualifier("immutablesCriteriaObjectMapper")
    @ConditionalOnMissingBean(name = "immutablesCriteriaObjectMapper")
    public ObjectMapper objectMapper(){
        return new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .registerModule(new BsonModule())
            .registerModule(new Jdk8Module())
            .registerModule(new IdAnnotationModule());
    }

    @Bean
    @ConditionalOnMissingBean(CodecRegistry.class)
    public CodecRegistry codecRegistry(
        @Qualifier("immutablesCriteriaObjectMapper") ObjectMapper mapper
    ) {
        return JacksonCodecs.registryFromMapper(mapper);
    }

    @Bean
    @ConditionalOnMissingBean(MongoDatabase.class)
    public MongoDatabase mongoClients(
        MongoProperties properties,
        CodecRegistry registry
    ) {
        return MongoClients
            .create(properties.getUri())
            .getDatabase(properties.getDatabase())
            .withCodecRegistry(registry);
    }

    @Bean
    @ConditionalOnMissingBean(Backend.class)
    public Backend mongoBackend(
        MongoDatabase mongo
    ) {
        return new MongoBackend(MongoSetup.of(mongo));
    }

}
