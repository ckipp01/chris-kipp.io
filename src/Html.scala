package io.kipp.site

import scalatags.Text.all.*
import scalatags.Text.tags2

object Html:

  def blogPage(blogPost: MarkdownPage) =
    htmlWrapper(
      headFrag(
        pageTitle = blogPost.title,
        description = blogPost.description,
        thumbnail = blogPost.thumbnail
      ),
      body(
        Style.bodyBase,
        headerFrag(blogPost.title),
        tags2.main(
          Style.writing,
          Style.tableBase,
          // TODO maybe the date here
          raw(blogPost.content)
        ),
        footerFrag()
      )
    )

  def blogOverview(blogPosts: Seq[MarkdownPage]) =
    htmlWrapper(
      headFrag(
        pageTitle = "chris-kipp.io - blog",
        description =
          "A collection of blogs posts by Chris Kipp over the years."
      ),
      body(
        Style.bodyBase,
        headerFrag("blog"),
        tags2.main(
          Style.tableBase,
          Style.largeFontOverview,
          blogPosts.map: blogPost =>
            div(
              Style.blogListing,
              a(href := s"./blog/${blogPost.urlify}")(blogPost.title),
              span(blogPost.date)
            )
        ),
        footerFrag()
      )
    )

  def talksOverview(talks: Talks) =
    htmlWrapper(
      headFrag(
        pageTitle = s"chris-kipp.io - ${talks.title}",
        description = talks.description
      ),
      body(
        Style.bodyBase,
        headerFrag("talks"),
        tags2.main(
          Style.talkListing,
          talks.renderHtml()
        ),
        footerFrag()
      )
    )

  def listsOverview(lists: Seq[SiteList]) =
    htmlWrapper(
      headFrag(
        pageTitle = "chris-kipp.io - lists",
        description =
          "A collection of lists and links that I want to refer back on."
      ),
      body(
        Style.bodyBase,
        headerFrag("lists"),
        tags2.main(
          Style.largeFontOverview,
          lists.map: list =>
            div(
              Style.blogListing,
              a(href := s"./lists/${list.id}")(list.title),
              span(list.description)
            )
        ),
        footerFrag()
      )
    )

  def listPage(list: SiteList) =
    htmlWrapper(
      headFrag(
        pageTitle = s"chris-kipp.io - ${list.id}",
        description = list.description
      ),
      body(
        Style.bodyBase,
        headerFrag("lists"),
        tags2.main(
          Style.headings,
          Style.linkBase,
          Style.linkHoverBase,
          list.renderHtml()
        ),
        footerFrag()
      )
    )

  def custom404() =
    htmlWrapper(
      headFrag(
        pageTitle = "chris-kipp.io - 404",
        description = "Page not found"
      ),
      body(
        Style.bodyBase,
        headerFrag("404"),
        tags2.main(
          Style.writing,
          h1("Page not found"),
          p(
            "Please report this ",
            a(
              href := "https://github.com/ckipp01/chris-kipp.io/issues",
              "here."
            )
          )
        )
      )
    )

  private def htmlWrapper(content: scalatags.Text.Modifier*) =
    doctype("html")(html(lang := "en", Style.root, content))

  private def headFrag(
      pageTitle: String,
      description: String,
      thumbnail: Option[String] = None
  ) =
    head(
      meta(charset := "utf-8"),
      meta(
        name := "viewport",
        content := "width=device-width, initial-scale=1"
      ),
      meta(name := "description", content := description),
      meta(
        name := "keywords",
        content := "Chris Kipp, ckipp01, ckipp, Scala, Developer Tooling"
      ),
      meta(
        name := "thumbnail",
        content := s"../images/${thumbnail.getOrElse("me-cream.png")}"
      ),
      meta(
        name := "og:type",
        content := "website"
      ),
      meta(
        name := "og:title",
        content := pageTitle
      ),
      meta(
        name := "og:image",
        content := s"../images/${thumbnail.getOrElse("me-cream.png")}"
      ),
      meta(
        name := "og:description",
        content := description
      ),
      link(
        rel := "icon",
        href := "../images/favicon.ico",
        `type` := "image/x-icon"
      ),
      tags2.title(pageTitle),
      tags2.style(Style.raw),
      tags2.style(Style.styleSheetText),
      tags2.style(Style.mediaQueries),
      script(`type` := "text/javascript", src := "../js/prism.js"),
      link(
        rel := "stylesheet",
        `type` := "text/css",
        href := "../css/prism.css"
      )
    )

  case class NavItem(link: String, name: String, active: String):
    def html() =
      a(if active == name then Style.activePage else "", href := link, name)

  private def headerFrag(active: String) =
    header(
      Style.headerBase,
      tags2.nav(
        NavItem("/about", "about", active).html(),
        NavItem("/blog", "blog", active).html(),
        NavItem("/lists", "lists", active).html(),
        NavItem("/talks", "talks", active).html(),
        a(href := "https://www.tooling-talks.com", "tooling talks podcast")
      )
    )

  private def footerFrag() =
    footer(
      Style.footerBase,
      a(
        href := "https://github.com/ckipp01",
        target := "_blank",
        rel := "noopener noreferrer"
      )(
        img(
          Style.scaleOnHover,
          src := "../images/github.svg",
          alt := "GitHub logo"
        )
      ),
      a(href := "mailto:hello@chris-kipp.io")(
        img(
          Style.scaleOnHover,
          src := "../images/email.svg",
          alt := "email logo"
        )
      ),
      a(
        href := "https://hachyderm.io/@ckipp",
        rel := "me noopener noreferrer",
        target := "_blank"
      )(
        img(
          Style.scaleOnHover,
          src := "../images/mastodon.svg",
          alt := "mastodon logo"
        )
      ),
      a(
        href := "https://www.chris-kipp.io/rss.xml"
      )(
        img(
          Style.scaleOnHover,
          src := "../images/rss.svg",
          alt := "rss icon"
        )
      )
    )
end Html
