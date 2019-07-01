package org.shl.scql

import scala.annotation.StaticAnnotation
import scala.language.experimental.macros
import scala.reflect.macros.Context


object Expander {
  def expand_impl(c: Context)(annottees: c.Expr[Any]*) = {
    import c.universe._

    annottees.map(_.tree) match {
      case List(q"trait $name") => c.Expr[Any](
        // Add your own logic here, possibly using arguments on the annotation.
        q"""
          sealed trait $name
          case class Foo(i: Int) extends $name
          case class Bar(s: String) extends $name
          case object Baz extends $name
        """
      )
      // Add validation and error handling here.
    }
  }
}

import scala.annotation.compileTimeOnly
// Add constructor arguments here.
@compileTimeOnly("enable macro paradise to expand macro annotations")
class expand extends StaticAnnotation {
  def macroTransform(annottees: Any*) = macro Expander.expand_impl
}
