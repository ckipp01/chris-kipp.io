package io.kipp.site

import scalatags.Text.all.*
import scalatags.Text.tags2
import scalatags.Text.TypedTag

sealed trait SiteList:
  def id: String
  def title: String
  def description: String
  def items: Seq[ListItem] | Seq[Map[String, Seq[ListItem]]]
  def renderHtml(): Seq[TypedTag[String]]

object SiteList:
  import io.circe.Json
  import io.circe.yaml.parser
  import io.circe.generic.auto.*

  def fromPath(
      path: os.Path
  ): Either[String, SiteList] =
    val contents = os.read(path)
    val json = parser.parse(contents)

    json match
      case Left(err) =>
        scribe.error(s"Choked when processing ${path}")
        Left(err.getMessage)
      case Right(json) =>
        toListOf(json) match
          case Left(err) =>
            scribe.error(s"Choked when processing ${path}")
            Left(err)
          case Right(value) => Right(value)

  private def toListOf(
      json: Json
  ): Either[String, SiteList] =
    val cursor = json.hcursor
    val id = cursor
      .downField("id")
      .as[String]

    // TODO is there a better way to do this?
    // and when I do that get rid of the `-Xmax-inlines`
    id match
      case Right("albums")   => json.as[Albums].left.map(_.message)
      case Right("articles") => json.as[Articles].left.map(_.message)
      case Right("sites")    => json.as[Sites].left.map(_.message)
      case Right("talks")    => json.as[Talks].left.map(_.message)
      case Right("videos")   => json.as[Videos].left.map(_.message)
      case Right(value) => Left(s"${value} seems to be unmapped for decoded.")
      case Left(err)    => Left(err.getMessage)

final case class Albums(
    id: String,
    title: String,
    description: String,
    items: Seq[Album]
) extends SiteList:
  override def renderHtml(): Seq[TypedTag[String]] =
    Seq(h1(title), p(description)) ++ items.map { album =>
      div(
        Style.album,
        img(
          src := s"../images/albums/${album.album.replace(" ", "-").toLowerCase()}.jpeg"
        ),
        div(
          Style.albumDescription,
          // TODO remove inline styling
          p(marginBottom := 0, b("Band: "), album.artist),
          b("Album: "),
          a(
            href := album.link,
            target := "_blank",
            album.album
          ),
          p(marginBottom := 0, b("Favorite song: "), album.`favorite-song`)
        ),
        div(
          (1 to album.rating).map(_ =>
            img(Style.star, src := "../images/star.svg")
          )
        )
      )
    }

final case class Articles(
    id: String,
    title: String,
    description: String,
    items: Seq[Map[String, Seq[Article]]]
) extends SiteList:
  override def renderHtml(): Seq[TypedTag[String]] = items.map(article => div())

final case class Sites(
    id: String,
    title: String,
    description: String,
    items: Seq[Site]
) extends SiteList:
  override def renderHtml(): Seq[TypedTag[String]] = items.map(site => div())

final case class Talks(
    id: String,
    title: String,
    description: String,
    items: Seq[Talk]
) extends SiteList:
  override def renderHtml(): Seq[TypedTag[String]] =
    items.map { talk =>
      div(
        Style.largeFontOverview,
        p(talk.title),
        span(
          a(
            borderBottomStyle.none,
            href := talk.place.link,
            target := "_blank",
            talk.place.name
          ),
          " | ",
          a(
            borderBottomStyle.none,
            href := s"slides/${talk.slides}",
            target := "_blank",
            "slides"
          ),
          talk.video
            .map[scalatags.Text.Modifier] { vid =>
              Seq(
                stringFrag(" | "),
                a(
                  borderBottomStyle.none,
                  rel := "me noopener noreferrer",
                  target := "_blank",
                  href := vid,
                  "video"
                )
              )
            }
            .getOrElse(Seq.empty[scalatags.Text.Modifier])
        )
      )
    }

final case class Videos(
    id: String,
    title: String,
    description: String,
    items: Seq[Map[String, Seq[Video]]]
) extends SiteList:
  override def renderHtml(): Seq[TypedTag[String]] = items.map(video => div())
