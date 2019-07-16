package org.shl.scql

import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.session.Session

import scala.reflect.ClassTag
import org.shl.util.NestedObjectReflector

sealed trait QueryElement[T] {
  override def toString = ""
}

//case class WhereElement[T] extends

class Query[T](elems: List[QueryElement[T]]) extends TraversableOnce[T] {
  // Members declared in scala.collection.GenTraversableOnce
  def isTraversableAgain: Boolean = false
  def toIterator: Iterator[T] = ???
  def toStream: Stream[T] = ???

  // Members declared in scala.collection.TraversableOnce
  def copyToArray[B >: T](xs: Array[B], start: Int, len: Int): Unit = ???
  def exists(p: T => Boolean): Boolean = ???
  def find(p: T => Boolean): Option[T] = ???
  def forall(p: T => Boolean): Boolean = ???
  def foreach[U](f: T => U): Unit = ???
  def hasDefiniteSize: Boolean = ???
  def isEmpty: Boolean = ???
  def seq: scala.collection.TraversableOnce[T] = ???
  def toTraversable: Traversable[T] = ???

  // custom overrides to add query functionality

  // custom function to materialise query
  def apply(): Query[T] = {
    this
  }
}

object from {
  def apply[L: ClassTag, T: ClassTag]: Query[T] = {
    null
  }
}

trait CanSave[T <: CanSave[T]] extends SelfNamedObject with NestedObjectReflector with TableDecl with HasKeyspace {
  val session:CqlSession
  lazy val insertStatement = {
    val cols = orderedColumns.map(_.name$$).mkString(",") // TODO: filter on transients and deriveds, etc.
    val placeHolders = orderedColumns.map(":" + _.name$$).mkString(",")
    s"INSERT INTO ${keyspace$$}.${name$$} (${cols}) VALUES (${placeHolders})"
  }

  lazy val saveStatement = session.prepare(insertStatement)



  def bindDecl = {
    s"""
       | def bind(statement:Statement) = {}
     """.stripMargin
  }
}


package object types {
  type Row = com.datastax.oss.driver.api.core.cql.Row
}
