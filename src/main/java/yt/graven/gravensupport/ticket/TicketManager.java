package yt.graven.gravensupport.ticket;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import org.simpleyaml.configuration.file.YamlConfiguration;
import org.springframework.stereotype.Component;
import yt.graven.gravensupport.database.repo.TicketRepository;

@Component
@RequiredArgsConstructor
public class TicketManager {

    private final TicketRepository repository;
    private final JDA jda;
    private final YamlConfiguration configuration;



}
