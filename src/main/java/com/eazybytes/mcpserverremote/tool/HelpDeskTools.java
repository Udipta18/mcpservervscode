package com.eazybytes.mcpserverremote.tool;

import com.eazybytes.mcpserverremote.entity.HelpDeskTicket;
import com.eazybytes.mcpserverremote.model.TbDetails;
import com.eazybytes.mcpserverremote.model.TicketRequest;
import com.eazybytes.mcpserverremote.service.HelpDeskTicketService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class HelpDeskTools {

    private static final Logger LOGGER = LoggerFactory.getLogger(HelpDeskTools.class);

    private final HelpDeskTicketService service;

    @Tool(name = "createTicket", description = "Create the Support Ticket")
    String createTicket(@ToolParam(description = "Details to create a Support ticket")
    TicketRequest ticketRequest) {
        LOGGER.info("Creating support ticket for user: {} with details: {}", ticketRequest);
        HelpDeskTicket savedTicket = service.createTicket(ticketRequest);
        LOGGER.info("Ticket created successfully. Ticket ID: {}, Username: {}", savedTicket.getId(), savedTicket.getUsername());
        return "Ticket #" + savedTicket.getId() + " created successfully for user " + savedTicket.getUsername();
    }

    @Tool(name="getTicketStatus", description = "Fetch the status of the tickets based on a given username")
    List<HelpDeskTicket> getTicketStatus(@ToolParam(description =
            "Username to fetch the status of the help desk tickets") String username) {
        LOGGER.info("Fetching tickets for user: {}", username);
        List<HelpDeskTicket> tickets =  service.getTicketsByUsername(username);
        LOGGER.info("Found {} tickets for user: {}", tickets.size(), username);
        return tickets;
    }


    @Tool(name = "createTbData", description = "Create data for run the batches on top of that", returnDirect = true)
    String createTicket(@ToolParam(description = "Details to create a Support ticket")
                        TbDetails ticketRequest, ToolContext toolContext) {
        String username = (String) toolContext.getContext().get("username");
        LOGGER.info("Creating support ticket for user: {} with details: {}", username, ticketRequest);
//        HelpDeskTicket savedTicket = service.createTicket(ticketRequest,username);
//        LOGGER.info("Ticket created successfully. Ticket ID: {}, Username: {}", savedTicket.getId(), savedTicket.getUsername());
        return "Data Inserted Successfully";
    }

    @Tool(name= "runTheBatches", description = "Run the batches on the TB data")
    String runTheBatches(ToolContext toolContext) {
        String username = (String) toolContext.getContext().get("username");
        LOGGER.info("Run The Batches: {}", username);
//        List<HelpDeskTicket> tickets =  service.getTicketsByUsername(username);
//        LOGGER.info("Found {} tickets for user: {}", tickets.size(), username);
//        // throw new RuntimeException("Unable to fetch ticket status");
        return  "Batch Run Successfully";
    }


    @Tool(name= "validateData", description = "validate the data after batch run")
    String validateData(ToolContext toolContext) {
        String username = (String) toolContext.getContext().get("username");
        LOGGER.info("Validate The data: {}", username);
//        List<HelpDeskTicket> tickets =  service.getTicketsByUsername(username);
//        LOGGER.info("Found {} tickets for user: {}", tickets.size(), username);
//        // throw new RuntimeException("Unable to fetch ticket status");
        return  "Validate data Successfully";
    }

}
