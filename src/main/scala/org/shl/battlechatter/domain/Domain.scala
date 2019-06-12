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
import java.time.ZoneId
import java.util.UUID

import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension
import com.vladsch.flexmark.ext.tables.TablesExtension
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.data.MutableDataSet
import org.owasp.validator.html.{Policy, AntiSamy}

import scala.collection.JavaConverters._

object Test extends App {
  println("foo")
}

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

  def apply(s:String) = renderer.render(parser.parse(s))
}

object HtmlValidation {
  private val policyFileName = "antisamy-slashdot-1.4.4.xml"
  val policy = Policy.getInstance(this.getClass().getResourceAsStream(policyFileName))
  val antisamy = new AntiSamy

  def apply(s:String) = {
    val res = antisamy.scan(s, policy)
    (res.getCleanHTML, res.getErrorMessages.asScala)
  }
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
    Markdown.parser.parse("This is *Sparta*") // TODO: add a test failure here.
  }

  type Email = javax.mail.internet.InternetAddress
}

trait UniqueId {
  val id: UUID
}

trait UniqueInstanceId extends UniqueId {
  val iid: UUID
}

trait PersistenceOp[T <:UniqueId] {
  // todo: add logging, etc
}

trait Saves[T <:UniqueId] extends PersistenceOp[T] {
  def save:T
}

trait SavesOverwrite [T >: UniqueInstanceId <: UniqueId ] extends PersistenceOp[T] {
  def save:T
}

trait SavesUpdate [T <: UniqueInstanceId] extends PersistenceOp[T] {
  def save:T
}


trait Deletes[T <: UniqueId] extends PersistenceOp[T] {
  def delete:Boolean
  val deleted:Boolean
}

trait Retrieves[T <: UniqueId] extends PersistenceOp[T]{

}