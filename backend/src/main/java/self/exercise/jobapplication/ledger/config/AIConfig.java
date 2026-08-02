package self.exercise.jobapplication.ledger.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AIConfig {

    @Bean
    public ChatClient ollamaClient(OllamaChatModel ollamaChatModel) {
        return ChatClient.builder(ollamaChatModel).build();
    }
}
