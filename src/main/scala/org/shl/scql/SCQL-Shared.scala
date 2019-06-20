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

trait Statement {
  /**
    * the `$$` prefix is to prevent any possible conflict with objects and clauses that are intended to name schema
    * objects (because $$ is a poor choise for column names given it's a bad column name)
    * @param s
    * @return
    */
  @inline def $$minify(s: String): String = s.stripMargin('|').trim.replaceAll("\\s+", " ")
  @inline def $$minify(x: (String, String)): (String, String) = ($$minify(x._1), $$minify(x._2))
}

trait UnionType{
  // Credit to Miles Sabin for this little bit of awesome.
  // https://milessabin.com/blog/2011/06/09/scala-union-types-curry-howard/

  def unexpected : Nothing = sys.error("Unexpected invocation")

  trait <:!<[A, B]

  // Uses ambiguity to rule out the cases we're trying to exclude
  implicit def nsub[A, B] : A <:!< B = null
  implicit def nsubAmbig1[A, B >: A] : A <:!< B = unexpected
  implicit def nsubAmbig2[A, B >: A] : A <:!< B = unexpected

  private type ¬[A] = A => Nothing
  private type ∨[T, U] = ¬[¬[T] with ¬[U]]
  private type ¬¬[A] = ¬[¬[A]]
  type |[T, U] = { type union[X] = ¬¬[X] <:< (T ∨ U) }



}

trait IfNotExistsClause {
  protected val $$ifNotExists: Boolean = true
  protected final lazy val $$ifNotExistsClause = if ($$ifNotExists) "IF NOT EXISTS" else ""
}

object NestedObjectReflector {
  import scala.reflect.runtime.universe
  val mirror = universe.runtimeMirror(getClass.getClassLoader)
}

trait NestedObjectReflector {

  import scala.reflect.runtime.universe.typeOf
  import scala.reflect.runtime.universe.TypeTag
  protected final val $$mirror = NestedObjectReflector.mirror

  protected final def $$self = $$mirror.reflect(this).symbol.typeSignature

  protected final def $$modules[T: TypeTag]:Set[T] = $$self
    .members
    .filter(_.isModule)
    .filter(_.typeSignature <:< typeOf[T])
    .map(_.asModule)
    .map($$mirror.reflectModule(_))
    .map(_.instance.asInstanceOf[T])
    .toSet
}

trait NamedSchemaObject {
  lazy val $$name:String = this.getClass.getSimpleName.replaceAll("\\$$", "") //TODO: better prefix for inner vals, so there is no namecollisions
}

