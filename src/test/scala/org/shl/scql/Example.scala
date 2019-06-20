package org.shl.scql


import scala.annotation.elidable

@elidable(level = 1000)
object Foo extends CreateKeyspace {
  lazy val $$replicationStrategy = SimpleReplicationStrategy()
  lazy val $$durableWrites = true
}


@elidable(level = 1000)
object Bar extends CreateTable with NestedObjectReflector {

  @column object firstName extends TextColumn(partitionKey = true)

  @column object lastName extends TextColumn(clusteringKey = true)

  @column object email extends TextColumn()

  @column object someNumber extends IntColumn()

  lazy val $$keyspace = Foo

  override val $$options = Comment("Bar") and CompactStorage and ClusteringOrder(Asc(firstName), Desc(someNumber))

}

@elidable(level = 1000)
object Example extends App {

  println(Bar.$$optionsToString)
}

