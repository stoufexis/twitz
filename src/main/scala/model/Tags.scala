package model

import cats.Show

import common.*

import type_classes.*

object Tags:
  opaque type ClearChatTags       = String
  opaque type ClearMsgTags        = String
  opaque type GlobalUserStateTags = String
  opaque type NoticeTags          = String
  opaque type UserNoticeTags      = String
  opaque type PrivmsgTags         = String
  opaque type RoomStateTags       = String
  opaque type UserStateTags       = String
  opaque type WhisperTags         = String

  object ClearChatTags:
    def apply(map: Map[String, String]): ClearChatTags       = showTagsF(map)
    def unapply(string: String): Option[Map[String, String]] = readTagsF(string)

  object ClearMsgTags:
    def apply(map: Map[String, String]): ClearMsgTags        = showTagsF(map)
    def unapply(string: String): Option[Map[String, String]] = readTagsF(string)

  object GlobalUserStateTags:
    def apply(map: Map[String, String]): GlobalUserStateTags = showTagsF(map)
    def unapply(string: String): Option[Map[String, String]] = readTagsF(string)

  object NoticeTags:
    def apply(map: Map[String, String]): NoticeTags          = showTagsF(map)
    def unapply(string: String): Option[Map[String, String]] = readTagsF(string)

  object UserNoticeTags:
    def apply(map: Map[String, String]): UserNoticeTags      = showTagsF(map)
    def unapply(string: String): Option[Map[String, String]] = readTagsF(string)

  object PrivmsgTags:
    def apply(map: Map[String, String]): PrivmsgTags         = showTagsF(map)
    def unapply(string: String): Option[Map[String, String]] = readTagsF(string)

  object RoomStateTags:
    def apply(map: Map[String, String]): RoomStateTags       = showTagsF(map)
    def unapply(string: String): Option[Map[String, String]] = readTagsF(string)

  object UserStateTags:
    def apply(map: Map[String, String]): UserStateTags       = showTagsF(map)
    def unapply(string: String): Option[Map[String, String]] = readTagsF(string)

  object WhisperTags:
    def apply(map: Map[String, String]): WhisperTags         = showTagsF(map)
    def unapply(string: String): Option[Map[String, String]] = readTagsF(string)

  private val tagsToMap: String => Map[String, String] =
    _.split(";").flatMap {
      _.splitList("=") match
        case key :: value :: Nil if value.nonEmpty => Some((key, value))
        case _                                     => None
    }.toMap

  private val readTagsF: String => Option[Map[String, String]] =
    matchOption("@([^ ]+)".r)(_).map(tagsToMap)

  private val showTagsF: Map[String, String] => String =
    "@" ++ _.toList.map((k, v) => s"$k=$v").reduce(_ + ";" + _)