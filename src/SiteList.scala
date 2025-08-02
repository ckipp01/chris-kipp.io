package io.kipp.site

import scalatags.Text.TypedTag
import scalatags.Text.all.*

sealed trait SiteList:
  def id: String
  def title: String
  def description: String
  def items: Seq[ListItem] | Map[String, Seq[ListItem]]
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
        scribe.error(s"Choked when processing ${path} to json")
        Left(err.getMessage)
      case Right(json) =>
        toListOf(json) match
          case Left(err) =>
            scribe.error(s"Choked when processing ${path} to it's case class")
            Left(err)
          case Right(value) => Right(value)

  private def toListOf(
      json: Json
  ): Either[String, SiteList] =
    val cursor = json.hcursor
    val id = cursor
      .downField("id")
      .as[String]

    id match
      case Right("albums")   => json.as[Albums].left.map(_.getMessage)
      case Right("articles") => json.as[Articles].left.map(_.getMessage)
      case Right("sites")    => json.as[Sites].left.map(_.getMessage)
      case Right("talks")    => json.as[Talks].left.map(_.getMessage)
      case Right("videos")   => json.as[Videos].left.map(_.getMessage)
      case Right(value) => Left(s"${value} seems to be unmapped for decoded.")
      case Left(err)    => Left(err.getMessage)

final case class Albums(
    id: String,
    title: String,
    description: String,
    items: Seq[Album]
) extends SiteList:
  override def renderHtml(): Seq[TypedTag[String]] =
    Seq(h1(title), p(description), hr) ++ items.map { album =>
      div(
        Style.album,
        img(
          src := s"../images/albums/${album.albumImageName}",
          attr("loading") := "lazy"
        ),
        div(
          Style.albumDescription,
          p(b("Band: "), album.artist),
          p(
            b("Album: "),
            a(
              href := album.link,
              target := "_blank",
              album.album
            )
          ),
          album.`favorite-song`
            .map(song => p(marginBottom := 0, b("Favorite song: "), song))
            .getOrElse("")
        ),
        div(
          Style.albumRating,
          (1 to album.rating).map: _ =>
            img(Style.star, src := "../images/star.svg")
        )
      )
    }

final case class Articles(
    id: String,
    title: String,
    description: String,
    items: Map[String, Seq[Article]]
) extends SiteList:
  override def renderHtml(): Seq[TypedTag[String]] =
    Seq(h1(title), p(description)) ++ items.toSeq
      .sortBy(_._1)
      .map: (topic, articles) =>
        div(
          Style.article,
          h2(topic),
          articles.map: article =>
            div(
              a(href := article.link, target := "_blank", article.title),
              p(article.author)
            )
        )

final case class Sites(
    id: String,
    title: String,
    description: String,
    items: Seq[Site]
) extends SiteList:
  override def renderHtml(): Seq[TypedTag[String]] =
    Seq(h1(title), p(description), hr) ++ items.map: site =>
      div(
        Style.sites,
        a(href := site.url, target := "_blank", site.url),
        p(site.owner)
      )

final case class Talks(
    id: String,
    title: String,
    description: String,
    items: Seq[Talk]
) extends SiteList:
  override def renderHtml(): Seq[TypedTag[String]] =
    items.map: talk =>
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
            href := s"../slides/${talk.slides}",
            target := "_blank",
            "slides"
          ),
          talk.video
            .map[scalatags.Text.Modifier]: vid =>
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
            .getOrElse(Seq.empty[scalatags.Text.Modifier])
        )
      )

final case class Videos(
    id: String,
    title: String,
    description: String,
    items: Map[String, Seq[Video]]
) extends SiteList:
  override def renderHtml(): Seq[TypedTag[String]] =
    Seq(h1(title), p(description)) ++ items.toSeq
      .sortBy(_._1)
      .map: (topic, videos) =>
        div(
          Style.article,
          h2(topic),
          videos.map: video =>
            div(
              a(href := video.link, target := "_blank", video.title),
              p(video.author)
            )
        )
