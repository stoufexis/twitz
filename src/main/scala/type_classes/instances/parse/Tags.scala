package type_classes.instances.parse

import cats.Show

import model.Tags.*

import type_classes.{Read, Unwrap}

given Read[ClearChatTags]       = readClearChatTags
given Read[ClearMsgTags]        = readClearMsgTags
given Read[GlobalUserStateTags] = readGlobalUserStateTas
given Read[NoticeTags]          = readNoticeTags
given Read[PrivmsgTags]         = readPrivmsgTags
given Read[RoomStateTags]       = readRoomStateTags
given Read[UserStateTags]       = readUserStateTags
given Read[UserNoticeTags]      = readUserNoticeTags
given Read[WhisperTags]         = readWhisperTags

given Show[ClearChatTags]       = showClearChatTags
given Show[ClearMsgTags]        = showClearMsgTags
given Show[GlobalUserStateTags] = showGlobalUserStateTas
given Show[NoticeTags]          = showNoticeTags
given Show[PrivmsgTags]         = showPrivmsgTags
given Show[RoomStateTags]       = showRoomStateTags
given Show[UserStateTags]       = showUserStateTags
given Show[UserNoticeTags]      = showUserNoticeTags
given Show[WhisperTags]         = showWhisperTags
