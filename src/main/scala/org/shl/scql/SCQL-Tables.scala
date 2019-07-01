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
import java.time.Instant
import java.util.UUID
import scala.reflect.ClassTag

import javax.swing.SortOrder

import scala.annotation.StaticAnnotation

sealed trait TableParamDecl extends Statement {

}


trait ColumnDecls extends NestedObjectReflector{


  sealed abstract class ColumnDecl(val partitionKey: Boolean = false, val clusteringKey: Boolean = false) extends TableParamDecl with SelfNamedObject {
    val typeLabel$$: String
    val typeClass$$: Class[_]

    final override val toString = s"${name$$} ${typeLabel$$}"
    final val toStringInlineKeyDecl$$ = s"${name$$} ${typeLabel$$} PRIMARY KEY"
  }


  abstract class AsciiColumn(override val partitionKey: Boolean = false, override val clusteringKey: Boolean = false) extends ColumnDecl(partitionKey, clusteringKey) {
    final override val typeLabel$$ = "ascii"
    final override val typeClass$$ = classOf[String]
  }

  abstract class BigintColumn(override val partitionKey: Boolean = false, override val clusteringKey: Boolean = false) extends ColumnDecl(partitionKey, clusteringKey) {
    final override val typeLabel$$ = "bigint"
    final override val typeClass$$ = classOf[BigInt]
  }

  abstract class BlobColumn(override val partitionKey: Boolean = false, override val clusteringKey: Boolean = false) extends ColumnDecl(partitionKey, clusteringKey) {
    final override val typeLabel$$ = "blob"
    final override val typeClass$$ = classOf[Array[Byte]]
  }

  abstract class BooleanColumn(override val partitionKey: Boolean = false, override val clusteringKey: Boolean = false) extends ColumnDecl(partitionKey, clusteringKey) {
    final override val typeLabel$$ = "Boolean"
    final override val typeClass$$ = classOf[Array[Byte]]
  }

  abstract class CounterColumn(override val partitionKey: Boolean = false, override val clusteringKey: Boolean = false) extends ColumnDecl(partitionKey, clusteringKey) {
    final override val typeLabel$$ = "counter"
    final override val typeClass$$ = classOf[Int]
  }

  abstract class DecimalColumn(override val partitionKey: Boolean = false, override val clusteringKey: Boolean = false) extends ColumnDecl(partitionKey, clusteringKey) {
    final override val typeLabel$$ = "decimal"
    final override val typeClass$$ = classOf[Double] // TODO: check this

  }

  abstract class DoubleColumn(override val partitionKey: Boolean = false, override val clusteringKey: Boolean = false) extends ColumnDecl(partitionKey, clusteringKey) {
    final override val typeLabel$$ = "double"
    final override val typeClass$$ = classOf[Double]
  }

  abstract class FloatColumn(override val partitionKey: Boolean = false, override val clusteringKey: Boolean = false) extends ColumnDecl(partitionKey, clusteringKey) {
    final override val typeLabel$$ = "float"
    final override val typeClass$$ = classOf[Float]
  }

  abstract class InetColumn(override val partitionKey: Boolean = false, override val clusteringKey: Boolean = false) extends ColumnDecl(partitionKey, clusteringKey) {
    final override val typeLabel$$ = "inet"
    final override val typeClass$$ = classOf[InetAddress]
  }

  abstract class IntColumn(override val partitionKey: Boolean = false, override val clusteringKey: Boolean = false) extends ColumnDecl(partitionKey, clusteringKey) {
    final override val typeLabel$$ = "int"
    final override val typeClass$$ = classOf[Int]
  }

  abstract class TextColumn(override val partitionKey: Boolean = false, override val clusteringKey: Boolean = false) extends ColumnDecl(partitionKey, clusteringKey) {
    final override val typeLabel$$ = "text"
    final override val typeClass$$ = classOf[String]
  }

  abstract class TimestampColumn(override val partitionKey: Boolean = false, override val clusteringKey: Boolean = false) extends ColumnDecl(partitionKey, clusteringKey) {
    final override val typeLabel$$ = "timestamp"
    final override val typeClass$$ = classOf[Instant]
  }

  abstract class TimeuuidColumn(override val partitionKey: Boolean = false, override val clusteringKey: Boolean = false) extends ColumnDecl(partitionKey, clusteringKey) {
    final override val typeLabel$$ = "timeuuid"
    final override val typeClass$$ = classOf[UUID]
  }

  abstract class UuidColumn(override val partitionKey: Boolean = false, override val clusteringKey: Boolean = false) extends ColumnDecl(partitionKey, clusteringKey) {
    final override val typeLabel$$ = "uuid"
    final override val typeClass$$ = classOf[UUID]
  }

  abstract class VarcharColumn(override val partitionKey: Boolean = false, override val clusteringKey: Boolean = false) extends ColumnDecl(partitionKey, clusteringKey) {
    final override val typeLabel$$ = "varchar"
    final override val typeClass$$ = classOf[String]
  }

  abstract class VarintColumn(override val partitionKey: Boolean = false, override val clusteringKey: Boolean = false) extends ColumnDecl(partitionKey, clusteringKey) {
    final override val typeLabel$$ = "varint"
    final override val typeClass$$ = classOf[Long]
  }

  abstract class ListColumn[T :ClassTag](override val partitionKey: Boolean = false, override val clusteringKey: Boolean = false) extends ColumnDecl(partitionKey, clusteringKey) {
    final override val typeLabel$$ = "list"
    final override val typeClass$$ = classOf[List[T]]
  }

  abstract class MapColumn[K :ClassTag, V :ClassTag](override val partitionKey: Boolean = false, override val clusteringKey: Boolean = false) extends ColumnDecl(partitionKey, clusteringKey) {
    final override val typeLabel$$ = "map"
    final override val typeClass$$ = classOf[Map[K, V]]
  }

  abstract class SetColumn[T :ClassTag](override val partitionKey: Boolean = false, override val clusteringKey: Boolean = false) extends ColumnDecl(partitionKey, clusteringKey) {
    final override val typeLabel$$ = "set"
    final override val typeClass$$ = classOf[Set[T]]
  }

}

trait TableOption {// not sealed to allow vendor specific options
  final def and(t: TableOption) = Set(this, t)
}

sealed trait TableOptions {
  implicit def convertOptionToSingletonSet(option: TableOption) = Set(option)
  implicit class TableOptionsSet(options: Set[TableOption]) {
    def and(t: TableOption) = options + t
    def asListString = if (options.isEmpty) "" else "WITH %s".format(options.mkString(" AND "))
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
    def stringifyOptList(l:List[(String, Any)]) = l
      .map{x => x._2 match {
        case v @ (_:Int | _:Long | _:Short | _:Byte | _:Double | _:Float | _:Boolean) => s"'${x._1}' = ${x._2}"
        case v:AnyRef => s"'${x._1}' = '${x._2}'"
      }}
      .mkString(", ")

  }

  case class Compaction(
    `class`:String,
    options:(String, Any)*
  ) extends OptionWithOptionList {
    override def toString = s"compaction = ${optListString}"
    lazy val optListString = stringifyOptList( ("class" -> `class`) +: options.toList)
  }

  case class Compression(
    `class`:String,
    enabled:Boolean = true,
    chunkLengthInKb:Long = 64,
    crcCheckChance:Double = 1.0
  ) extends OptionWithOptionList {
    override def toString = s"compaction = ${optListString}"
    lazy val optListString = stringifyOptList( List("class" -> `class`, "enabled" -> enabled, "chunk_length_in_kb" -> chunkLengthInKb, "crc_check_chance" -> crcCheckChance) )
  }


  object All {
    override def toString = "ALL"
  }

  // TODO: make the union type trait handle n-arity, because this view bound is bloody ugly
  case class AnyOf3[A,B,C](a:A, b:B, c:C)

  implicit def int2AnyOf3(x:Int) = AnyOf3[All.type, None.type, Int](All, None, x)
  implicit def all2AnyOf3(x:All.type ) = AnyOf3[All.type, None.type, Int](All, None, 0)
  implicit def None2AnyOf3(x:None.type ) = AnyOf3[All.type, None.type, Int](All, None, 0)

  AnyOf3[All.type, None.type, Int](_, _, 2)

  case class Caching[K: (All.type | None.type)#union, RPP <% AnyOf3[All.type, None.type, Int]](
    keys:K,
    rowsPerPartition: RPP
  )extends OptionWithOptionList {
    override def toString = s"compaction = ${optListString}"
    lazy val optListString = stringifyOptList( List("keys" -> keys, "rows_per_partition" -> rowsPerPartition) )
  }
}


trait TableDecl extends Statement with SelfNamedObject with HasKeyspace with NestedObjectReflector {

  class column extends StaticAnnotation // todo: at the moment just a semaphore

}

abstract class CreateTable extends TableDecl with IfNotExistsClause with ColumnDecls with AlterableTableOptions {

  val useInlinePrimaryKeyDecls$$ = false



  final lazy val columns$$:Set[ColumnDecl] = modules$$[ColumnDecl]

  final lazy val partitionKeyDecls$$ = columns$$.filter(_.partitionKey)
  final lazy val clusteringKeyDecls$$ = columns$$.filter(_.clusteringKey)

  sealed trait PrimaryKeyDecl extends TableParamDecl {

  }

  case class SimpleKeyDecl(val column:ColumnDecl) extends PrimaryKeyDecl {
    override def toString = {
      val colName = column.name$$
      s"PRIMARY KEY (${colName})"
    }
  }


  case class ClusteringPartitionKeyDecl(val partitionColumns:Set[ColumnDecl], val clusteringKeys:Set[ColumnDecl]) extends PrimaryKeyDecl {
    override def toString = {
      val pColNames = partitionColumns.map(_.name$$).mkString(", ")
      val cColNames = partitionColumns.map(_.name$$).mkString(", ")
      s"PRIMARY KEY ((${pColNames}), (${cColNames}))"
    }
  }

  case class ClusteringOrderDecl(decl: (ColumnDecl, SortOrder)*)

  sealed abstract class SortDirection(column: ColumnDecl) {
    // TODO: check columns later?
    if (!columns$$.contains(column)) throw new Exception(s"Column ${column.name$$} does not exist in table ${name$$}")
    protected val dirString$$:String
    private lazy val asString$$ = s"${column.name$$} ${dirString$$}"
    override final def toString = asString$$
  }

  case class Asc(column: ColumnDecl) extends SortDirection(column) {
    override protected val dirString$$ = "ASC"
  }
  case class Desc(column: ColumnDecl) extends SortDirection(column) {
    override protected val dirString$$ = "DESC"
  }

  case class ClusteringOrder(decls: SortDirection*) extends TableOption{
    private lazy val orderingString = decls
      .toSet
      .map{x:SortDirection => x.toString}
      .mkString(", ")
    override final def toString = s"CLUSTERING ORDER BY (${orderingString})"
  }

  case object CompactStorage extends TableOption{
    override final def toString = s"COMPACT STORAGE"
  }

  final lazy val keyDecl$$ = {
    (partitionKeyDecls$$.size, clusteringKeyDecls$$.size) match {
      case (0, _) =>  throw new Exception(s"You must declare a primary key on table ${name$$}")
      case (x, y) if (x >= 1 || y >= 1) => ClusteringPartitionKeyDecl(partitionKeyDecls$$, clusteringKeyDecls$$)
      case (1, _) => SimpleKeyDecl(partitionKeyDecls$$.head)
    }
  }

  private lazy val columnDeclString$$ = (if (useInlinePrimaryKeyDecls$$) {
    if (partitionKeyDecls$$.size == 0) throw new Exception(s"You must declare a single primary key on table ${name$$} to use inline notation for partition keys")
    columns$$.map(_.toStringInlineKeyDecl$$)
  } else {
    (columns$$.toList :+ keyDecl$$)
  }).mkString(",\n")


  val options$$: Set[TableOption] = Set()

  override val toString =
    s"""CREATE TABLE ${ifNotExistsClause$$} ${keyspace$$.name$$}.${name$$} (
       | ${columnDeclString$$}
       ) ${options$$.asListString}""".stripMargin
}


abstract class AlterTable extends TableDecl with ColumnDecls with AlterableTableOptions with HasKeyspace {
  trait ColumnOperation

  case class Add(columnDecls: ColumnDecl*) extends ColumnOperation {
    def colString = columnDecls.mkString(", ")
    override def toString = s"ADD ${colString}"
  }


  case class Drop(columnDecls: ColumnDecl*) extends ColumnOperation {
    def colString = columnDecls.map(_.name$$).mkString(", ")
    override def toString = s"DROP ${colString}"
  }

  private case object NoColumnAlterations extends ColumnOperation {
    override def toString = ""
  }

  protected case class AlterDecl(columns: ColumnOperation, options: Set[TableOption])

  protected implicit def columnOperationToAlterDecl(c:ColumnOperation) = AlterDecl(c, Set())
  protected implicit def optionSetToAlterDecl(c:Set[TableOption]) = AlterDecl(NoColumnAlterations, c)

  val alterations:AlterDecl

  override def toString =
    s"""ALTER TABLE ${keyspace$$.name$$}.${name$$} (
       | ${alterations.columns}
       ) ${alterations.options}""".stripMargin
}