package io.quarkus.benchmark.resource;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import io.quarkus.benchmark.model.World;
import io.quarkus.benchmark.repository.WorldRepository;

@ApplicationScoped
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DbResource {

    @Inject
    WorldRepository worldRepository;

    @GET
    @Path("/db")
    public World db() {
        return randomWorldForRead();
    }

    @GET
    @Path("/queries")
    public World[] queries(@QueryParam("queries") String queries) {
        final int count = parseQueryCount( queries );
        World[] worlds = new World[count];
        for (int i=0; i<count; i++) {
            worlds[i] = randomWorldForRead();
        }
        return worlds;
    }

    @GET
    @Path("/updates")
    //Rules: https://github.com/TechEmpower/FrameworkBenchmarks/wiki/Project-Information-Framework-Tests-Overview#database-updates
    //N.B. the benchmark seems to be designed to get in deadlocks when using a "safe pattern" of updating
    // the entity within the same transaction as the one which read it.
    // We therefore need to do a "read then write" while relinquishing the transaction between the two operations, as
    // all other tested frameworks seem to do.
    public World[] updates(@QueryParam("queries") String queries) {
        final int count = parseQueryCount( queries );
        World[] worlds = new World[count];
//        worldRepository.hintBatchSize(count);
        for (int i=0; i<count; i++) {
            World world = randomWorldForRead();
            final int previousRead = world.getRandomNumber();
            world.setRandomNumber( randomWorldNumber() );
            worlds[i] = world;
        }
        worldRepository.updateAll(worlds);
        return worlds;
    }

    @GET
    @Path( "/createdata" )
    public String createData() {
        worldRepository.createData();
        return "OK";
    }

    private World randomWorldForRead() {
        return worldRepository.findReadonly(randomWorldNumber());
    }

    private int randomWorldNumber() {
        return 1 + ThreadLocalRandom.current().nextInt(10000);
    }

    private int parseQueryCount(String textValue) {
        if (textValue == null) {
            return 1;
        }
        int parsedValue;
        try {
            parsedValue = Integer.parseInt(textValue);
        } catch (NumberFormatException e) {
            return 1;
        }
        return Math.min(500, Math.max(1, parsedValue));
    }
}
