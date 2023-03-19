package yt.graven.gravensupport.database;

import java.sql.SQLException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import yt.graven.gravensupport.database.repo.IRepository;
import yt.graven.gravensupport.database.repo.Repository;

@Component
@RequiredArgsConstructor
public class DatabaseManager {

    private final ApplicationContext ctx;

    public void createAll() {
        ctx.getBeansWithAnnotation(Repository.class).forEach(this::create);
    }

    private void create(String name, Object instance) {
        if (!(instance instanceof IRepository repo)) {
            return;
        }

        try {
            repo.create();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
