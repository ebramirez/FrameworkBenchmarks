package io.quarkus.benchmark.repository;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;

import io.quarkus.benchmark.model.World;

@ApplicationScoped
public class WorldRepository {

    @Inject
    SessionFactory sf;

    @Transactional
    public void createData() {
        try (StatelessSession statelessSession = sf.openStatelessSession()) {
            final ThreadLocalRandom random = ThreadLocalRandom.current();
            for (int i=1; i<=10000; i++) {
                final World world = new World();
                world.setId(i);
                world.setRandomNumber(1 + random.nextInt(10000));
                statelessSession.insert(world);
            }
        }
    }

    public World findSingleAndStateless(int id) {
        try (StatelessSession ss = sf.openStatelessSession()) {
            return singleStatelessWorldLoad(ss,id);
        }
    }

    public void updateAll(Collection<World> worlds) {
        try (Session s = sf.openSession()) {
            s.setJdbcBatchSize(worlds.size());
            s.setHibernateFlushMode(FlushMode.MANUAL);
            for (World w : worlds) {
                s.update(w);
            }
            s.flush();
            s.clear();
        }
    }

    public Collection<World> findReadonly(Set<Integer> ids) {
        try (StatelessSession s = sf.openStatelessSession()) {
            //The rules require individual load: we can't use the Hibernate feature which allows load by multiple IDs as one single operation
            return ids.stream().map(id -> singleStatelessWorldLoad(s,id)).collect(Collectors.toList());
        }
    }

    private static World singleStatelessWorldLoad(final StatelessSession ss, final Integer id) {
        return (World) ss.get(World.class, id);
    }

}
