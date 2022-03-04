package org.acme

import io.smallrye.mutiny.Multi
import io.smallrye.mutiny.Uni
import io.vertx.mutiny.pgclient.PgPool
import java.net.URI
import javax.annotation.PostConstruct
import javax.enterprise.inject.Default
import javax.inject.Inject
import javax.ws.rs.DELETE
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.jboss.resteasy.annotations.jaxrs.PathParam

@Path("/fruits")
class FruitResource {
    @Inject
    @field:Default
    @ConfigProperty(name = "myapp.schema.create", defaultValue = "true")
    private val schemaCreate: Boolean = true

    @Inject @field:Default lateinit var client: io.vertx.mutiny.pgclient.PgPool

    @PostConstruct
    fun config() {
        if (schemaCreate) {
            initdb()
        }
    }

    private fun initdb() {
        client.query("DROP TABLE IF EXISTS fruits")
                .execute()
                .flatMap {
                    client.query("CREATE TABLE fruits (id SERIAL PRIMARY KEY, name TEXT NOT NULL)")
                            .execute()
                }
                .flatMap { client.query("INSERT INTO fruits (name) VALUES ('Orange')").execute() }
                .flatMap { client.query("INSERT INTO fruits (name) VALUES ('Pear')").execute() }
                .flatMap { client.query("INSERT INTO fruits (name) VALUES ('Apple')").execute() }
                .await()
                .indefinitely()
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun getFruits(): Multi<Fruit> {
        return Fruit.findAll(client)
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    fun getFruit(@PathParam id: Long): Uni<Response> {
        return Fruit.findById(client, id)
                .onItem()
                .transform { fruit ->
                    if (fruit != null) Response.ok(fruit) else Response.status(Status.NOT_FOUND)
                }
                .onItem()
                .transform { it.build() }
    }

    @POST
    fun createFruit(fruit: Fruit): Uni<Response> {
        return fruit
                .save(client)
                .onItem()
                .transform { URI.create("/fruits/" + it) }
                .onItem()
                .transform { Response.created(it).build() }
    }

    @DELETE
    @Path("{id}")
    fun delete(@PathParam id: Long): Uni<Response> {
        return Fruit.delete(client, id)
                .onItem()
                .transform { deleted -> if (deleted) Status.NO_CONTENT else Status.NOT_FOUND }
                .onItem()
                .transform { Response.status(it).build() }
    }
}
