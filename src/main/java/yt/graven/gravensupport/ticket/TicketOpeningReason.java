package yt.graven.gravensupport.ticket;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.emoji.Emoji;

import java.util.function.UnaryOperator;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
public enum TicketOpeningReason {
    USER_REPORT(Emoji.fromFormatted(":pencil:"), "Signaler un utilisateur", Reason.always("Signaler un utilisateur")),
    APPEAL_SANCTION(Emoji.fromFormatted(":no_entry:"), "Faire appel d'une sanction", Reason.always("Faire appel d'une sanction")),
    SUGGESTION(Emoji.fromFormatted(":bulb:"), "Faire une suggestion", ignored -> "Faire une suggestion"),
    BOT_PROBLEM(Emoji.fromFormatted(":robot:"), "Signaler un problème avec un bot", ignored -> "Signaler un problème avec un bot"),
    OTHER(Emoji.fromFormatted(":speech_left:"), "Autre", Reason.sameAsInput());

    private final Emoji emoji;
    private final String label;
    private final UnaryOperator<String> reasonProvider;

    private static class Reason {
        private static UnaryOperator<String> always(String reason) {
            return ignored -> reason;
        }

        private static UnaryOperator<String> sameAsInput() {
            return UnaryOperator.identity();
        }
    }
}
