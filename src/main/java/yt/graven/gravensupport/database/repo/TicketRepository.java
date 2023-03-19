package yt.graven.gravensupport.database.repo;

import java.sql.*;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import yt.graven.gravensupport.ticket.RawTicket;
import yt.graven.gravensupport.ticket.TicketState;

@Repository
@RequiredArgsConstructor

@Slf4j
public class TicketRepository implements IRepository<RawTicket, Long> {

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
                            opened_by BIGINT NOT NULL,
                            opening_reason VARCHAR(255) NOT NULL
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
    public void insert(RawTicket rawTicket) throws SQLException {
        log.info("Inserting ticket with id {} in Tickets ({})", rawTicket.getId(), rawTicket);
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
                            opened_by,
                            opening_reason
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
                        """);

        insertStmt.setLong(1, rawTicket.getUserId());
        insertStmt.setString(2, rawTicket.getUsername());
        insertStmt.setLong(3, rawTicket.getChannelId());
        insertStmt.setLong(4, rawTicket.getOpeningMessageId());
        insertStmt.setLong(5, rawTicket.getClosingMessageId());
        insertStmt.setString(6, rawTicket.getStatus().name());
        insertStmt.setTimestamp(7, Timestamp.from(rawTicket.getCreatedAt()));
        insertStmt.setTimestamp(8, Timestamp.from(rawTicket.getClosedAt()));
        insertStmt.setLong(9, rawTicket.getOpenedBy());
        insertStmt.setString(10, rawTicket.getOpeningReason());

        insertStmt.execute();
        insertStmt.close();
    }

    @Override
    public void update(RawTicket rawTicket) throws SQLException {
        log.info("Updating ticket with id {} in Tickets ({})", rawTicket.getId(), rawTicket);
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
                            opened_by = ?,
                            opening_reason = ?
                        WHERE id = ?;
                        """);

        updateStmt.setLong(1, rawTicket.getUserId());
        updateStmt.setString(2, rawTicket.getUsername());
        updateStmt.setLong(3, rawTicket.getChannelId());
        updateStmt.setLong(4, rawTicket.getOpeningMessageId());
        updateStmt.setLong(5, rawTicket.getClosingMessageId());
        updateStmt.setString(6, rawTicket.getStatus().name());
        updateStmt.setTimestamp(7, Timestamp.from(rawTicket.getCreatedAt()));
        updateStmt.setTimestamp(8, Timestamp.from(rawTicket.getClosedAt()));
        updateStmt.setLong(9, rawTicket.getOpenedBy());
        updateStmt.setLong(10, rawTicket.getId());
        updateStmt.setString(11, rawTicket.getOpeningReason());

        updateStmt.execute();
        updateStmt.close();
    }

    @Override
    public RawTicket delete(RawTicket rawTicket) throws SQLException {
        log.info("Deleting ticket with id {} in Tickets ({})", rawTicket.getId(), rawTicket);
        PreparedStatement deleteStmt = connection.prepareStatement("DELETE FROM Tickets WHERE id = ?;");
        deleteStmt.setLong(1, rawTicket.getId());

        deleteStmt.execute();
        deleteStmt.close();

        return rawTicket;
    }

    @Override
    public Optional<RawTicket> findById(Long id) throws SQLException {
        log.info("Retrieving ticket with id {} in Tickets", id);
        PreparedStatement findStmt = connection.prepareStatement("SELECT * FROM Tickets WHERE id = ?;");
        findStmt.setLong(1, id);

        ResultSet rs = findStmt.executeQuery();
        RawTicket rawTicket = null;

        if (rs.next()) {
            rawTicket = RawTicket.builder()
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
                    .openingReason(rs.getString("opening_reason"))
                    .build();
        }

        rs.close();
        findStmt.close();

        return Optional.ofNullable(rawTicket);
    }
}
