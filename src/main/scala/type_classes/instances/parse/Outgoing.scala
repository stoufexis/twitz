package type_classes.instances.parse

import cats.Show
import cats.syntax.all.*

import model.Outgoing

import Outgoing.*

given Show[Outgoing] =
  case Outgoing.PRIVMSG(replyTo, channel, body) => show"$replyTo PRIVMSG $channel $body"
  case Outgoing.PONG(body)                      => show"PONG $body"
  case Outgoing.NICK(channel)                   => show"NICK $channel"
  case Outgoing.JOIN(channel)                   => show"JOIN $channel"
