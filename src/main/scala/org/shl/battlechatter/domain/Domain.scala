// Copyright (C) 2018-2019 the original author or authors.
// See the LICENCE.txt file distributed with this work for additional
// information regarding copyright ownership.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.shl.battlechatter.domain

import java.nio.charset.Charset
import java.time.{Instant, ZoneId}
import java.util.UUID

import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension
import com.vladsch.flexmark.ext.tables.TablesExtension
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.data.MutableDataSet
import org.owasp.validator.html.{AntiSamy, Policy}

import scala.collection.JavaConverters._

object MarkdownRenderer {
  private val settings = new MutableDataSet {
    set(Parser.EXTENSIONS, List(
      TablesExtension.create,
      StrikethroughExtension.create).asJava
    )
    //set(HtmlRenderer.SOFT_BREAK, "<br />\n")
  }

  var renderer: HtmlRenderer = HtmlRenderer.builder(settings).build()

  val parser: Parser = Parser.builder(settings).build

  def apply(s: String): String = renderer.render(parser.parse(s))
}

object HtmlValidation {
  private val policyFileName = "antisamy-slashdot-1.4.4.xml"
  val policy = Policy.getInstance(this.getClass().getResourceAsStream(policyFileName))
  val antisamy = new AntiSamy

  def apply(s: String): (String, List[String]) = {
    val res = antisamy.scan(s, policy)
    (res.getCleanHTML, res.getErrorMessages.asScala.toList)
  }
}

object MarkdownVerifier {
}

/**
  * Contains constants and shared resources: standard caveats of use apply, not threadsafe, etc etc
  */
object Domain {
  val UTC = ZoneId.of("UTC")
  val UTF8 = Charset.forName("UTF-8")
  val rootId = UUID.randomUUID()
}

object Types {

  implicit class MarkdownString(val text: String) {
    val (html, errors) = HtmlValidation(MarkdownRenderer(text))
  }

  type Email = javax.mail.internet.InternetAddress
}


trait UniqueId[T <: UniqueId[T]] {
  val id: UUID
  val timestamp: Instant

  def newid = UUID.randomUUID

  def setID: T

  def setTimestamp: T

  def prep: T = {
    verify()
    (if (id == null) this.setID else this.asInstanceOf[T]).setTimestamp
  }

  def verify(): Unit = {}
}

trait UniqueInstanceId[T <: UniqueInstanceId[T]] extends UniqueId[T] {
  val iid: UUID

  def setIID: T

  override def prep: T = super[UniqueId].prep.setIID
}

trait PersistenceOp[T <: UniqueId[T]] {
  // todo: add logging, etc

  protected def doSave: T = {
    this.asInstanceOf[T]
  }
}

trait Savable[T <: UniqueId[T]] extends PersistenceOp[T] with UniqueId[T] {
  def save: T = this.prep.setTimestamp.asInstanceOf[Savable[T]].doSave
}

trait Deletable[T <: Deletable[T]] extends PersistenceOp[T] with UniqueId[T] {
  val deleted: Boolean = false

  def setDeleted: T

  override def prep: T = super.prep.setDeleted
}

trait Retrievable[T <: UniqueId[T], ST] {
  def apply(u: UUID)
  def apply(search: ST)
}

trait CassandraDecl {
  val updates: Map[Int, (String, String)]
}

