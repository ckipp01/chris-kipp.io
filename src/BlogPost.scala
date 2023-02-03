package io.kipp.site

import com.vladsch.flexmark.ext.anchorlink.AnchorLinkExtension
import com.vladsch.flexmark.ext.yaml.front.matter.AbstractYamlFrontMatterVisitor
import com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterExtension
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.data.MutableDataSet
import com.vladsch.flexmark.util.misc.Extension

import java.{util as ju}
import scala.jdk.CollectionConverters.*
import com.vladsch.flexmark.ext.tables.TablesExtension

/** Representation of a blog post.
  *
  * @param title
  *   Title of the post
  * @param date
  *   Date the post was written
  * @param updated
  *   Date if the post has been updated
  * @param description
  *   The description of the post
  * @param content
  *   The content of the post (basically the markup)
  * @param thumbnail
  *   Possible thumbnail to use for the post
  */
final case class BlogPost(
    title: String,
    date: String,
    updated: Option[String],
    description: String,
    content: String,
    thumbnail: Option[String]
):
  val urlify: String =
    title.replace("-", "").replace(" ", "-").replace("--", "-").toLowerCase()

object BlogPost:
  import Extensions.getOrLeft

  /** Given a path, read the file contents and extract what's needed to create a
    * BlogPost.
    *
    * @param path
    *   Where we're reading from
    * @return
    *   either a parse error of some sort or the BlogPost
    */
  def fromPath(path: os.Path): Either[String, BlogPost] =
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

    val articleHtml = renderer.render(document)

    for
      title <- metadata.getOrLeft("title")
      date <- metadata.getOrLeft("date")
      description <- metadata.getOrLeft("description")
    yield BlogPost(
      title,
      date,
      metadata.get("updated"),
      description,
      articleHtml,
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
end BlogPost
