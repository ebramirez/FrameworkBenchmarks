package io.quarkus.benchmark.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Collections;
import java.util.Map;

@Path("/json")
public class JsonResource {
    private static final String MESSAGE = "message";
    private static final String HELLO = "Hello, World!";
    private static final Map<String, String> map = Collections.singletonMap( MESSAGE, HELLO );

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> json() {
        return map;
    }
}

