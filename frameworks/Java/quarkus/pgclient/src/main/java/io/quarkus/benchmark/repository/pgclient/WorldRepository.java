package io.quarkus.benchmark.repository.pgclient;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.benchmark.model.World;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.vertx.reactivex.pgclient.PgPool;
import io.vertx.reactivex.sqlclient.RowIterator;
import io.vertx.reactivex.sqlclient.RowSet;
import io.vertx.reactivex.sqlclient.Tuple;

@ApplicationScoped
public class WorldRepository {

    private static Logger LOG = LoggerFactory.getLogger(WorldRepository.class);

    @Inject
    PgPool client;

    public Maybe<World> find(int id) {
        return client.rxPreparedQuery("SELECT id, randomnumber FROM world WHERE id = $1", Tuple.of(id))
                .map(RowSet::iterator)
                .filter(RowIterator::hasNext)
                .map(RowIterator::next)
                .map(row -> new World(row.getInteger(0), row.getInteger(1)));
    }

    public Single<World> update(World world) {
        return client.rxPreparedQuery("UPDATE world SET randomnumber = $1 WHERE id = $2",
                Tuple.of(world.getRandomNumber(), world.getId()))
                .map(rows -> world);
    }
}
