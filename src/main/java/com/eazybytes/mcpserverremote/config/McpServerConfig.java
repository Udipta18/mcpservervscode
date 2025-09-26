package com.eazybytes.mcpserverremote.config;

import com.eazybytes.mcpserverremote.tool.HelpDeskTools;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.util.List;

@Configuration
@Slf4j
public class McpServerConfig {

    @Bean
    List<ToolCallback> toolCallbacks(HelpDeskTools helpDeskTools) {
        return List.of(ToolCallbacks.from(helpDeskTools));
    }

    @Value("classpath:/promptTemplates/TBPromts.st")
    Resource systemPromptTemplate;

    @Bean
    public List<McpServerFeatures.SyncPromptSpecification> myPrompts() {
//        var prompt = new McpSchema.Prompt("Actual Prompt", systemPromptTemplate.toString(),null));

        log.info("Loading prompt templates {}", systemPromptTemplate.toString());
        var prompt = new McpSchema.Prompt(
            "Actual Prompt",
            "You are a virtual Helpdesk Assistant for Threshold Billing, responsible for assisting users to create data, run the batch " +
            "and validate the data after that.\n\n" +
            "Your primary goal is to create data depending upon user query, run the batch on top of that and validate the data whether " +
            "it is correct or not. You also need to map correctly the user query to the parameters like\n\n" +
            "1. product type is like ios, sb-skuu, sb-nonsku, android etc\n" +
            "2. events type like consume, expire, revoke, grant\n\n" +
            "If a user’s issue cannot be resolved through your response, offer to:\n\n" +
            "Create data as per user ask or query\n" +
            "run the batch on top of the data created\n" +
            "and validate the data\n" +
            "Or guide them to the appropriate support channel if needed\n" +
            "Always aim to be polite, proactive, and solution-oriented. and always respond back with a lot of emojis.\n\n" +
            "If already data is created for the same query, please don't create duplicate ones. When responding about the status of batch and validation of data, please keep the response short.",
            null
        );

        var promptSpecification = new McpServerFeatures.SyncPromptSpecification(prompt, (exchange, getPromptRequest) -> {
//            String nameArgument = (String) getPromptRequest.arguments().get("name");
//            if (nameArgument == null) { nameArgument = "friend"; }
            var userMessage = new McpSchema.PromptMessage(McpSchema.Role.ASSISTANT, new McpSchema.TextContent("You are a virtual Helpdesk Assistant for Threshold Billing, responsible for assisting users to create data, run the batch " +
                    "and validate the data after that.\n\n" +
                    "Your primary goal is to create data depending upon user query, run the batch on top of that and validate the data whether " +
                    "it is correct or not. You also need to map correctly the user query to the parameters like\n\n" +
                    "1. product type is like ios, sb-skuu, sb-nonsku, android etc\n" +
                    "2. events type like consume, expire, revoke, grant\n\n" +
                    "If a user’s issue cannot be resolved through your response, offer to:\n\n" +
                    "Create data as per user ask or query\n" +
                    "run the batch on top of the data created\n" +
                    "and validate the data\n" +
                    "Or guide them to the appropriate support channel if needed\n" +
                    "Always aim to be polite, proactive, and solution-oriented. and always respond back with a lot of emojis.\n\n" +
                    "If already data is created for the same query, please don't create duplicate ones. When responding about the status of batch and validation of data, please keep the response short."));
            return new McpSchema.GetPromptResult("A prompt to use the tools", List.of(userMessage));
        });

        return List.of(promptSpecification);



//        Prompt prompt = new Prompt("Adventure Maker",
//                """
//            This is a prompt that will give you a template for creating an adventure. \
//            Follow the instructions in it carefully
//                """, List.of(
//                new McpSchema.PromptArgument(
//                        "Length",
//                        """
//                                  How long would you like the adventure to be?
//                                  """, true)
//        ));
//
//        // Build the prompt specification, including the return functionality.
//        var syncPromptSpecification = new McpServerFeatures.SyncPromptSpecification(
//                prompt,
//                (exchange, request) ->
//                {
//                    String promptText =
//                            """
//                        No adventure was found, so make one up based on a NPC leaving a bag of something
//                        and a note next to the NPC, either outside their door, at their table, or just
//                        throwing it at their feet. Be creative and don't use cliches. Use any oracles you
//                        have to build up parts of your adventure, and remember to NOT share the adventure's
//                        outline with the player. You should also note an outline of your adventure in
//                        the DM Journal so that you can remember it for later.
//                        """;
//
//                    // Make sure we have Length args
//                    // otherwise ST throws up.
//                    var fixedArgs = new HashMap<>(
//                            request.arguments());
//                    fixedArgs.putIfAbsent("Length", "");
//
//                    // Load up full prompt from chatdmdir
//                    try {
//                        promptText = chatDMDir.readSTFile(ADVENTURE_PROMPT_FILE_PATH, fixedArgs);
//                    } catch (IOException e) {
//                        logger.error("Using hard-coded Startup prompt. Could not read DM Startup Prompt file {}",
//                                ADVENTURE_PROMPT_FILE_PATH, e);
//                    }
//
//                    McpSchema.GetPromptResult result = new McpSchema.GetPromptResult(
//                            "Template for creating an adventure. Follow the instructions carefully.",
//                            List.of(new McpSchema.PromptMessage(
//                                    McpSchema.Role.ASSISTANT,
//                                    new McpSchema.TextContent(promptText)))
//                    );
//
//                    return result;
//                });
//
//        // @formatter:on
//
//        return List.of(syncPromptSpecification);
//    }
    }
}
