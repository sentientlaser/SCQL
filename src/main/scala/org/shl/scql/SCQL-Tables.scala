// Copyright (C) 2018-2019 the original author or authors.
// See the LICENCE.txt file distributed with this work for additional
// information regarding copyright ownership.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at

// http://www.apache.org/licenses/LICENSE-2.0

// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.shl.scql

import java.net.InetAddress
import java.nio.ByteBuffer
import java.time.Instant
import java.util.UUID

import com.datastax.oss.driver.api.core.ProtocolVersion
import com.datastax.oss.driver.api.core.`type`.DataTypes
import com.datastax.oss.driver.api.core.`type`.codec.{PrimitiveIntCodec, TypeCodec, TypeCodecs}

import scala.reflect.runtime.universe.typeOf
import scala.reflect.runtime.universe.typeTag
import scala.reflect.runtime.universe.TypeTag
import javax.swing.SortOrder


import org.shl.util.{NestedObjectReflector, UnionType}
import scala.annotation.StaticAnnotation

sealed trait TableParamDecl extends Statement {

}


trait PartitionKey

trait ClusteringKey

trait Transient[T]{
  val default:T
}

trait ComputeOnStore[T, U] {

}

trait ComputeOnLoad[T, U] {

}

abstract class ScalaCodec[T: TypeTag] extends TypeCodec[T] {
  override def getJavaType = ???
  def getScalaType = typeTag[T]
}

class ScalaCodecFacade[T: TypeTag, K](val underlyingCodec:TypeCodec[K]) extends ScalaCodec[T] {
  override def getCqlType = underlyingCodec.getCqlType
  override def encode(value: T, protocolVersion: ProtocolVersion) = underlyingCodec.encode(value.asInstanceOf[K], protocolVersion)
  override def decode(bytes: ByteBuffer, protocolVersion: ProtocolVersion) = underlyingCodec.decode(bytes, protocolVersion).asInstanceOf[T]

  override def format(value: T) = underlyingCodec.format(value.asInstanceOf[K])
  override def parse(value: String) = underlyingCodec.parse(value).asInstanceOf[T]
}

// todo: turn this into a trait
trait ColumnDecl[T] extends TableParamDecl with SelfNamedObject with NestedObjectReflector{

  val ord:Option[Int] = None

  val codec:TypeCodec[T]
  final type Row = com.datastax.oss.driver.api.core.cql.Row

  @inline def toRuntimeType(row:Row):T = row.get(name$$, codec)

  lazy val cassandraTypeDecl: String = codec.getCqlType.asCql(true, true)
  val scalaRuntimeType: TypeTag[T]
  lazy val scalaRuntimeTypeDecl: String = scalaRuntimeType.tpe.toString

  lazy val cassandraTableDecl = s"${name$$} ${cassandraTypeDecl}"
  lazy val cassandraInlineKeyTableDecl = s"${name$$} ${cassandraTypeDecl} PRIMARY KEY"
  lazy val directContructorDecl = s"${name$$}:${scalaRuntimeTypeDecl}"
  lazy val mapConstructorStmt = s"""${name$$} = map("${name$$}").asInstanceOf[${scalaRuntimeTypeDecl}]"""
  lazy val rowConstructorStmt = {
    val fqnClass = mirror$$.classSymbol(this.getClass).toType.toString.replaceAll("\\.type$", "")
    s"""${name$$} = ${fqnClass}.toRuntimeType(row)"""
  }
}


abstract class AsciiColumn(override val ord:Option[Int] = None) extends ColumnDecl[String] {
  def this(ord:Int) = this(Some(ord))
  override val codec = TypeCodecs.ASCII
  override val scalaRuntimeType = typeTag[String]
}


//abstract class BigintColumn extends ColumnDecl {
//  override val cassandraTypeDecl = "bigint"
//}
//
abstract class BlobColumn(override val ord:Option[Int] = None) extends ColumnDecl[Array[Byte]]{
  def this(ord:Int) = this(Some(ord))
  override val codec = new ScalaCodecFacade[Array[Byte], ByteBuffer](TypeCodecs.BLOB){
    override def encode(value: Array[Byte], protocolVersion: ProtocolVersion) = super.encode(value, protocolVersion)
    override def decode(bytes: ByteBuffer, protocolVersion: ProtocolVersion) = underlyingCodec.decode(bytes, protocolVersion).array
  }
  override val scalaRuntimeType = typeTag[Array[Byte]]
}


//
//abstract class BooleanColumn extends ColumnDecl {
//  override val cassandraTypeDecl = "boolean"
//  override val scalaRuntimeType = classOf[Boolean]
//  override val scalaRuntimeTypeDecl = "Boolean"
//}
//
//abstract class CounterColumn extends ColumnDecl {
//  override val cassandraTypeDecl = "counter"
//  override val scalaRuntimeType = classOf[Int]
//  override val scalaRuntimeTypeDecl = "Int"
//}
//
//abstract class DecimalColumn extends ColumnDecl {
//  override val cassandraTypeDecl = "decimal"
//  override val scalaRuntimeType = classOf[Double] // TODO: check this
//  override val scalaRuntimeTypeDecl = "Double"
//
//}
//
//abstract class DoubleColumn extends ColumnDecl {
//  override val cassandraTypeDecl = "double"
//  override val scalaRuntimeType = classOf[Double]
//  override val scalaRuntimeTypeDecl = "Double"
//}
//
//abstract class FloatColumn extends ColumnDecl {
//  override val cassandraTypeDecl = "float"
//  override val scalaRuntimeType = classOf[Float]
//  override val scalaRuntimeTypeDecl = "Float"
//}
//
//abstract class InetColumn extends ColumnDecl {
//  override val cassandraTypeDecl = "inet"
//  override val scalaRuntimeType = classOf[InetAddress]
//}
//
abstract class IntColumn(override val ord:Option[Int] = None) extends ColumnDecl[Int]{
  def this(ord:Int) = this(Some(ord))
  override val codec = new ScalaCodecFacade[Int, Integer](TypeCodecs.INT)
  override val scalaRuntimeType = typeTag[Int]
}


abstract class TextColumn(override val ord:Option[Int] = None) extends ColumnDecl[String]{
  def this(ord:Int) = this(Some(ord))
  override val codec = TypeCodecs.TEXT
  override val scalaRuntimeType = typeTag[String]
}

//
//abstract class TimestampColumn extends ColumnDecl {
//  override val cassandraTypeDecl = "timestamp"
//  override val scalaRuntimeType = classOf[Instant]
//}
//
//abstract class TimeuuidColumn extends ColumnDecl {
//  override val cassandraTypeDecl = "timeuuid"
//  override val scalaRuntimeType = classOf[UUID]
//}
//
//abstract class UuidColumn extends ColumnDecl {
//  override val cassandraTypeDecl = "uuid"
//  override val scalaRuntimeType = classOf[UUID]
//}
//
//abstract class VarcharColumn extends ColumnDecl {
//  override val cassandraTypeDecl = "varchar"
//  override val scalaRuntimeType = classOf[String]
//  override val scalaRuntimeTypeDecl = "String"
//}
//
//abstract class VarintColumn extends ColumnDecl {
//  override val cassandraTypeDecl = "varint"
//  override val scalaRuntimeType = classOf[Long]
//  override val scalaRuntimeTypeDecl = "Long"
//}

// todo: fix these

//abstract class ListColumn[T: ClassTag] extends ColumnDecl {
//  final override val cassandraTypeDecl = "list"
//  final override val scalaRuntimeType = classOf[List[T]]
//}
//
//abstract class MapColumn[K: ClassTag, V: ClassTag] extends ColumnDecl {
//  final override val cassandraTypeDecl = "map"
//  final override val scalaRuntimeType = classOf[Map[K, V]]
//}
//
//abstract class SetColumn[T: ClassTag] extends ColumnDecl {
//  final override val cassandraTypeDecl = "set"
//  final override val scalaRuntimeType = classOf[Set[T]]
//}


trait TableOption { // not sealed to allow vendor specific options
  final def and(t: TableOption) = Set(this, t)
}

sealed trait TableOptions {
  implicit def convertOptionToSingletonSet(option: TableOption) = Set(option)

  implicit class TableOptionsSet(options: Set[TableOption]) {
    def and(t: TableOption) = options + t
    def asListString = {
      if (options.isEmpty)
        ""
      else
        "WITH %s".format(options.mkString(" AND "))
    }
  }

}

trait AlterableTableOptions extends TableOptions with UnionType {

  case object None {
    override def toString = "NONE"
  }

  sealed trait SpeculativeRetryValue

  case object Always extends SpeculativeRetryValue {
    override def toString = "ALWAYS"
  }

  case class Percentile(percentile: Int) extends SpeculativeRetryValue {
    override def toString = s"${percentile}percentile"
  }

  case class Millis(millis: Long) extends SpeculativeRetryValue {
    override def toString = s"${millis}ms"
  }

  sealed trait ReadRepairValue

  case object Blocking extends ReadRepairValue {
    override def toString = "BLOCKING"
  }

  case class SpeculativeRetry[T: (SpeculativeRetryValue | None.type)#union](strategy: T) extends TableOption {
    override def toString = s"speculative_retry = '${strategy}'"
  }

  case class AdditionalRetryPolicy(policy: SpeculativeRetryValue) extends TableOption {
    override def toString = s"additional_write_policy = '${policy}'"
  }

  case class Comment(comment: String) extends TableOption {
    override final def toString = s"comment = '${comment}'"
  }

  case class GcGraceSeconds(seconds: Long) extends TableOption {
    override final def toString = s"gc_grace_seconds = ${seconds}"
  }

  case class BloomFilterFpChance(chance: Double) extends TableOption {
    override final def toString = s"bloom_filter_fp_chance = ${chance}"
  }

  case class DefaultTimeToLive(seconds: Long) extends TableOption {
    override final def toString = s"default_time_to_live = ${seconds}"
  }

  case class MemtableFlushPeriodInMs(millis: Long) extends TableOption {
    override final def toString = s"memtable_flush_period_in_ms = ${millis}"
  }

  case class ReadRepair[T: (ReadRepairValue | None.type)#union](policy: T) extends TableOption {
    override final def toString = s"read_repair = ${policy}"
  }

  trait OptionWithOptionList {
    def stringifyOptList(l: List[(String, Any)]) = l
      .map { x =>
        x._2 match {
          case v@(_: Int | _: Long | _: Short | _: Byte | _: Double | _: Float | _: Boolean) => s"'${x._1}' = ${x._2}"
          case v: AnyRef => s"'${x._1}' = '${x._2}'"
        }
      }
      .mkString(", ")

  }

  case class Compaction(
    `class`: String,
    options: (String, Any)*
  ) extends OptionWithOptionList {
    override def toString = s"compaction = ${optListString}"
    lazy val optListString = stringifyOptList(("class" -> `class`) +: options.toList)
  }

  case class Compression(
    `class`: String,
    enabled: Boolean = true,
    chunkLengthInKb: Long = 64,
    crcCheckChance: Double = 1.0
  ) extends OptionWithOptionList {
    override def toString = s"compaction = ${optListString}"
    lazy val optListString = stringifyOptList(List("class" -> `class`, "enabled" -> enabled, "chunk_length_in_kb" -> chunkLengthInKb, "crc_check_chance" -> crcCheckChance))
  }


  object All {
    override def toString = "ALL"
  }

  // TODO: make the union type trait handle n-arity, because this view bound is bloody ugly
  case class AnyOf3[A, B, C](a: A, b: B, c: C)

  implicit def int2AnyOf3(x: Int) = AnyOf3[All.type, None.type, Int](All, None, x)
  implicit def all2AnyOf3(x: All.type) = AnyOf3[All.type, None.type, Int](All, None, 0)
  implicit def None2AnyOf3(x: None.type) = AnyOf3[All.type, None.type, Int](All, None, 0)

  AnyOf3[All.type, None.type, Int](_, _, 2)

  case class Caching[K: (All.type | None.type)#union, RPP <% AnyOf3[All.type, None.type, Int]](
    keys: K,
    rowsPerPartition: RPP
  ) extends OptionWithOptionList {
    override def toString = s"compaction = ${optListString}"
    lazy val optListString = stringifyOptList(List("keys" -> keys, "rows_per_partition" -> rowsPerPartition))
  }


}


trait TableDecl extends Statement with SelfNamedObject with HasKeyspace with NestedObjectReflector {

  class column extends StaticAnnotation // todo: at the moment just a semaphore

  final lazy val columns$$: Set[ColumnDecl[_]] = modules$$[ColumnDecl[_]]


  final lazy val orderedColumns = {
    // TODO: filter on transients and deriveds, etc.
    val orderedCols = this.columns$$.filter(_.ord.isDefined).toList.sortBy(x=> (x.ord.get, x.name$$))
    val unorderedCols = this.columns$$.filter(!_.ord.isDefined).toList.sortBy(_.name$$)

    orderedCols ++ unorderedCols
  }

}

abstract class CreateTable extends TableDecl with IfNotExistsClause with AlterableTableOptions {

  val useInlinePrimaryKeyDecls$$ = false


  final lazy val partitionKeyDecls$$ = columns$$.filter(_.isInstanceOf[PartitionKey])
  final lazy val clusteringKeyDecls$$ = columns$$.filter(_.isInstanceOf[ClusteringKey])

  sealed trait PrimaryKeyDecl extends TableParamDecl {

  }

  case class SimpleKeyDecl(val column: ColumnDecl[_]) extends PrimaryKeyDecl {
    override def toString = {
      val colName = column.name$$
      s"PRIMARY KEY (${colName})"
    }
  }


  case class ClusteringPartitionKeyDecl(val partitionColumns: Set[ColumnDecl[_]], val clusteringKeys: Set[ColumnDecl[_]]) extends PrimaryKeyDecl {
    override def toString = {
      val pColNames = partitionColumns.map(_.name$$).mkString(", ")
      val cColNames = partitionColumns.map(_.name$$).mkString(", ")
      s"PRIMARY KEY ((${pColNames}), (${cColNames}))"
    }
  }

  case class ClusteringOrderDecl(decl: (ColumnDecl[_], SortOrder)*)

  sealed abstract class SortDirection(column: ColumnDecl[_]) {
    // TODO: check columns later?
    if (!columns$$.contains(column)) throw new Exception(s"Column ${column.name$$} does not exist in table ${name$$}")
    protected val dirString$$: String
    private lazy val asString$$ = s"${column.name$$} ${dirString$$}"
    override final def toString = asString$$
  }

  case class Asc(column: ColumnDecl[_]) extends SortDirection(column) {
    override protected val dirString$$ = "ASC"
  }

  case class Desc(column: ColumnDecl[_]) extends SortDirection(column) {
    override protected val dirString$$ = "DESC"
  }

  case class ClusteringOrder(decls: SortDirection*) extends TableOption {
    private lazy val orderingString = decls
      .toSet
      .map { x: SortDirection => x.toString }
      .mkString(", ")
    override final def toString = s"CLUSTERING ORDER BY (${orderingString})"
  }

  case object CompactStorage extends TableOption {
    override final def toString = s"COMPACT STORAGE"
  }

  final lazy val keyDecl$$ = {
    (partitionKeyDecls$$.size, clusteringKeyDecls$$.size) match {
      case (0, _) => throw new Exception(s"You must declare a primary key on table ${name$$}")
      case (x, y) if (x >= 1 || y >= 1) => ClusteringPartitionKeyDecl(partitionKeyDecls$$, clusteringKeyDecls$$)
      case (1, _) => SimpleKeyDecl(partitionKeyDecls$$.head)
    }
  }

  private lazy val columnDeclString$$ = (if (useInlinePrimaryKeyDecls$$) {
    if (partitionKeyDecls$$.size == 0) throw new Exception(s"You must declare a single primary key on table ${name$$} to use inline notation for partition keys")
    columns$$.map(_.cassandraInlineKeyTableDecl)
  } else {
    (columns$$.toList.map(_.cassandraTableDecl) :+ keyDecl$$)
  }).mkString(",\n")


  lazy val options$$: Set[TableOption] = Set()

  override val toString = {
    s"""CREATE TABLE ${ifNotExistsClause$$} ${keyspace$$.name$$}.${name$$} (
       | ${columnDeclString$$}
       ) ${options$$.asListString}""".stripMargin
  }
}


abstract class AlterTable extends TableDecl with AlterableTableOptions with HasKeyspace {

  trait ColumnOperation

  case class Add(columnDecls: ColumnDecl[_]*) extends ColumnOperation {
    def colString = columnDecls.mkString(", ")
    override def toString = s"ADD ${colString}"
  }


  case class Drop(columnDecls: ColumnDecl[_]*) extends ColumnOperation {
    def colString = columnDecls.map(_.name$$).mkString(", ")
    override def toString = s"DROP ${colString}"
  }

  private case object NoColumnAlterations extends ColumnOperation {
    override def toString = ""
  }

  protected case class AlterDecl(columns: ColumnOperation, options: Set[TableOption])

  protected implicit def columnOperationToAlterDecl(c: ColumnOperation) = AlterDecl(c, Set())
  protected implicit def optionSetToAlterDecl(c: Set[TableOption]) = AlterDecl(NoColumnAlterations, c)

  val alterations: AlterDecl

  // todo: tostrings should be replaced with something a bit more fit-for-purpose, like "hasCassandraRepresention".  
  // Partly this is because you have to _force_ a specific function, but toString will just fail silently, and it 
  // may be that at some point you _want_ a seperate toString
  override def toString =
    s"""ALTER TABLE ${keyspace$$.name$$}.${name$$} (
       | ${alterations.columns}
       ) ${alterations.options}""".stripMargin
}