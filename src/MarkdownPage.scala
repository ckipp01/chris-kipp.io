package io.kipp.site

import com.vladsch.flexmark.ext.anchorlink.AnchorLinkExtension
import com.vladsch.flexmark.ext.yaml.front.matter.AbstractYamlFrontMatterVisitor
import com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterExtension
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.data.MutableDataSet
import com.vladsch.flexmark.util.misc.Extension

import java.util as ju
import scala.jdk.CollectionConverters.*
import com.vladsch.flexmark.ext.tables.TablesExtension

/** Representation of a markdown page.
  *
  * @param title
  *   Title of the page
  * @param date
  *   Date the page was written
  * @param updated
  *   Date if the page has been updated
  * @param description
  *   The description of the page
  * @param content
  *   The content of the page (basically the markup)
  * @param thumbnail
  *   Possible thumbnail to use for the post
  */
final case class MarkdownPage(
    title: String,
    date: String,
    updated: Option[String],
    description: String,
    content: String,
    thumbnail: Option[String]
):
  val urlify: String =
    title.replace("-", "").replace(" ", "-").replace("--", "-").toLowerCase()

object MarkdownPage:
  import Extensions.getOrLeft

  /** Given a path, read the file contents and extract what's needed to create a
    * page of markdown.
    *
    * @param path
    *   Where we're reading from
    * @return
    *   Either a parse error of some sort or the page.
    */
  def fromPath(path: os.Path): Either[String, MarkdownPage] =
    given os.Path = path

    val contents = os.read(path)
    val document = parser.parse(contents)
    val metadata =
      val visitor = new AbstractYamlFrontMatterVisitor()
      visitor.visit(document)
      visitor
        .getData()
        .asScala
        .toMap
        .collect:
          case (key, value) if value.asScala.toList.nonEmpty =>
            key -> value.asScala.toList.mkString

    val pageHtml = renderer.render(document)

    for
      title <- Right(metadata.getOrElse("title", path.baseName))
      date <- metadata.getOrLeft("date")
      description <- Right(
        metadata.getOrElse("description", "Blog of Chris Kipp")
      )
    yield MarkdownPage(
      title,
      date,
      metadata.get("updated"),
      description,
      pageHtml,
      metadata.get("thumbnail")
    )
  end fromPath

  private val extentsions: ju.Collection[Extension] =
    List(
      YamlFrontMatterExtension.create(),
      AnchorLinkExtension.create(),
      TablesExtension.create()
    ).asJavaCollection

  private val options = new MutableDataSet()
    .set(Parser.EXTENSIONS, extentsions)

  private val parser = Parser.builder(options).build()
  private val renderer = HtmlRenderer.builder(options).build()
end MarkdownPage
