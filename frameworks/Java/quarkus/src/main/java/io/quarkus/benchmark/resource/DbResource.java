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
        World[] worlds = new World[parseQueryCount(queries)];
        Arrays.setAll(worlds, i -> randomWorldForRead());
        return worlds;
    }

    @GET
    @Path("/updates")
    //Rules: https://github.com/TechEmpower/FrameworkBenchmarks/wiki/Project-Information-Framework-Tests-Overview#database-updates
    @Transactional
    public World[] updates(@QueryParam("queries") String queries) {
        final int count = parseQueryCount( queries );
        World[] worlds = new World[count];
        worldRepository.hintBatchSize(count);
        Arrays.setAll(worlds, i -> {
            World world = worldRepository.readWriteWorld(randomWorldNumber());
            world.setRandomNumber(randomWorldNumber());
            return world;
        });

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
