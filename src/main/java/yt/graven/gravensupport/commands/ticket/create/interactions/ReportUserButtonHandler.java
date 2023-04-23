package yt.graven.gravensupport.commands.ticket.create.interactions;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.springframework.stereotype.Component;
import yt.graven.gravensupport.utils.exceptions.TicketException;
import yt.graven.gravensupport.utils.interactions.InteractionAction;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class ReportUserButtonHandler implements InteractionAction<ButtonInteractionEvent> {

    @Override
    public void run(ButtonInteractionEvent event) throws TicketException, IOException {

        Modal modal = Modal.create("op-report-user", "Signaler un utilisateur")
                .addActionRow(
                        TextInput.create("user-id", "ID de l'utilisateur", TextInputStyle.SHORT)
                                .setPlaceholder("Identifiant de l'utilisateur")
                                .setRequiredRange(15, 20)
                                .setRequired(true)
                                .build()
                )
                .addActionRow(
                        TextInput.create("reason", "Raison", TextInputStyle.PARAGRAPH)
                                .setPlaceholder("""
                                        Décrivez en quelques mots la raison de votre signalement.
                                        """)
                                .build()
                )
                .build();

        event.replyModal(modal).queue();

    }
}
