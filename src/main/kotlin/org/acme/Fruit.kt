package org.acme

import io.smallrye.mutiny.Multi
import io.smallrye.mutiny.Uni
import io.vertx.mutiny.pgclient.PgPool
import io.vertx.mutiny.sqlclient.Row
import io.vertx.mutiny.sqlclient.Tuple

class Fruit(id: Long, name: String) {
  val id: Long
  val name: String

  companion object {
    fun from(row: Row): Fruit {
      return Fruit(row.getLong("id"), row.getString("name"))
    }

    fun findAll(client: PgPool): Multi<Fruit> {
      return client
          .query("SELECT id, name FROM fruits ORDER BY name ASC")
          .execute()
          .onItem()
          .transformToMulti { set -> Multi.createFrom().iterable(set) }
          .onItem()
          .transform(Fruit::from)
    }

    fun findById(client: PgPool, id: Long): Uni<Fruit> {
      return client
          .preparedQuery("SELECT id, name FROM fruits WHERE id = $1")
          .execute(Tuple.of(id))
          .onItem()
          .transform { it.iterator() }
          .onItem()
          .transform { iterator -> if (iterator.hasNext()) from(iterator.next()) else null }
    }

    fun delete(client: PgPool, id: Long): Uni<Boolean> {
      return client
          .preparedQuery("DELETE FROM fruits WHERE id = $1")
          .execute(Tuple.of(id))
          .onItem()
          .transform { it.rowCount() == 1 }
    }
  }

  init {
    this.id = id
    this.name = name
  }

  fun save(client: PgPool): Uni<Long> {
    return client
        .preparedQuery("INSERT INTO fruits (name) VALUES ($1) RETURNING id")
        .execute(Tuple.of(name))
        .onItem()
        .transform { it.iterator().next().getLong("id") }
  }
}
