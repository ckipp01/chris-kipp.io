package io.kipp.site

import scalatags.Text.all.*
import scalatags.Text.tags2
import dotty.tools.dotc.config.Settings.Setting

object Html:
  def blogPage(blogPost: BlogPost) =
    doctype("html")(
      html(
        lang := "en",
        Style.cascasdeRoot,
        headFrag(
          pageTitle = blogPost.title,
          description = blogPost.description,
          thumbnail = blogPost.thumbnail
        ),
        body(
          div(
            Style.wrapper,
            headerFrag(),
            tags2.main(
              // TODO maybe the date here
              raw(blogPost.content)
            ),
            footerFrag()
          )
        )
      )
    )

  def blogOverview(blogPosts: Seq[BlogPost]) =
    doctype("html")(
      html(
        lang := "en",
        Style.cascasdeRoot,
        headFrag(
          pageTitle = "chris-kipp.io - blog",
          description =
            "Collection of blogs posts by Chris Kipp over the years."
        ),
        body(
          div(
            Style.wrapper,
            headerFrag(),
            tags2.main(
              Style.overview,
              blogPosts.map { blogPost =>
                div(
                  Style.blogListing,
                  a(
                    borderBottomStyle.none,
                    href := s"./blog/${blogPost.urlify}"
                  )(
                    blogPost.title
                  ),
                  span(blogPost.date)
                )
              }
            ),
            footerFrag()
          )
        )
      )
    )

  def talksOverview(talks: Seq[Talk]) =
    doctype("html")(
      html(
        lang := "en",
        Style.cascasdeRoot,
        headFrag(
          pageTitle = "chris-kipp.io - talks",
          description = "Collection of talks by Chris Kipp over the years."
        ),
        body(
          div(
            Style.wrapper,
            headerFrag(),
            tags2.main(
              Style.talkListing,
              talks.map { talk =>
                div(
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
            ),
            footerFrag()
          )
        )
      )
    )

  def aboutPage() =
    doctype("html")(
      html(
        lang := "en",
        Style.cascasdeRoot,
        headFrag(
          pageTitle = "about - chris-kipp.io",
          description =
            "A litte bit about me, Chris Kipp, the author of this blog."
        ),
        body(
          div(
            Style.wrapper,
            headerFrag(),
            tags2.main(
              img(src := "../images/me.png"),
              p(
                """Hi, I'm Chris. You've stumbled upon my blog and website. It's a simple place
                  |where I write some things and hold links to other places that I'd like to
                  |remember. You'll find me writing or working on things related to developer
                  |tooling, primarily with Neovim and Scala, talking about music, or sharing stuff
                  |I find interesting. I have a varied background, including growing up on a farm
                  |in the United States and having a M.A. in International Relations. I first got
                  |started in tech as I was finishing grad school, and I haven't really looked back since.
                  |I'm currently located in the Netherlands with my wife and working at """.stripMargin,
                a(href := "https://lunatech.nl/", "Lunatech"),
                ", where they are kind enough to lend me out to the ",
                a(href := "https://scala.epfl.ch/", "Scala Center"),
                " where I work full-time on Scala tooling."
              ),
              p(
                "You can take a look at the projects I work on ",
                a(href := "https://github.com/ckipp01", "here on GitHub"),
                " find me on ",
                a(href := "https://hachyderm.io/@ckipp", "Mastodon"),
                ", ",
                a(href := "https://twitter.com/ckipp01", "Twitter"),
                ", or streaming on ",
                a(href := "https://www.twitch.tv/ckipp", "Twitch"),
                "."
              ),
              p(
                """Over the years this site has taken many shapes ranging from a custom JS
                  |framework powered site, one that fully tracked all my free time, to ultimately
                  |the minimal shape you see it in now. It will continue to change and grow as I do.""".stripMargin
              ),
              p("Thanks for stopping by."),
              p("Chris")
            ),
            footerFrag()
          )
        )
      )
    )

  def scalacSettings(settings: Seq[Setting[?]]) =
    doctype("html")(
      html(
        lang := "en",
        Style.cascasdeRoot,
        headFrag(
          pageTitle = "chris-kipp.io - scalacOptions",
          description = "All the Scala 3 options"
        ),
        body(
          div(
            Style.wrapper,
            headerFrag(),
            tags2.main(
              settings.map { setting =>
                div(h3(setting.name), p(setting.description))
              },
              footerFrag()
            )
          )
        )
      )
    )

  private def headFrag(
      pageTitle: String,
      description: String,
      thumbnail: Option[String] = None
  ) = {
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
      script(`type` := "text/javascript", src := "../js/prism.js"),
      link(
        rel := "stylesheet",
        `type` := "text/css",
        href := "../css/prism.css"
      ),
      script(
        defer := true,
        `type` := "text/javascript",
        src := "https://api.pirsch.io/pirsch.js",
        id := "pirschjs",
        attr("data-code") := "2N1ystwyAD7E52EmuxQkTyoAno02YcQE"
      )
    )
  }

  private def headerFrag() = {
    header(
      tags2.nav(
        a(href := "/about", "about"),
        a(href := "/blog", "blog"),
        a(href := "/talks", "talks"),
        a(href := "https://www.tooling-talks.com", "tooling talks podcast")
      )
    )
  }

  private def footerFrag() = {
    footer(
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
        href := "https://twitter.com/ckipp01",
        target := "_blank",
        rel := "noopener noreferrer"
      )(
        img(
          Style.scaleOnHover,
          src := "../images/twitter.svg",
          alt := "Twitter logo"
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
  }
