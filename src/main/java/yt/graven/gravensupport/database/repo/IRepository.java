package yt.graven.gravensupport.database.repo;

import java.sql.SQLException;
import java.util.Optional;

public interface IRepository<ENTITY, ID> {

    void create() throws SQLException;

    void drop() throws SQLException;

    void insert(ENTITY entity) throws SQLException;

    void update(ENTITY entity) throws SQLException;

    ENTITY delete(ENTITY entity) throws SQLException;

    Optional<ENTITY> findById(ID id) throws SQLException;
}
