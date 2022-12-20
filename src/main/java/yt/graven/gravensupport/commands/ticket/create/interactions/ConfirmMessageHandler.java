package yt.graven.gravensupport.commands.ticket.create.interactions;

import java.awt.*;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.utils.MiscUtil;
import org.springframework.stereotype.Component;
import yt.graven.gravensupport.commands.ticket.Ticket;
import yt.graven.gravensupport.commands.ticket.TicketManager;
import yt.graven.gravensupport.utils.interactions.IIInteractionAction;
import yt.graven.gravensupport.utils.messages.Embeds;

@Component
@RequiredArgsConstructor
public class ConfirmMessageHandler implements IIInteractionAction<ButtonInteractionEvent> {

  private final TicketManager ticketManager;
  private final Embeds embeds;

  @Override
  public void run(ButtonInteractionEvent event) {
    Message embedMessage = event.getMessage();

    MessageEmbed baseEmbed = embedMessage.getEmbeds().get(0);
    String originalMessageId = baseEmbed.getFields().get(0).getValue();
    originalMessageId =
        originalMessageId
            .replace(originalMessageId.replaceAll("\\[[0-9]+]", ""), "")
            .replaceAll("[\\[\\]]", "");

    Message referingMessage =
        event
            .getChannel()
            .getHistoryAround(originalMessageId, 50)
            .complete()
            .getMessageById(originalMessageId);

    if (referingMessage == null) {
      event
          .deferReply(true)
          .addEmbeds(
              new EmbedBuilder()
                  .setColor(Color.RED)
                  .setTitle("Erreur")
                  .setDescription("Impossible de trouver le message original !")
                  .build())
          .queue();
      return;
    }

    Optional<Ticket> ticket =
        ticketManager.get(MiscUtil.parseLong(((TextChannel) event.getChannel()).getTopic()));
    if (ticket.isEmpty()) {
      event
          .deferReply(true)
          .addEmbeds(
              new EmbedBuilder()
                  .setColor(Color.RED)
                  .setTitle("Erreur")
                  .setDescription("Impossible de trouver le ticket associé à ce salon !")
                  .setFooter("")
                  .build())
          .queue();
      return;
    }

    boolean attachements = referingMessage.getAttachments().size() != 0;

    InteractionHook interaction = null;
    if (attachements) {
      interaction = event.deferReply().complete();
    }

    InteractionHook fInteraction = interaction;
    ticket
        .get()
        .confirmSendToUser(referingMessage)
        .thenAccept(
            (message) -> {
              EmbedBuilder embed =
                  new EmbedBuilder(baseEmbed)
                      .setTitle("Message transmis :")
                      .setDescription(message.getContentRaw())
                      .setFooter("")
                      .setTimestamp(Instant.now())
                      .setColor(Color.GREEN);

              embed
                  .getFields()
                  .add(
                      new MessageEmbed.Field(
                          "🔗 Identifiant du message envoyé", message.getId(), true));

              if (attachements) {
                fInteraction.deleteOriginal().queue();
                embedMessage
                    .editMessageEmbeds(embed.build())
                    .setActionRow(
                        Button.of(
                            ButtonStyle.SUCCESS,
                            "edit-message",
                            "Modifier le message",
                            Emoji.fromUnicode("✏️")),
                        Button.of(
                            ButtonStyle.DANGER,
                            "delete-message",
                            "Supprimer le message",
                            Emoji.fromUnicode("🗑️")))
                    .queue();
              } else {
                event
                    .deferEdit()
                    .setEmbeds(embed.build())
                    .setActionRow(
                        Button.of(
                            ButtonStyle.SUCCESS,
                            "edit-message",
                            "Modifier le message",
                            Emoji.fromUnicode("✏️")),
                        Button.of(
                            ButtonStyle.DANGER,
                            "delete-message",
                            "Supprimer le message",
                            Emoji.fromUnicode("🗑️")))
                    .queue();
              }
            })
        .exceptionally(
            (error) -> {
              fInteraction.editOriginal(embeds.errorMessage(error.getMessage()).build()).queue();
              return null;
            });
  }
}
