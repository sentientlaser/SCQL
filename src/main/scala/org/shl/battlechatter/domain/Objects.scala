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
import java.nio.ByteBuffer
import java.security.{MessageDigest, SecureRandom}

import java.util.UUID
import java.time.{Instant, ZoneId}


case class UserPrinciple(
  override val id: UUID,
  override val timestamp: Instant,
  username: String,
  password: String,
  digestedPassword: Array[Byte]
) extends UniqueId[UserPrinciple] with Savable[UserPrinciple] {
  override def setID = this.copy(id = newid)
  override def setTimestamp = this.copy(timestamp = Instant.now())
  override def prep = super[UniqueId].prep.digest
  def digest = this.copy(digestedPassword = UserPrinciple.digest(id, password.getBytes()), password = null)
}

object UserPrinciple {
  private def saltProvider(in: UUID) = Range(0, 128)
    .map {_.asInstanceOf[Byte]}
    .toArray[Byte]

  private val digester = MessageDigest.getInstance(s"SHA-${Salt.saltWidth}")
  def digest(userId: UUID, pass: Array[Byte]) = digester.digest(pass ++ saltProvider(userId))
}


case class Salt(
  override val id: UUID,
  override val timestamp: Instant,
  salt: ByteBuffer = Salt()
) extends UniqueId[Salt] with Savable[Salt] {
  override def setID = this.copy(id = newid)
  override def setTimestamp = this.copy(timestamp = Instant.now())
}

object Salt {
  private val secRand = SecureRandom.getInstanceStrong
  val saltWidth = 512
  def apply() = {
    val ra = new Array[Byte](saltWidth)
    secRand.nextBytes(ra)
    ByteBuffer.wrap(ra)
  }
}


//case class UserProfile(
//  override val id: UUID, // Doubles userid
//  override val iid: UUID,
//  override val timestamp: Instant,
//  homeTZ: ZoneId,
//  email: Types.Email,
//  avatar: URL,
//  bio: Types.MarkdownString = "",
//) extends UniqueInstanceId[UserProfile] {
//  override def setIID: UserProfile = this.copy(iid = newid)
//  override def setID: UserProfile = this.copy(id = newid)
//  override def setTimestamp: UserProfile = this.copy(timestamp = Instant.now())
//}

//case class Post(
//                 override val id: UUID,
//                 override val iid: UUID,
//                 userID: UUID,
//                 text: Types.MarkdownString,
//                 timestamp: ZonedDateTime = ZonedDateTime.now(Domain.UTC),
//                 parentId: UUID = Domain.rootId
//               ) extends UniqueInstanceId {
//
//}
//
//abstract class Reaction(
//                         override val id: UUID,
//                         override val iid: UUID,
//                         val userId:UUID,
//                         val postId:UUID,
//                         val valence:Int) extends UniqueInstanceId
//
//case class UpVote(
//                   override val id: UUID,
//                   override val iid: UUID,
//                   override val userId: UUID,
//                   override val postId: UUID
//                 ) extends Reaction(id, iid, userId, postId, 1)
//
//case class DownVote(
//                     override val id: UUID,
//                     override val iid: UUID,
//                     override val userId: UUID,
//                     override val postId: UUID
//                   ) extends Reaction(id, iid, userId, postId, -1)
//
//
//case class MehVote(
//                    override val id: UUID,
//                    override val iid: UUID,
//                    override val userId: UUID,
//                    override val postId: UUID
//                  ) extends Reaction(id, iid, userId, postId, 0)
//

