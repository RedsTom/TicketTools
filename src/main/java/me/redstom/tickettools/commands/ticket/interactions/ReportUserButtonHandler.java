package me.redstom.tickettools.commands.ticket.interactions;

import java.io.IOException;
import lombok.RequiredArgsConstructor;
import me.redstom.tickettools.utils.exceptions.TicketException;
import me.redstom.tickettools.utils.interactions.InteractionAction;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReportUserButtonHandler implements InteractionAction<ButtonInteractionEvent> {

    @Override
    public void run(ButtonInteractionEvent event) throws TicketException, IOException {
        // spotless:off
        Modal modal = Modal.create("op-report-user", "Signaler un utilisateur")
                .addActionRow(TextInput.create("user-id", "ID de l'utilisateur", TextInputStyle.SHORT)
                        .setPlaceholder("Identifiant de l'utilisateur")
                        .setRequiredRange(15, 20)
                        .setRequired(true)
                        .build()
                )
                .addActionRow(TextInput.create("reason", "Raison", TextInputStyle.PARAGRAPH)
                        .setPlaceholder("""
                                Décrivez en quelques mots la raison de votre signalement.
                                """)
                        .build()
                )
                .build();
        // spotless:on

        event.replyModal(modal).queue();
    }
}
