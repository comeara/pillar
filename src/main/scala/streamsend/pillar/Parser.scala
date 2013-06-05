package streamsend.pillar

import java.util.Date
import java.io.InputStream
import scala.collection.mutable

object Parser {
  def apply(): Parser = new Parser

  private val MatchAttribute = """^-- (authoredAt|description|up|down):(.*)$""".r
}

class PartialMigration {
  var description: String = ""
  var authoredAt: String = ""
  var up = new mutable.MutableList[String]()
  var down: Option[mutable.MutableList[String]] = None

  def validate: Option[Map[String, String]] = {
    val errors = mutable.Map[String, String]()

    if (description.isEmpty) errors("description") = "must be present"
    if (authoredAt.isEmpty) errors("authoredAt") = "must be present"
    if (!authoredAt.isEmpty && authoredAtAsLong < 1) errors("authoredAt") = "must be a number greater than zero"
    if (up.isEmpty) errors("up") = "must be present"

    if (!errors.isEmpty) Some(errors.toMap) else None
  }

  def authoredAtAsLong: Long = {
    try {
      authoredAt.toLong
    } catch {
      case _:NumberFormatException => -1
    }
  }

}

class Parser {

  import Parser.MatchAttribute

  trait ParserState

  case object ParsingAttributes extends ParserState

  case object ParsingUp extends ParserState

  case object ParsingDown extends ParserState

  def parse(resource: InputStream): Migration = {
    val inProgress = new PartialMigration
    var state: ParserState = ParsingAttributes
    io.Source.fromInputStream(resource).getLines().foreach {
      line =>
        line match {
          case MatchAttribute("authoredAt", authoredAt) =>
            inProgress.authoredAt = authoredAt.trim
          case MatchAttribute("description", description) =>
            inProgress.description = description.trim
          case MatchAttribute("up", _) =>
            state = ParsingUp
          case MatchAttribute("down", _) =>
            inProgress.down = Some(new mutable.MutableList[String]())
            state = ParsingDown
          case cql =>
            if (!cql.isEmpty) {
              state match {
                case ParsingUp => inProgress.up += cql
                case ParsingDown => inProgress.down.get += cql
                case other => // ignored
              }
            }
        }
    }
    inProgress.validate match {
      case Some(errors) => throw new InvalidMigrationException(errors)
      case None =>
        inProgress.down match {
          case Some(downLines) =>
            if (downLines.isEmpty) {
              Migration(inProgress.description, new Date(inProgress.authoredAtAsLong), inProgress.up.mkString("\n"), None)
            } else {
              Migration(inProgress.description, new Date(inProgress.authoredAtAsLong), inProgress.up.mkString("\n"), Some(downLines.mkString("\n")))
            }
          case None => Migration(inProgress.description, new Date(inProgress.authoredAtAsLong), inProgress.up.mkString("\n"))
        }
    }
  }
}
