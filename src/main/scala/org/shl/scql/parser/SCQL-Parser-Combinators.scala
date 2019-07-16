package org.shl.scql.parser

import scala.util.parsing.combinator.RegexParsers
import org.shl.util.RegexStringContext

object Grammar extends RegexParsers {


  // todo: probably useful to have some hungarian for utils and atoms and grammar modules, or use further encapsulation

  type DelimitedList[T] = T ~ List[String ~ T]
  type VerificationFunction[T] = Iterable[T] => Option[Exception]

  def collapse[T](v: DelimitedList[T]): List[T] = v match {
    case head ~ tail => collapse(head, tail)
  }

  def collapse[T](head: T, tail: List[String ~ T]): List[T] = head +: tail.map {
    _ match {
      case _ ~ col => col
    }
  }


  def optionListDecl(v:Parser[Nodes.Options.Option]) = { // TODO: functionalise this
    regexi"with" ~ v ~ (regexi"and" ~ v).*
  } ^^ {
    case _ ~ head ~ tail => collapse(head, tail)
  }

  val identifier = regex"[a-zA-Z][a-zA-Z0-9_]*"

  def cqlNativeType: Parser[Nodes.Types.Native] = {
    regexi"ascii" |
      regexi"bigint" |
      regexi"blob" |
      regexi"boolean" |
      regexi"counter" |
      regexi"decimal" |
      regexi"double" |
      regexi"float" |
      regexi"inet" |
      regexi"int" |
      regexi"text" |
      regexi"timestamp" |
      regexi"timeuuid" |
      regexi"uuid" |
      regexi"varchar" |
      regexi"varint"
  } ^^ {
    case x: String => Nodes.Types.Native(x)
  }

  def cqlListType: Parser[Nodes.Types.List] = {
    regexi"list<" ~ cqlType ~ regexi">"
  } ^^ {
    case _ ~ tpe ~ _ => Nodes.Types.List(tpe)
  }

  def cqlSetType: Parser[Nodes.Types.Set] = {
    regexi"set<" ~ cqlType ~ ">"
  } ^^ {
    case _ ~ tpe ~ _ => Nodes.Types.Set(tpe)
  }

  def cqlMapType: Parser[Nodes.Types.Map] = {
    regexi"map<" ~ cqlType ~ "," ~ cqlType ~ ">"
  } ^^ {
    case _ ~ keyType ~ _ ~ valType ~ _ => Nodes.Types.Map(keyType, valType)
  }

  def cqlTupleType: Parser[Nodes.Types.Tuple] = {
    "frozen".? ~ "tuple<" ~ cqlType ~ ("," ~ cqlType).+ ~ ">"
  } ^^ {
    case _ ~ _ ~ head ~ tail ~ _ => Nodes.Types.Tuple(collapse(head, tail))
  }

  val quotedString = regexi"'[0-9a-zA-Z\\. _-]+'"
  def int = regex"[1-9]([0-9]+)?" ^^ {
    _.toInt
  }
  def double = regex"([0]|[1-9]([0-9]+)?)\\.[0-9]+" ^^ {
    _.toDouble
  }

  def number = int | double

  def mapValue = quotedString | number
  def mapKey: Parser[String] = quotedString
  def mapEntry = {
    mapKey ~ ":" ~ mapValue
  } ^^ {
    case key ~ _ ~ value => (key -> value)
  }
  def mapStmt = {
    "{" ~ mapEntry ~ ("," ~ mapEntry).* ~ "}"
  } ^^ {
    case _ ~ head ~ tail ~ _ => collapse(head, tail).toMap
  }

  def compactStorageStmt: Parser[Nodes.Options.Option] = regexi"compact storage" ^^ {
    case _ => Nodes.Options.Tables.CompactStorage
  }

  def clusteringStmt = {
    identifier ~ regexi"(asc|desc)"
  } ^^ {
    case col ~ any => any.toLowerCase match {
      case "asc" => Nodes.Ordering.Asc(col)
      case "desc" => Nodes.Ordering.Desc(col)
    }
  }
  def clusteringOrderStmt: Parser[Nodes.Options.Option] = {
    regexi"clustering order \\(" ~ clusteringStmt ~ ("," ~ clusteringStmt).* ~ ")"
  } ^^ {
    case _ ~ head ~ tail ~ _ => Nodes.Options.Tables.ClusteringOrder(collapse(head, tail))
  }

  def otherOptionStmt: Parser[Nodes.Options.Option] = {
    quotedString ~ "=" ~ mapStmt
  } ^^ {
    case name ~ _ ~ valueMap => Nodes.Options.Tables.OtherOption(name, valueMap)
  }

  def tableOption:Parser[Nodes.Options.Option] = {compactStorageStmt | clusteringStmt | otherOptionStmt} ^^{
    case x => x.asInstanceOf[Nodes.Options.Option]
  }


  def tableOptionList = optionListDecl(tableOption)


  def cqlType: Parser[Nodes.Types.Type] = cqlNativeType | cqlListType | cqlSetType | cqlMapType

  def listOfCols: Parser[List[String]] = {
    identifier ~ (regex"," ~ identifier).*
  } ^^ {

    case list: DelimitedList[String] => collapse(list)
  }

  def primaryKeyStmt: Parser[Nodes.PrimaryKey] = {
    regexi"primary key \\(" ~ listOfCols ~ (regex", (" ~ listOfCols ~ ")").? ~ ")"
  } ^^ {
    case _ ~ pkList ~ opt ~ _ => {
      val clusteringKeys = opt.map {
        _ match {
          case _ ~ clusKey ~ _ => Nodes.ClusteringKeys(clusKey)
        }
      }
      Nodes.PrimaryKey(pkList, clusteringKeys)
    }
  }

  def columnStmt: Parser[Nodes.Column] = {
    identifier ~ cqlType ~ regexi"primary key".?
  } ^^ {
    case name ~ tpe ~ pk => Nodes.Column(name, tpe, pk.isDefined)
  }

  def columnListStmt = { // TODO: add PK def
    columnStmt ~ (regex"," ~ columnStmt).*
  } ^^ {
    case list: DelimitedList[Nodes.Column] => collapse(list)
  }

  def schemaQualifier = {
    identifier ~ regex"\\."
  } ^^ {
    case sName ~ _ => Nodes.SchemaName(sName)
  }

  def nameWithSchema = schemaQualifier.? ~ identifier ^^ {
    case None ~ tName => Nodes.TableName(name = tName)
    case tSchema ~ tName => Nodes.TableName(name = tName, schema = tSchema)
  }

  def createTable = {
    regexi"create table" ~ regexi"if not exists".? ~ nameWithSchema ~ regex"\\(" ~ columnListStmt ~ primaryKeyStmt.? ~ "\\)" ~ tableOptionList.?
  } ^^ {
    case _ ~ ine ~ tName ~ _ ~ colList ~ pk ~ _ ~ opts => {
      Nodes.Statements.CreateTable(name = tName, columns = colList, options = opts, primaryKeys = pk, ifNotExists = ine.isDefined)
    }
  }


  def durableWritesStmt = {regexi"durable_writes = " ~ {regexi"true" | regexi"false"}} ^^{
    case _ ~ value => Nodes.Options.KeySpaces.DurableWrites(value.toBoolean)
  }

  def replicationStmt = { regexi"replication = " ~ mapStmt} ^^ {
    case _ ~ mapVals => Nodes.Options.KeySpaces.Replication(mapVals)
  }

  def keyspaceOptions = optionListDecl{durableWritesStmt | replicationStmt}

  def createKeySpace:Parser[Nodes.Statements.CreateKeyspace] = {
    regexi"create keyspace" ~ regexi"if not exists".? ~ identifier ~ keyspaceOptions.?
  } ^^ {
    case _ ~ ine ~ name ~ opts => Nodes.Statements.CreateKeyspace(name, ine.isDefined, opts)
  }


  def dropKeySpace = {
    regexi"drop keyspace" ~ identifier
  }
}