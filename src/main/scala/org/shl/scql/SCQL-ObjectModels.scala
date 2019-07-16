package org.shl.scql

import java.io.File


trait Packaged {

  val package$$:String
}

trait GeneratesModelClass extends TableDecl with Packaged{

  import org.shl.util.{RichFile}

  def writeModelSourceFiles(path:String) = {

    def indentArgs(i:Int) = ",\n" + Range(0, i).map( x => "  ").mkString("")
    def indentStmts(i:Int) = "\n" + Range(0, i).map( x => "  ").mkString("")

    lazy val fields = {
      orderedColumns
        .map("val " + _.directContructorDecl)
        .mkString(indentStmts(1))
    }


    lazy val overrideConstructor = {
      orderedColumns
        .map("override val " + _.directContructorDecl)
        .mkString(indentArgs(1))
    }

    lazy val mapConstructor = {
      val argList = orderedColumns
        .map(_.mapConstructorStmt)
        .mkString(indentArgs(2))
      s"""def this(map:Map[String, Any]) = this(
         |    ${argList}
         |    )""".stripMargin
    }


    lazy val rowConstructor = {
      val rowClass = "org.shl.scql.types.Row"
      val argList = orderedColumns
        .map(_.rowConstructorStmt)
        .mkString(indentArgs(2))
      s"""def this(row:${rowClass}) = this(
         |    ${argList}
         |    )""".stripMargin
    }

    val traitDecl = {
      s"""trait Abstract${name$$} {
         |  ${fields}
         |}
       """.stripMargin
    }

    val caseClassDecl = {
      s"""case class ${name$$} (${overrideConstructor})
         |extends Abstract${name$$} with Custom${name$$}{
         |
         |  ${mapConstructor}
         |  ${rowConstructor}
         |
         |  def save:Unit = {}
         |}
       """.stripMargin
    }


    val totalDecl =
      s"""package ${package$$}
         |
         |${traitDecl}
         |
         |${caseClassDecl}
         |
       """.stripMargin


    val custDecl =
      s"""package ${package$$}
         |
         |trait Custom${name$$} extends AbstractBar{
         | // put deriveds here
         |}""".stripMargin



    val pathList = (path.split("/") ++ package$$.split("\\.")).toList

    new File(pathList.mkString("/")).mkdirs()

    new File((pathList :+ s"${name$$}.scala").mkString("/")).write(totalDecl)

    new File((pathList :+ s"Custom${name$$}.scala").mkString("/")).ifNotExists(_.write(custDecl))
  }
}
