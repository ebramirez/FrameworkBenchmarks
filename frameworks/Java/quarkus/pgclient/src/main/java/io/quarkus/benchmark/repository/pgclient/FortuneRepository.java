package io.quarkus.benchmark.repository.pgclient;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.quarkus.benchmark.model.Fortune;
import io.reactivex.Single;
import io.vertx.reactivex.pgclient.PgPool;
import io.vertx.reactivex.sqlclient.Row;
import io.vertx.reactivex.sqlclient.RowSet;

@ApplicationScoped
public class FortuneRepository {

    @Inject
    PgPool client;

    public Single<List<Fortune>> findAll() {
        return client.rxQuery("SELECT id, message FROM fortune")
                .map(RowSet::iterator)
                .map(rowIterator -> {
                    List<Fortune> fortunes = new ArrayList<>();
                    while (rowIterator.hasNext()) {
                        Row row = rowIterator.next();
                        Fortune fortune = new Fortune(row.getInteger(0), row.getString(1));
                        fortunes.add(fortune);
                    }
                    return fortunes;
                });
    }
}
