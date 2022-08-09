package model

import cats.Show

import model.AuxTypes.Channel

import common.*

import type_classes.*
import type_classes.Unwrap.unwrap

object Tags:
  opaque type ClearChatTags       = Map[String, String]
  opaque type ClearMsgTags        = Map[String, String]
  opaque type GlobalUserStateTags = Map[String, String]
  opaque type NoticeTags          = Map[String, String]
  opaque type UserNoticeTags      = Map[String, String]
  opaque type PrivmsgTags         = Map[String, String]
  opaque type RoomStateTags       = Map[String, String]
  opaque type UserStateTags       = Map[String, String]
  opaque type WhisperTags         = Map[String, String]

  type Tags =
    ClearChatTags | ClearMsgTags | GlobalUserStateTags |
      NoticeTags | UserNoticeTags | PrivmsgTags |
      RoomStateTags | UserStateTags | WhisperTags

  case class Extract[T <: Tags, A](key: String, f: String => Option[A])(using U: Unwrap[T, Map[String, String]]):
    def unapply(tags: T): Option[(A, T)] =
      Option(U.unwrap(tags))
        .flatMap { tg =>
          tg.get(key)
            .flatMap(f)
            .map((_, U.wrap(tg.removed(key))))
        }

  object ClearChatTags:
    def apply(map: Map[String, String]): ClearChatTags = map
    def unapply(string: String): Option[ClearChatTags] = readTagsF(string)

  object ClearMsgTags:
    def apply(map: Map[String, String]): ClearMsgTags = map
    def unapply(string: String): Option[ClearMsgTags] = readTagsF(string)

  object GlobalUserStateTags:
    def apply(map: Map[String, String]): GlobalUserStateTags = map
    def unapply(string: String): Option[GlobalUserStateTags] = readTagsF(string)

  object NoticeTags:
    def apply(map: Map[String, String]): NoticeTags = map
    def unapply(string: String): Option[NoticeTags] = readTagsF(string)

  object UserNoticeTags:
    def apply(map: Map[String, String]): UserNoticeTags = map
    def unapply(string: String): Option[UserNoticeTags] = readTagsF(string)

  object PrivmsgTags:
    def apply(map: Map[String, String]): PrivmsgTags = map
    def unapply(string: String): Option[PrivmsgTags] = readTagsF(string)

  object RoomStateTags:
    def apply(map: Map[String, String]): RoomStateTags = map
    def unapply(string: String): Option[RoomStateTags] = readTagsF(string)

  object UserStateTags:
    def apply(map: Map[String, String]): UserStateTags = map
    def unapply(string: String): Option[UserStateTags] = readTagsF(string)

  object WhisperTags:
    def apply(map: Map[String, String]): WhisperTags = map
    def unapply(string: String): Option[WhisperTags] = readTagsF(string)

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

  val unwrapClearChatTags: Unwrap[ClearChatTags, Map[String, String]]             = Unwrap.make(identity, identity)
  val unwrapClearMsgTags: Unwrap[ClearMsgTags, Map[String, String]]               = Unwrap.make(identity, identity)
  val unwrapGlobalUserStateTags: Unwrap[GlobalUserStateTags, Map[String, String]] = Unwrap.make(identity, identity)
  val unwrapNoticeTags: Unwrap[NoticeTags, Map[String, String]]                   = Unwrap.make(identity, identity)
  val unwrapUserNoticeTags: Unwrap[UserNoticeTags, Map[String, String]]           = Unwrap.make(identity, identity)
  val unwrapPrivmsgTags: Unwrap[PrivmsgTags, Map[String, String]]                 = Unwrap.make(identity, identity)
  val unwrapRoomStateTags: Unwrap[RoomStateTags, Map[String, String]]             = Unwrap.make(identity, identity)
  val unwrapUserStateTags: Unwrap[UserStateTags, Map[String, String]]             = Unwrap.make(identity, identity)
  val unwrapWhisperTags: Unwrap[WhisperTags, Map[String, String]]                 = Unwrap.make(identity, identity)
