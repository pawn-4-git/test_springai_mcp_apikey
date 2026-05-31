package com.example.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springaicommunity.mcp.security.server.apikey.memory.ApiKeyEntityImpl;
import org.springaicommunity.mcp.security.server.apikey.ApiKeyEntityRepository;
import org.springaicommunity.mcp.security.server.apikey.memory.InMemoryApiKeyEntityRepository;

import java.util.List;

import static org.springaicommunity.mcp.security.server.config.McpApiKeyConfigurer.mcpServerApiKey;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class McpServerConfiguration {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(authz -> authz
                        .anyRequest().authenticated()
                )
                .csrf(csrf -> csrf.disable()) // disable CSRF for API endpoints
                .with(
                        mcpServerApiKey(),
                        (apiKey) -> {
                            // REQUIRED: the repo for API keys
                            apiKey.apiKeyRepository(apiKeyRepository());
                        }
                )
                .build();
    }

    /**
     * Provide a repository of {@link org.springaicommunity.mcp.server.security.apikey.ApiKeyEntity}.
     */
    @Bean
    public ApiKeyEntityRepository<ApiKeyEntityImpl> apiKeyRepository() {
        var apiKey = ApiKeyEntityImpl.builder()
                .name("test api key")
                .id("api01")
                .secret("mycustomapikey")
                .build();
        return new InMemoryApiKeyEntityRepository<>(List.of(apiKey));
    }

    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    interface McpRecordMixIn {}

    @Bean
    @org.springframework.context.annotation.Primary
    public com.fasterxml.jackson.databind.ObjectMapper mcpObjectMapper() {
        var objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.addMixIn(io.modelcontextprotocol.spec.McpSchema.InitializeRequest.class, McpRecordMixIn.class);
        objectMapper.addMixIn(io.modelcontextprotocol.spec.McpSchema.ClientCapabilities.class, McpRecordMixIn.class);
        objectMapper.addMixIn(io.modelcontextprotocol.spec.McpSchema.ClientCapabilities.Elicitation.class, McpRecordMixIn.class);
        objectMapper.addMixIn(io.modelcontextprotocol.spec.McpSchema.ClientCapabilities.RootCapabilities.class, McpRecordMixIn.class);
        objectMapper.addMixIn(io.modelcontextprotocol.spec.McpSchema.ClientCapabilities.Sampling.class, McpRecordMixIn.class);
        return objectMapper;
    }

    @Bean
    public org.springframework.ai.tool.ToolCallbackProvider myToolsCallbackProvider(MyToolsService myToolsService) {
        return org.springframework.ai.tool.method.MethodToolCallbackProvider.builder()
                .toolObjects(myToolsService)
                .build();
    }

}
