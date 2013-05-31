package streamsend.pillar

import java.util.Date
import java.io.InputStream
import scala.collection.mutable

object MigrationParser {
  def apply(): MigrationParser = new MigrationParser

  private val MatchAttribute = """^-- (authoredAt|description|up|down):(.*)$""".r
}

class PartialMigration {
  var description: String = _
  var authoredAt: Date = _
  var up = new mutable.MutableList[String]()
  var down: Option[mutable.MutableList[String]] = None
}

class MigrationParser {

  import MigrationParser.MatchAttribute

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
            inProgress.authoredAt = new Date(authoredAt.trim.toInt)
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
    inProgress.down match {
      case Some(downLines) =>
        if (downLines.isEmpty) {
          Migration(inProgress.description, inProgress.authoredAt, inProgress.up.mkString("\n"), None)
        } else {
          Migration(inProgress.description, inProgress.authoredAt, inProgress.up.mkString("\n"), Some(downLines.mkString("\n")))
        }
      case None => Migration(inProgress.description, inProgress.authoredAt, inProgress.up.mkString("\n"))
    }
  }
}
