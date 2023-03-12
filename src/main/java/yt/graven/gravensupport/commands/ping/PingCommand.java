package yt.graven.gravensupport.commands.ping;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import yt.graven.gravensupport.utils.commands.Command;
import yt.graven.gravensupport.utils.commands.ICommand;
import yt.graven.gravensupport.utils.messages.Embeds;
import yt.graven.gravensupport.utils.messages.TMessage;

@Command
@RequiredArgsConstructor
public class PingCommand implements ICommand {
    private final PingManager pingManager;

    private final Embeds embeds;

    @Override
    public String getName() {
        return "ping";
    }

    @Override
    public SlashCommandData getSlashCommandData() {
        return Commands.slash("ping", "Calcule la latence du bot")
                .setDefaultPermissions(DefaultMemberPermissions.ENABLED);
    }

    @Override
    public void run(SlashCommandInteractionEvent event) {
        MessageEmbed embed = pingManager.compute();

        TMessage.from(embed)
                .actionRow()
                .add(Button.of(ButtonStyle.PRIMARY, "refresh-ping", "Actualiser", Emoji.fromUnicode("🔁")))
                .build()
                .reply(event)
                .queue();
    }
}
