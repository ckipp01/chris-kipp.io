//> using scala "3.2.1"
//> using lib "org.scala-lang::scala3-compiler:3.2.1"
//> using lib "com.lihaoyi::os-lib:0.9.0"
//> using lib "com.lihaoyi::scalatags:0.12.0"
//> using lib "com.lihaoyi::pprint:0.8.1"
//> using lib "com.outr::scribe:3.10.5"
//> using lib "com.vladsch.flexmark:flexmark-all:0.64.0"
//> using lib "org.virtuslab::scala-yaml:0.0.6"

package io.kipp.site

import dotty.tools.dotc.config.ScalaSettings

object Main:

  @main def buildSite() =
    val blogPosts =
      getBlogPosts(Constants.BLOG_DIR).sortBy(_.date).reverse
    val blogPages =
      blogPosts.map { post =>
        scribe.info(s"putting together ${post.urlify}")
        post.copy(content = Html.blogPage(post).render)
      }
    scribe.info("generating rss feed")
    val blogRss = Rss.generate(blogPosts)

    val talks = getTalks(Constants.TALKS_FILE)
    scribe.info("putting together talks page")
    val talksPage = Html.talksOverview(talks)

    scribe.info("putting together overivew page")
    val blogOverview = Html.blogOverview(blogPages)
    scribe.info("putting together about page")
    val aboutPage = Html.aboutPage()

    scribe.info("""cleaning "site" dir""")
    val keep = Seq(
      ".well-known",
      "vercel.json",
      "slides",
      "images",
      "js",
      "css",
      "chris-kipp-resume.pdf"
    )
    val filesToClean =
      os.walk(Constants.SITE_DIR, skip = path => keep.contains(path.last))
    filesToClean.foreach(os.remove.all)

    blogPages.foreach { post =>
      scribe.info(s"writing blog/${post.urlify}.html")
      os.write(
        os.Path(Constants.SITE_DIR / "blog" / (post.urlify + ".html"), os.pwd),
        post.content,
        createFolders = true
      )
    }

    scribe.info("writing index.html")
    os.write(
      os.Path(Constants.SITE_DIR / "index.html", os.pwd),
      blogOverview.render
    )

    scribe.info("writing blog.html")
    os.write(
      os.Path(Constants.SITE_DIR / "blog.html", os.pwd),
      blogOverview.render
    )

    scribe.info("writing talks.html")
    os.write(
      os.Path(Constants.SITE_DIR / "talks.html", os.pwd),
      talksPage.render
    )

    scribe.info("writing about.html")
    os.write(
      os.Path(Constants.SITE_DIR / "about.html", os.pwd),
      aboutPage.render
    )

    val allSettings = new ScalaSettings().allSettings
    val htmlSettings = Html.scalacSettings(allSettings)
    scribe.info("writing hideen scala3-scalac-options.txt")
    os.write(
      os.Path(Constants.SITE_DIR / "scala3-scalac-options.html", os.pwd),
      htmlSettings.render
    )

    scribe.info("writting rss feed")
    os.write(os.Path(Constants.SITE_DIR / "rss.xml", os.pwd), blogRss.render)

  private def getBlogPosts(path: os.Path) =
    scribe.info(s"Fetching blogs from ${path.baseName}")
    os.list(path).map(BlogPost.apply)

  private def getTalks(path: os.Path) =
    import org.virtuslab.yaml.*

    scribe.info(s"Fetching talks from ${path.baseName}")
    val contents = os.read(path)
    val decoded = contents.as[Seq[Talk]]

    decoded match
      case Left(e) =>
        scribe.error(e.msg)
        throw new RuntimeException(
          "Unable to correctly decode talks, so quitting."
        )
      case Right(talks) => talks
