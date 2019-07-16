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

import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.cql.{BoundStatement, PreparedStatement}


import scala.reflect.ClassTag
import scala.reflect.classTag
import scala.util.parsing.combinator.RegexParsers

trait Statement {
  /**
    * the `$$` prefix is to prevent any possible conflict with objects and clauses that are intended to name schema
    * objects (because $$ is a poor choise for column names given it's a bad column name)
    *
    * @param s
    * @return
    */
  @inline def $$minify(s: String): String = s.stripMargin('|').trim.replaceAll("\\s+", " ")
  @inline def $$minify(x: (String, String)): (String, String) = ($$minify(x._1), $$minify(x._2))
}

trait IfNotExistsClause {
  protected val ifNotExists$$: Boolean = true
  protected final lazy val ifNotExistsClause$$ = if (ifNotExists$$) "IF NOT EXISTS" else ""
}


trait SelfNamedObject {
  lazy val name$$: String = this.getClass.getSimpleName.replaceAll("\\$$", "")
}

abstract class VersionedDecl(val version: String)
