package io.quarkus.benchmark.repository;

import java.util.concurrent.ThreadLocalRandom;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.hibernate.FlushMode;
import org.hibernate.Session;

import io.quarkus.benchmark.model.World;

@ApplicationScoped
public class WorldRepository {

    @Inject
    EntityManager em;

    public World find(int id) {
        return em.find(World.class, id);
    }

    @Transactional
    public void update(World[] worlds) {
        for (World world : worlds) {
            em.merge(world);
        }
    }

    @Transactional
    public void createData() {
        Session s = em.unwrap( Session.class );
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        s.setHibernateFlushMode( FlushMode.MANUAL );
        for ( int i = 1; i <= 10000; i++ ) {
            final World world = new World();
            world.setId( i );
            world.setRandomNumber( 1 + random.nextInt( 10000 ) );
            s.persist( world );
            if ( 1 % 200 == 0 ) {
                s.flush();
            }
        }
        s.flush();
    }
}
