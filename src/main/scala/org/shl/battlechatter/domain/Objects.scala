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

import java.net.URL
import java.time.{ZoneId, ZonedDateTime}
import java.util.UUID


case class UserPrinciple(
                          username: String,
                          password: Option[Array[Byte]],
                          userId: UUID = UUID.randomUUID(),
                          saltProvider: UUID => Array[Byte] = User.saltProvider
                        ) {
//  def this(
//            username: String,
//            password: Option[String],
//            id: UUID = UUID.randomUUID(),
//            saltProvider: UUID => Array[Byte] = User.saltProvider
//          ) = this(username, password.map(_.getBytes(Domain.UTF8)), id, saltProvider)
}

case object User {
  val saltProvider: UUID => Array[Byte] = { in: UUID =>
    Range(0, 128).map{_.asInstanceOf[Byte]}.toArray[Byte] // scalastyle:ignore null
  }
}

case class UserProfile(
                        override val id: UUID, // Doubles userid
                        homeTZ: ZoneId,
                        email:Types.Email,
                        avatar:URL,
                        bio:Types.MarkdownString = "",
                        override val iid: UUID = UUID.randomUUID()
                      ) extends UniqueInstanceId

case class Post(
                 override val id: UUID,
                 override val iid: UUID,
                 userID: UUID,
                 text: Types.MarkdownString,
                 timestamp: ZonedDateTime = ZonedDateTime.now(Domain.UTC),
                 parentId: UUID = Domain.rootId
               ) extends UniqueInstanceId {

}

abstract class Reaction(
                         override val id: UUID,
                         override val iid: UUID,
                         val userId:UUID,
                         val postId:UUID,
                         val valence:Int) extends UniqueInstanceId

case class UpVote(
                   override val id: UUID,
                   override val iid: UUID,
                   override val userId: UUID,
                   override val postId: UUID
                 ) extends Reaction(id, iid, userId, postId, 1)

case class DownVote(
                     override val id: UUID,
                     override val iid: UUID,
                     override val userId: UUID,
                     override val postId: UUID
                   ) extends Reaction(id, iid, userId, postId, -1)


case class MehVote(
                    override val id: UUID,
                    override val iid: UUID,
                    override val userId: UUID,
                    override val postId: UUID
                  ) extends Reaction(id, iid, userId, postId, 0)


