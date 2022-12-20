package yt.graven.gravensupport.utils.messages;

import java.awt.*;
import java.time.Instant;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import org.springframework.stereotype.Component;

@Component
public class Embeds {

  public EmbedBuilder error(String message) {
    return new EmbedBuilder().setColor(Color.RED).setTitle("Erreur").setDescription(message);
  }

  public TicketMessage errorMessage(String message) {
    return TicketMessage.from(error(message)).actionRow().deletable().build();
  }

  public EmbedBuilder success(String message) {
    return new EmbedBuilder().setColor(Color.GREEN).setTitle("Succès").setDescription(message);
  }

  public TicketMessage successMessage(String message) {
    return TicketMessage.from(success(message)).actionRow().deletable().build();
  }

  public EmbedBuilder ticketAlreadyExists(boolean personal) {
    return error(
        personal
            ? "Vous avez déjà un ticket ouvert."
            : "Un ticket est déjà ouvert avec cet utilisateur.");
  }

  public TicketMessage ticketAlreadyExistsMessage(boolean personal) {
    return TicketMessage.from(ticketAlreadyExists(personal)).actionRow().deletable().build();
  }

  public TicketMessage ticketAlreadyExistsMessage(
      GuildMessageChannel ticketChannel, boolean personal) {
    return TicketMessage.from(ticketAlreadyExists(personal))
        .actionRow()
        .button()
        .withText("Aller au ticket")
        .withLink(
            String.format(
                "https://discord.com/channels/%s/%s",
                ticketChannel.getGuild().getId(), ticketChannel.getId()))
        .build()
        .deletable()
        .build();
  }

  public EmbedBuilder noTicketAttached() {
    return error("Impossible de trouver un ticket rattaché à ce salon");
  }

  public TicketMessage noTicketAttachedMessage() {
    return TicketMessage.from(noTicketAttached()).actionRow().deletable().build();
  }

  public EmbedBuilder proposeOpening(String sentEmote) {
    return new EmbedBuilder()
        .setAuthor("Message automatique")
        .setTitle("Ticket en cours d'ouverture !")
        .setDescription(
            """
                Votre demande d'ouverture de ticket a bien été prise en compte.
                Veuillez cependant confirmer que vous avez pris connaissance des règles de ceux-cis.
                """)
        .addField(
            "✉️ Règles des tickets :",
            """
                - Les messages que vous envoyez ne peuvent ni être édités, ni être supprimés.
                - Tous les messages envoyés sont enregistrés.
                - Les règles du discord `Graven - Développement` sont également applicables dans les tickets.
                - Les tickets ouverts sans justification seront sanctionnés.
                - Les tickets sont destinés à la modération. Les demandes d'aides sont susceptibles d'être sanctionnées.
                """,
            false)
        .addField(
            "❔ Utilisation :",
            String.format(
                """
                - Pour transmettre un message, vous avez juste à envoyer un message en privé avec ce bot.
                - Les réponses de la modération se feront par le biais de ces message privés.
                - Si votre message est transmis, la réaction %s sera ajoutée à vos messages
                """,
                sentEmote),
            false)
        .addField(
            "✨ Terminer l'ouverture du ticket",
            """
                Afin de terminer l'ouverture du ticket, merci de sélectionner ci-dessous la raison de l'ouverture de votre ticket.
                Vous serez ensuite recontacté dans les plus brefs délais.
                """,
            false)
        .setColor(Color.GREEN);
  }

  public EmbedBuilder forceOpening(String sentEmote) {
    return new EmbedBuilder()
        .setAuthor("Message automatique")
        .setTitle("Ticket ouvert !")
        .setDescription(
            """
                La modération a ouvert un ticket vous impliquant.
                Vous pouvez désormais discuter avec le staff en message privé avec le bot
                """)
        .addField(
            "✉️ Règles des tickets :",
            """
                - Les messages que vous envoyez ne peuvent ni être édités, ni être supprimés.
                - Tous les messages envoyés sont enregistrés.
                - Les règles du discord `Graven - Développement` sont également applicables dans les tickets.
                """,
            false)
        .addField(
            "❔ Utilisation :",
            String.format(
                """
                - Pour transmettre un message, vous avez juste à envoyer un message en privé avec ce bot.
                - Les réponses de la modération se feront par le biais de ces message privés.
                - Si votre message est transmis, la réaction %s sera ajoutée à vos messages
                """,
                sentEmote),
            false)
        .addField(
            "⚠️ Avertissement :",
            """
                Ce ticket a été ouvert par la modération. Il est donc sûrement lié à un comportement problématique.
                Veuillez en tenir compte dans les messages que vous adresserez tout au long de ce ticket.
                """,
            false)
        .setColor(Color.ORANGE);
  }

  public EmbedBuilder ticketOpening(
      boolean forced, User by, User from, TextChannel channel, String reason) {
    return new EmbedBuilder()
        .setTitle("Ticket ouvert")
        .setColor(forced ? Color.CYAN : Color.GREEN)
        .setTimestamp(Instant.now())
        .setThumbnail(from.getAvatarUrl())
        .addField(
            "ℹ️ Détails :",
            String.format(
                """
                    > **Identifiant de l'utilisateur**
                    %s

                    > **Nom de l'utilisateur**
                    %s (`@%s`)

                    :hash: **Salon**
                    %s (`#%s`)

                    %s
                    """,
                from.getId(),
                from.getAsMention(),
                from.getAsTag(),
                channel.getAsMention(),
                channel.getName(),
                forced
                    ? String.format(
                        """
                         🛂 **Ouvert par**
                         %s (`@%s`)

                        """,
                        by.getAsMention(), by.getAsTag())
                    : String.format(
                        """
                        📝 **Raison**
                        `%s`
                    """,
                        reason)),
            false);
  }

  public EmbedBuilder ticketClosing(User from, String jumpUrl) {
    return new EmbedBuilder()
        .setTitle("Ticket fermé")
        .setColor(Color.RED)
        .setTimestamp(Instant.now())
        .setThumbnail(from.getAvatarUrl())
        .addField(
            "ℹ️ Détails :",
            String.format(
                """
                    > **Identifiant de l'utilisateur**
                    %s

                    > **Nom de l'utilisateur**
                    %s (`@%s`)

                    :spiral_note_pad: **Rapport**
                    [Lien du rapport](%s)
                    """,
                from.getId(), from.getAsMention(), from.getAsTag(), jumpUrl),
            false);
  }
}
