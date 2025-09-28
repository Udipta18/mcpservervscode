package com.udipta.mcpvscode.config;

import com.udipta.mcpvscode.tool.BatchProcessingTools;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import java.util.List;

/**
 * Configuration class for MCP Server setup.
 * This class configures tool callbacks and prompt specifications for the MCP
 * server.
 * 
 * @author UDIPTA
 * @version 1.0
 * @since 1.0
 */
@Configuration
@Slf4j
public class McpServerConfig {

    /**
     * System prompt template resource.
     */
    @Value("classpath:/promptTemplates/batchProcessingExample.st")
    private Resource systemPromptTemplate;

    /**
     * Creates tool callbacks for batch processing tools.
     * 
     * @param batchProcessingTools the batch processing tools bean
     * @return list of tool callbacks
     */
    @Bean
    public List<ToolCallback> toolCallbacks(BatchProcessingTools batchProcessingTools) {
        return List.of(ToolCallbacks.from(batchProcessingTools));
    }

    /**
     * Creates prompt specifications for the MCP server.
     * 
     * @return list of prompt specifications
     * @throws PromptTemplateException if prompt template cannot be loaded
     */
    @Bean
    public List<McpServerFeatures.SyncPromptSpecification> myPrompts() {
        try {
            log.info("Loading prompt templates from: {}", systemPromptTemplate);

            final String promptContent = loadPromptTemplate();
            final McpSchema.Prompt prompt = new McpSchema.Prompt(
                    "Batch Processing Assistant",
                    promptContent,
                    null);

            final McpServerFeatures.SyncPromptSpecification promptSpecification = new McpServerFeatures.SyncPromptSpecification(
                    prompt, (exchange, getPromptRequest) -> {
                        final McpSchema.PromptMessage userMessage = new McpSchema.PromptMessage(
                                McpSchema.Role.ASSISTANT,
                                new McpSchema.TextContent(promptContent));
                        return new McpSchema.GetPromptResult(
                                "Batch processing assistant prompt for data creation, processing, and validation",
                                List.of(userMessage));
                    });

            log.info("Successfully loaded prompt template with {} characters", promptContent.length());
            return List.of(promptSpecification);

        } catch (Exception e) {
            log.error("Failed to load prompt template", e);
            throw new RuntimeException("Failed to initialize prompt specifications", e);
        }
    }

    private String loadPromptTemplate() {
        try {
            log.debug("Loading prompt from configured resource: {}", systemPromptTemplate);
            return StreamUtils.copyToString(systemPromptTemplate.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Failed to load prompt template from resource: {}", systemPromptTemplate, e);
            throw new RuntimeException("Unable to load prompt template", e);
        }
    }
}