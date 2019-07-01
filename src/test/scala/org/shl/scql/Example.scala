package org.shl.scql


object Foo extends CreateKeyspace with FooSpace{
  lazy val $$replicationStrategy = SimpleReplicationStrategy()
  lazy val $$durableWrites = true
}

trait FooSpace extends HasKeyspace {

  lazy val keyspace$$ = Foo

}


object Bar extends CreateTable with FooSpace{

  @column object firstName extends TextColumn(partitionKey = true)

  @column object lastName extends TextColumn(clusteringKey = true)

  @column object email extends TextColumn

  @column object someNumber extends IntColumn

  override val options$$ = Comment("Bar") and CompactStorage and ClusteringOrder(Asc(firstName), Desc(someNumber))

  @expand class Type
}

object Bar2 extends AlterTable with FooSpace {

  object someGarbage extends TextColumn
  override val alterations = Add(someGarbage)
}

