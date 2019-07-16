package org.shl.scql

import scala.language.experimental.macros


trait FooSpace extends HasKeyspace with Packaged{
  override lazy val package$$ = "org.shl.foo"
  override lazy val keyspace$$ = Foo
}


object Foo extends CreateKeyspace with FooSpace {
  lazy val $$replicationStrategy = SimpleReplicationStrategy()
  lazy val $$durableWrites = true
}




// TODO: make this a trait, or an abstract class with a version tag
object Bar extends CreateTable with FooSpace with GeneratesModelClass{

  object firstName extends TextColumn(1) with PartitionKey

  object lastName extends TextColumn(2) with ClusteringKey

  object pictureOrSomething extends BlobColumn(3)

  object someNumber extends IntColumn

  object passwordPlainText extends TextColumn(4) with Transient[String] {
    override val default = ""
  }

  object email extends TextColumn

  override lazy val options$$ = Comment("Bar") and CompactStorage and ClusteringOrder(Asc(firstName), Desc(someNumber))


}

object Bar2 extends AlterTable with FooSpace {

  object someGarbage extends TextColumn

  override val alterations = Add(someGarbage)
}


object Example extends App {
  Bar.writeModelSourceFiles("./src/test/scala")

}

