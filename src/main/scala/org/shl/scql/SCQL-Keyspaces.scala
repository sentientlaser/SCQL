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


trait HasKeyspace {
  val keyspace$$:KeyspaceDecl
}

trait KeyspaceDecl extends Statement with SelfNamedObject {
  protected val renderedString$$ :String
  override def toString = renderedString$$
}

trait AlterKeyspace extends KeyspaceDecl {
  sealed trait ReplicationStrategy
  case class SimpleReplicationStrategy(val replicationFactor: Int = 1) extends ReplicationStrategy {
    override def toString = s"WITH REPLICATION = {'class' : 'SimpleStrategy', 'replication_factor' : ${replicationFactor}}"
  }
  case class NetworkTopologyStrategy(val datacenterFactors: List[(String, String)]) extends ReplicationStrategy {
    override def toString = {
      datacenterFactors.map { c => s"'${c}._1':${c}._2" }.mkString(", ")
      s"WITH REPLICATION = {'class' : 'SimpleStrategy', 'replication_factor' : ${datacenterFactors}}"
    }
  }
  protected val $$replicationStrategy: ReplicationStrategy
  protected val $$durableWrites: Boolean
  protected final lazy val $$durableWritesClause = if ($$durableWrites) "AND DURABLE_WRITES = true" else ""
  protected override lazy val renderedString$$ = $$minify(s"ALTER KEYSPACE ${name$$} ${$$replicationStrategy} ${$$durableWritesClause}")
}

trait CreateKeyspace extends AlterKeyspace with IfNotExistsClause{
  protected override lazy val renderedString$$ = $$minify(s"CREATE KEYSPACE ${ifNotExistsClause$$} ${name$$} ${$$replicationStrategy} ${$$durableWritesClause}")
}

trait DropKeyspace extends KeyspaceDecl {
  protected override lazy val renderedString$$ = $$minify(s"DROP KEYSPACE ${name$$}")
}
