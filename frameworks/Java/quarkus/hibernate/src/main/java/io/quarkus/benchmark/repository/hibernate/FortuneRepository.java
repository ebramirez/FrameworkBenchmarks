package io.quarkus.benchmark.repository.hibernate;

import io.quarkus.benchmark.model.hibernate.Fortune;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;

@ApplicationScoped
public class FortuneRepository {

    @Inject
    SessionFactory sf;

    public List<Fortune> findAllStateless() {
        try (StatelessSession s = sf.openStatelessSession()) {
            return s.createQuery("FROM Fortune", Fortune.class).list();
        }
    }
}
