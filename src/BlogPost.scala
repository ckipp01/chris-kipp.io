package io.kipp.site

import com.vladsch.flexmark.ext.anchorlink.AnchorLinkExtension
import com.vladsch.flexmark.ext.yaml.front.matter.AbstractYamlFrontMatterVisitor
import com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterExtension
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.data.MutableDataSet
import com.vladsch.flexmark.util.misc.Extension

import java.{util => ju}
import scala.jdk.CollectionConverters.*
import com.vladsch.flexmark.ext.tables.TablesExtension

final case class BlogPost(
    title: String,
    date: String,
    updated: Option[String],
    description: String,
    content: String,
    thumbnail: Option[String]
):
  val urlify =
    title.replace("-", "").replace(" ", "-").replace("--", "-").toLowerCase()

object BlogPost:
  def apply(path: os.Path): BlogPost =
    val contents = os.read(path)
    val document = parser.parse(contents)
    val metadata = {
      val visitor = new AbstractYamlFrontMatterVisitor()
      visitor.visit(document)
      visitor.getData().asScala.toMap.collect {
        case (key, value) if value.asScala.toList.nonEmpty =>
          key -> value.asScala.toList.mkString
      }
    }

    val articleHtml = renderer.render(document)

    BlogPost(
      title = metadata.getOrElse(
        "title",
        throw new RuntimeException(s"Missing title in ${path}")
      ),
      date = metadata.getOrElse(
        "date",
        throw new RuntimeException(s"Missing date in ${path}")
      ),
      updated = metadata.get("updated"),
      description = metadata.getOrElse(
        "description",
        throw new RuntimeException(s"Missing description in ${path}")
      ),
      content = articleHtml,
      thumbnail = metadata.get("thumbnail")
    )

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
