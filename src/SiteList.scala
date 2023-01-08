package io.kipp.site

import io.circe.Json

sealed trait SiteList[A]:
  def title: String
  def description: String
  def items: Set[A] | Set[Map[String, Set[A]]]

object SiteList:
  import io.circe.yaml.parser
  import io.circe.generic.auto.*

  def fromPath(path: os.Path) =
    val contents = os.read(path)
    val json = parser.parse(contents)
    json match
      case Left(err) =>
        scribe.error(err.getMessage)
        // TODO alright before it was fine, but now we are throwing all over the place, let's
        // probably refactor all this out switch to an Either
        throw new RuntimeException(
          s"Unable to correctly decode ${path.baseName} from yaml to json, so quitting."
        )
      case Right(json) =>
        toList(json) match
          case Left(err) =>
            scribe.error(err.getMessage)
            throw new RuntimeException(
              s"puking when trying to go from json to your case class in ${path.baseName}"
            )
          case Right(value) => value

  private def toList(json: Json) =
    val cursor = json.hcursor
    val title = cursor
      .downField("title")
      .as[String]
      .getOrElse(throw new RuntimeException("missing title"))

    // TODO this is dumb, make this whole ADT into an enumm
    // and when I do that get rid of the `-Xmax-inlines`
    title match
      case "albums"   => json.as[Albums]
      case "articles" => json.as[Articles]
      case "sites"    => json.as[Sites]
      case "talks"    => json.as[Talks]
      case "videos"   => json.as[Videos]

final case class Albums(title: String, description: String, items: Set[Album])
    extends SiteList[Album]

final case class Articles(
    title: String,
    description: String,
    items: Set[Map[String, Set[Article]]]
) extends SiteList[Article]

final case class Sites(title: String, description: String, items: Set[Site])

final case class Talks(title: String, description: String, items: Set[Talk])
    extends SiteList[Talk]

final case class Videos(
    title: String,
    description: String,
    items: Set[Map[String, Set[Video]]]
) extends SiteList[Video]
