package yt.graven.gravensupport.database.repo;

import java.sql.*;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import yt.graven.gravensupport.ticket.TicketState;
import yt.graven.gravensupport.ticket.Ticket;

@Repository
@RequiredArgsConstructor

@Slf4j
public class TicketRepository implements IRepository<Ticket, Long> {

    private final Connection connection;

    @Override
    public void create() throws SQLException {
        log.info("[SQL] Creating table Tickets");
        PreparedStatement createStmt = connection.prepareStatement(
                """
                        CREATE TABLE IF NOT EXISTS Tickets(
                            id SERIAL PRIMARY KEY,
                            user_id BIGINT NOT NULL,
                            username VARCHAR(255) NOT NULL,
                            channel_id BIGINT NOT NULL,
                            opening_message_id BIGINT,
                            closing_message_id BIGINT,
                            status VARCHAR(255) NOT NULL,
                            created_at TIMESTAMP NOT NULL,
                            closed_at TIMESTAMP,
                            opened_by BIGINT NOT NULL
                        );
                        """);

        createStmt.execute();
        createStmt.close();
    }

    @Override
    public void drop() throws SQLException {
        log.info("[SQL] Dropping table Tickets");
        PreparedStatement dropStmt = connection.prepareStatement("DROP TABLE IF EXISTS Tickets;");

        dropStmt.execute();
        dropStmt.close();
    }

    @Override
    public void insert(Ticket ticket) throws SQLException {
        log.info("Inserting ticket with id {} in Tickets ({})", ticket.getId(), ticket);
        PreparedStatement insertStmt = connection.prepareStatement(
                """
                        INSERT INTO Tickets(
                            user_id,
                            username,
                            channel_id,
                            opening_message_id,
                            closing_message_id,
                            status,
                            created_at,
                            closed_at,
                            opened_by
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);
                        """);

        insertStmt.setLong(1, ticket.getUserId());
        insertStmt.setString(2, ticket.getUsername());
        insertStmt.setLong(3, ticket.getChannelId());
        insertStmt.setLong(4, ticket.getOpeningMessageId());
        insertStmt.setLong(5, ticket.getClosingMessageId());
        insertStmt.setString(6, ticket.getStatus().name());
        insertStmt.setTimestamp(7, Timestamp.from(ticket.getCreatedAt()));
        insertStmt.setTimestamp(8, Timestamp.from(ticket.getClosedAt()));
        insertStmt.setLong(9, ticket.getOpenedBy());

        insertStmt.execute();
        insertStmt.close();
    }

    @Override
    public void update(Ticket ticket) throws SQLException {
        log.info("Updating ticket with id {} in Tickets ({})", ticket.getId(), ticket);
        PreparedStatement updateStmt = connection.prepareStatement(
                """
                        UPDATE Tickets SET
                            user_id = ?,
                            username = ?,
                            channel_id = ?,
                            opening_message_id = ?,
                            closing_message_id = ?,
                            status = ?,
                            created_at = ?,
                            closed_at = ?,
                            opened_by = ?
                        WHERE id = ?;
                        """);

        updateStmt.setLong(1, ticket.getUserId());
        updateStmt.setString(2, ticket.getUsername());
        updateStmt.setLong(3, ticket.getChannelId());
        updateStmt.setLong(4, ticket.getOpeningMessageId());
        updateStmt.setLong(5, ticket.getClosingMessageId());
        updateStmt.setString(6, ticket.getStatus().name());
        updateStmt.setTimestamp(7, Timestamp.from(ticket.getCreatedAt()));
        updateStmt.setTimestamp(8, Timestamp.from(ticket.getClosedAt()));
        updateStmt.setLong(9, ticket.getOpenedBy());
        updateStmt.setLong(10, ticket.getId());

        updateStmt.execute();
        updateStmt.close();
    }

    @Override
    public Ticket delete(Ticket ticket) throws SQLException {
        log.info("Deleting ticket with id {} in Tickets ({})", ticket.getId(), ticket);
        PreparedStatement deleteStmt = connection.prepareStatement("DELETE FROM Tickets WHERE id = ?;");
        deleteStmt.setLong(1, ticket.getId());

        deleteStmt.execute();
        deleteStmt.close();

        return ticket;
    }

    @Override
    public Optional<Ticket> findById(Long id) throws SQLException {
        log.info("Retrieving ticket with id {} in Tickets", id);
        PreparedStatement findStmt = connection.prepareStatement("SELECT * FROM Tickets WHERE id = ?;");
        findStmt.setLong(1, id);

        ResultSet rs = findStmt.executeQuery();
        Ticket ticket = null;

        if (rs.next()) {
            ticket = Ticket.builder()
                    .id(rs.getLong("id"))
                    .userId(rs.getLong("user_id"))
                    .username(rs.getString("username"))
                    .channelId(rs.getLong("channel_id"))
                    .openingMessageId(rs.getLong("opening_message_id"))
                    .closingMessageId(rs.getLong("closing_message_id"))
                    .status(TicketState.valueOf(rs.getString("status")))
                    .createdAt(rs.getTimestamp("created_at").toInstant())
                    .closedAt(rs.getTimestamp("closed_at").toInstant())
                    .openedBy(rs.getLong("opened_by"))
                    .build();
        }

        rs.close();
        findStmt.close();

        return Optional.ofNullable(ticket);
    }
}
