package yt.graven.gravensupport.configuration;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import org.simpleyaml.configuration.file.YamlConfiguration;
import org.springframework.context.annotation.Bean;
import yt.graven.gravensupport.configuration.annotations.TicketGuild;

@Slf4j
public class TicketCommonsConfig {

    @Bean
    Guild ticketGuild(JDA jda, YamlConfiguration botConfiguration) {
        String guildId = botConfiguration.getString("config.ticket_guild.guild_id");
        Guild guild = jda.getGuildById(guildId);

        if (guild == null) {
            throw new IllegalStateException("Unable to find guild with id " + guildId);
        }

        return guild;
    }

    @Bean
    Emoji sentReaction(@TicketGuild Guild ticketGuild, YamlConfiguration botConfiguration) {
        String reactionId = botConfiguration.getString("config.ticket_guild.reaction_id");
        Emoji emoji = ticketGuild.retrieveEmojiById(reactionId).complete();

        if (emoji == null) {
            throw new IllegalStateException("Unable to find emoji with id " + reactionId);
        }

        return emoji;
    }

    @Bean
    Category ticketCategory(@TicketGuild Guild ticketGuild, YamlConfiguration botConfiguration) {
        String categoryId = botConfiguration.getString("config.ticket_guild.tickets_category");
        Category category = ticketGuild.getCategoryById(categoryId);

        if (category == null) {
            throw new IllegalStateException("Unable to find category with id " + categoryId);
        }

        return category;
    }

    @Bean
    TextChannel reportsChannel(@TicketGuild Guild ticketGuild, YamlConfiguration botConfiguration) {
        String reportsChannelId = botConfiguration.getString("config.ticket_guild.channels_ids.reports");
        TextChannel channel = ticketGuild.getTextChannelById(reportsChannelId);

        if (channel == null) {
            throw new IllegalStateException("Unable to find channel with id " + reportsChannelId);
        }

        return channel;
    }

    @Bean
    TextChannel ticketsChannel(@TicketGuild Guild ticketGuild, YamlConfiguration botConfiguration) {
        String ticketsChannelId = botConfiguration.getString("config.ticket_guild.channels_ids.tickets");
        TextChannel channel = ticketGuild.getTextChannelById(ticketsChannelId);

        if (channel == null) {
            throw new IllegalStateException("Unable to find channel with id " + ticketsChannelId);
        }

        return channel;
    }

    @Bean
    TextChannel attachementsChannels(@TicketGuild Guild ticketGuild, YamlConfiguration botConfiguration) {
        String attachementsChannelId = botConfiguration.getString("config.ticket_guild.channels_ids.attachements");
        TextChannel channel = ticketGuild.getTextChannelById(attachementsChannelId);

        if (channel == null) {
            throw new IllegalStateException("Unable to find channel with id " + attachementsChannelId);
        }

        return channel;
    }
}
