package io.kipp.site

enum ListOf[A](
    val title: String,
    val description: String,
    val items: Set[A] | Set[Map[String, Set[A]]]
):
  case albums(
      override val title: String,
      override val description: String,
      override val items: Set[Album]
  ) extends ListOf[Album](title, description, items)

  case articles(
      override val title: String,
      override val description: String,
      override val items: Set[Map[String, Set[Article]]]
  ) extends ListOf[Article](title, description, items)

  case sites(
      override val title: String,
      override val description: String,
      override val items: Set[Site]
  ) extends ListOf[Site](title, description, items)

  case talks(
      override val title: String,
      override val description: String,
      override val items: Set[Talk]
  ) extends ListOf[Talk](title, description, items)

  case videos(
      override val title: String,
      override val description: String,
      override val items: Set[Map[String, Set[Video]]]
  ) extends ListOf[Video](title, description, items)

object ListOf:
  import io.circe.Json
  import io.circe.yaml.parser
  import io.circe.generic.auto.*

  def fromPath(
      path: os.Path
  ): Either[String, albums | articles | sites | talks | videos] =
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
  ): Either[String, albums | articles | sites | talks | videos] =
    val cursor = json.hcursor
    val title = cursor
      .downField("title")
      .as[String]

    // TODO is there a better way to do this?
    // and when I do that get rid of the `-Xmax-inlines`
    title match
      case Right("albums")   => json.as[albums].left.map(_.message)
      case Right("articles") => json.as[articles].left.map(_.message)
      case Right("sites")    => json.as[sites].left.map(_.message)
      case Right("talks")    => json.as[talks].left.map(_.message)
      case Right("videos")   => json.as[videos].left.map(_.message)
      case Right(value) => Left(s"${value} seems to be unmapped for decoded.")
      case Left(err)    => Left(err.getMessage)
