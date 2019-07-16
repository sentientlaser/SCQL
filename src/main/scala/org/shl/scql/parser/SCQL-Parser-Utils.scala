package org.shl.scql.parser

import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.cql.{BoundStatement, PreparedStatement}

import scala.reflect.{ClassTag, classTag}

object Utils {
    implicit class CQLString(protected val cql:String) {
      def execute(implicit session:CqlSession) = session.execute(cql)
    }

    implicit class PreparedCQLStatement(protected override val cql:String) extends CQLString(cql) {
      private var preparedStmtInternal:Option[PreparedStatement] = None
      private def preparedStmt_=(s:PreparedStatement) = preparedStmtInternal = Some(s)
      def preparedStmt = preparedStmtInternal.getOrElse(throw new Exception("Statement is not prepared"))

      def prepare(implicit session:CqlSession) = {
        preparedStmt = session.prepare(cql)
        preparedStmt
      }

      override def execute(implicit session: CqlSession) = session.execute(preparedStmt.bind())
    }

    implicit class BindableCqlString(protected override val cql:String) extends PreparedCQLStatement(cql) {
      var boundStmtInternal:Option[BoundStatement] = None
      private def boundStmt_=(b:BoundStatement) = boundStmtInternal = Some(b)
      def boundStmt = boundStmtInternal.getOrElse(throw new Exception("Statement is not bound"))


      def bind(args:List[Any])(implicit session:CqlSession) = {
        def bind[T :ClassTag](stmt:BoundStatement, binding:(T, Int)) ={
          val c = classTag[T]
          val (value, ind) = binding
          boundStmt.set(ind + 1, value, c.runtimeClass)
        }
        boundStmt = args.zipWithIndex.foldLeft(preparedStmt.bind())(bind)
        boundStmt
      }

      override def execute(implicit session: CqlSession) = session.execute(boundStmt)
    }

    implicit class CQLStringContext(private val sc: StringContext) extends AnyVal {
      def cql(args: Any *) = sc.s(args)

    }

}
