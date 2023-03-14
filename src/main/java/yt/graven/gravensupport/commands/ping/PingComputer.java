package yt.graven.gravensupport.commands.ping;

import java.util.concurrent.CompletableFuture;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PingComputer {

    private final JDA client;

    @Getter
    private long restPing;

    @Getter
    private long gatewayPing;

    public CompletableFuture<Void> update() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        client.getRestPing().queue(ping -> {
            this.gatewayPing = client.getGatewayPing();
            this.restPing = ping;

            future.complete(null);
        });
        return future;
    }
}
