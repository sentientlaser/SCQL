package org.shl.scql.parser

import org.shl.util.toStringF


object Nodes {

  case class SchemaName(name:String) {
    override def toString = name
  }
  case class TableName(name:String, schema:Option[SchemaName] = None) {
    override def toString = s"${schema.map(_ + ".").getOrElse("")}${name}"
  }

  object Types {

    import scala.collection.immutable.{List => ScalaList}

    abstract class Type

    case class Native(val tpe: String) extends Type {
      // TODO: check tpe against list of known types
      override def toString = tpe.toUpperCase
    }

    case class List(val tpe: Type) extends Type {
      override def toString = s"LIST<${tpe}>"
    }

    case class Set(val tpe: Type) extends Type {
      override def toString = s"SET<${tpe}>"
    }

    case class Map(val keyType:Type, val valType:Type) extends Type {
      override def toString = s"MAP<${keyType}, ${valType}>"
    }

    case class Tuple(val types:ScalaList[Type]) extends Type {
      override def toString = s"TUPLE<${types.map(toStringF).mkString(", ")}>"
    }
  }

  case class Column(name:String, tpe: Types.Type, pk:Boolean = false) {
    override def toString = s"${name} ${tpe} ${if (pk) "PRIMARY KEY"}"
  }

  abstract class KeyList(val keys:List[String]) {
    override def toString = keys.mkString(", ")
  }

  case class ClusteringKeys(override val keys:List[String]) extends KeyList(keys)
  case class PrimaryKey(override val keys:List[String], clusteringKeys:Option[ClusteringKeys]) extends KeyList(keys)

  object Ordering {

    abstract class Ordering(val column: String) {// TODO: make these much more stronly typed
      val dir:String
      override def toString = s"${column} ${dir}"
    }
    case class Desc(override val column: String) extends Ordering(column) {
      override final val dir = "DESC"
    }
    case class Asc(override val column: String) extends Ordering(column) {
      override final val dir = "ASC"
    }
  }

  object Options {
    abstract class Option
    object Tables {
      case object CompactStorage extends Option {
        override def toString = "COMPACT STORAGE"
      }

      case class ClusteringOrder(val ordering: List[Ordering.Ordering]) extends Option {
        override def toString = ???
      }

      case class OtherOption(name: String, values: Map[String, Any]) extends Option {
        def renderValues = values.map { v =>
          val (key, value) = v
          val renderedValue = if (value.isInstanceOf[String]) s"'${value}'" else value.toString
          s"'${key}':${renderedValue}"
        }.mkString(", ")


        // TODO: add verification?

        override def toString = s"'${name}' = {${renderValues}}"
      }

    }

    object KeySpaces {
      case class DurableWrites(value:Boolean) extends Option
      case class Replication(policy:Map[String, Any]) extends Option
    }
  }

  object Statements {

    private trait IfNotExistsSupport {
      val ifNotExists:Boolean
      def ifNotExistsToString = if (ifNotExists) "IF NOT EXISTS" else ""
    }

    private trait OptionsSupport {

      val options:Option[List[Options.Option]]
      def optionsToString = options.map(opt => " WITH " + opt.map(toStringF).mkString(" AND ")).getOrElse("")
    }
    case class CreateTable(
      name:TableName,
      columns: List[Column],
      override val options:Option[List[Options.Option]],
      primaryKeys: Option[PrimaryKey],
      override val ifNotExists:Boolean) extends IfNotExistsSupport with OptionsSupport {
    // TODO: add verification
    override def toString = s"""
      |CREATE TABLE ${ifNotExistsToString} ${name} (
      |  ${columns.mkString(", ")}
      |  ${primaryKeys.map(", " + _.toString).getOrElse("")})
      |  ${optionsToString}
      """.stripMargin // tODO: add options to the string
    }

    case class CreateKeyspace(name:SchemaName, override val ifNotExists:Boolean, override val options:Option[List[Options.Option]]) extends IfNotExistsSupport with OptionsSupport{
      override def toString = s"CREATE KEYSPACE ${ifNotExistsToString} ${name} ${optionsToString}"
    }
    object CreateKeyspace {
      def apply(name:String, ifNotExists:Boolean, options:Option[List[Options.Option]]) = CreateKeyspace(SchemaName(name), ifNotExists, options)
    }

}

  }