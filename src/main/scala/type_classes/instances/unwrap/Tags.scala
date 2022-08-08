package type_classes.instances.unwrap

import model.Tags.*

import type_classes.Unwrap

given Unwrap[ClearChatTags, Map[String, String]]       = unwrapClearChatTags
given Unwrap[ClearMsgTags, Map[String, String]]        = unwrapClearMsgTags
given Unwrap[GlobalUserStateTags, Map[String, String]] = unwrapGlobalUserStateTags
given Unwrap[NoticeTags, Map[String, String]]          = unwrapNoticeTags
given Unwrap[UserNoticeTags, Map[String, String]]      = unwrapUserNoticeTags
given Unwrap[PrivmsgTags, Map[String, String]]         = unwrapPrivmsgTags
given Unwrap[RoomStateTags, Map[String, String]]       = unwrapRoomStateTags
given Unwrap[UserStateTags, Map[String, String]]       = unwrapUserStateTags
given Unwrap[WhisperTags, Map[String, String]]         = unwrapWhisperTags
