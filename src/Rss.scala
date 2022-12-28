package io.kipp.site

import scalatags.Text

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

import Text.all.*

object Rss:

  def generate(posts: Seq[BlogPost]) =
    val newest = posts.headOption
      .map(_.date)
      .getOrElse(Instant.now().toString())

    val t = (n: String) => Text.tags.tag(n)

    t("rss")(
      attr("version") := "2.0"
    )(
      t("channel")(
        t("title")("chris-kipp.io Blog"),
        t("link")("https://www.chris-kipp.io/blog"),
        t("description")(
          "Blog of Chris Kipp. Mostly thoughts on Scala tooling, Neovim, and music I like."
        ),
        t("lanugage")("en-us"),
        t("category")("Blog"),
        t("lastBuildDate")(convertToRFC822(newest)),
        posts.map { post =>
          val link = s"https://www.chris-kipp.io/blog/${post.urlify}"
          t("item")(
            t("title")(post.title),
            t("description")(post.content),
            t("link")(link),
            t("author")("Chris Kipp"),
            t("category")("Blog"),
            t("guid")(attr("isPermaLink") := true, link),
            t("pubDate")(convertToRFC822(post.date))
          )
        }
      )
    )

  private def convertToRFC822(date: String) =
    val localDate = LocalDate.parse(date)
    val zonedDate = localDate.atStartOfDay(ZoneId.of("Europe/Amsterdam"))
    val targetFormat = DateTimeFormatter.RFC_1123_DATE_TIME
    zonedDate.format(targetFormat)
