package yt.graven.gravensupport.commands.ticket;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;

public sealed interface TicketOpeningReason {

    String reason();

    record Simple(String reason) implements TicketOpeningReason {}

    record UserReport(String userId, String reportReason) implements TicketOpeningReason {

        public User user(JDA jda) {
            try {
                return jda.retrieveUserById(userId).complete();
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        public String reason() {
            return "Signaler un utilisateur";
        }
    }

    record Empty() implements TicketOpeningReason {
        @Override
        public String reason() {
            return "";
        }
    }
}
