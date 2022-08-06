package model

import cats.Show

import common.*

import type_classes.*
import type_classes.instances.parse.given

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

//  object PrivmsgTags:
//    def unapply

  private val tagsToMap: String => Map[String, String] =
    _.split(";").flatMap {
      _.splitList("=") match
        case key :: value :: Nil if value.nonEmpty => Some((key, value))
        case _                                     => None
    }.toMap

  private val readTagsF: String => Either[String, Map[String, String]] =
    matchEither("^@([^ ]+)".r, tagsToMap)(_)

  private val showTagsF: Map[String, String] => String =
    "@" ++ _.toList.map((k, v) => s"$k=$v").reduce(_ + ";" + _)

  val readClearChatTags: Read[ClearChatTags]            = readTagsF(_)
  val readClearMsgTags: Read[ClearMsgTags]              = readTagsF(_)
  val readGlobalUserStateTas: Read[GlobalUserStateTags] = readTagsF(_)
  val readNoticeTags: Read[NoticeTags]                  = readTagsF(_)
  val readPrivmsgTags: Read[PrivmsgTags]                = readTagsF(_)
  val readRoomStateTags: Read[RoomStateTags]            = readTagsF(_)
  val readUserStateTags: Read[UserStateTags]            = readTagsF(_)
  val readUserNoticeTags: Read[UserNoticeTags]          = readTagsF(_)
  val readWhisperTags: Read[WhisperTags]                = readTagsF(_)

  val showClearChatTags: Show[ClearChatTags]            = showTagsF(_)
  val showClearMsgTags: Show[ClearMsgTags]              = showTagsF(_)
  val showGlobalUserStateTas: Show[GlobalUserStateTags] = showTagsF(_)
  val showNoticeTags: Show[NoticeTags]                  = showTagsF(_)
  val showPrivmsgTags: Show[PrivmsgTags]                = showTagsF(_)
  val showRoomStateTags: Show[RoomStateTags]            = showTagsF(_)
  val showUserStateTags: Show[UserStateTags]            = showTagsF(_)
  val showUserNoticeTags: Show[UserNoticeTags]          = showTagsF(_)
  val showWhisperTags: Show[WhisperTags]                = showTagsF(_)

  val unwrapClearChatTags: Unwrap[ClearChatTags, Map[String, String]]            = identity(_)
  val unwrapClearMsgTags: Unwrap[ClearMsgTags, Map[String, String]]              = identity(_)
  val unwrapGlobalUserStateTas: Unwrap[GlobalUserStateTags, Map[String, String]] = identity(_)
  val unwrapNoticeTags: Unwrap[NoticeTags, Map[String, String]]                  = identity(_)
  val unwrapPrivmsgTags: Unwrap[PrivmsgTags, Map[String, String]]                = identity(_)
  val unwrapRoomStateTags: Unwrap[RoomStateTags, Map[String, String]]            = identity(_)
  val unwrapUserStateTags: Unwrap[UserStateTags, Map[String, String]]            = identity(_)
  val unwrapUserNoticeTags: Unwrap[UserNoticeTags, Map[String, String]]          = identity(_)
  val unwrapWhisperTags: Unwrap[WhisperTags, Map[String, String]]                = identity(_)
