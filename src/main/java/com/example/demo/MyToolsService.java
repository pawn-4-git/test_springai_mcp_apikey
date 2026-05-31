package com.example.demo;

import org.springframework.stereotype.Service;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

@Service
public class MyToolsService {

    @Tool(name = "greeter", description = "A tool that greets the user by name, in the selected language")
    @PreAuthorize("isAuthenticated()")
    public String greet(
            @ToolParam(description = "The language for the greeting (example: english, french, ...)") String language
    ) {
        if (!StringUtils.hasText(language)) {
            language = "english";
        }

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var name = (authentication != null && authentication.getName() != null) ? authentication.getName() : "Guest";

        return switch (language.toLowerCase()) {
            case "english" -> "Hello, %s!".formatted(name);
            case "french" -> "Salut %s!".formatted(name);
            default -> ("I don't understand language \"%s\". " +
                    "So I'm just going to say Hello %s!").formatted(language, name);
        };
    }
}
