//> using scala "3.2.2"
//> using options "-deprecation", "-feature", "-explain"
//> using lib "org.scala-lang::scala3-compiler:3.2.2"
//> using lib "com.lihaoyi::os-lib:0.9.0"
//> using lib "com.lihaoyi::scalatags:0.12.0"
//> using lib "com.lihaoyi::pprint:0.8.1"
//> using lib "com.outr::scribe:3.10.7"
//> using lib "com.vladsch.flexmark:flexmark-all:0.64.0"
//> using lib "io.circe::circe-yaml:0.14.2"
//> using lib "io.circe::circe-generic:0.14.3"

package io.kipp.site

import dotty.tools.dotc.config.ScalaSettings

import scala.util.Try

import Extensions.sequence

object Main:

  @main def buildSite() =
    val result: Either[String, Unit] = for
      blogPosts <- getBlogPosts(Constants.BLOG_DIR)
      lists <- getLists(Constants.LIST_DIR)
    yield
      /////////////////////
      // PROCESSING BLOG //
      /////////////////////
      scribe.info("processing the blog...")
      val orderedPosts = blogPosts.sortBy(_.date).reverse
      val blogPages = blogPosts
        .map { post =>
          scribe.info(s"putting together ${post.urlify}")
          post.copy(content = Html.blogPage(post).render)
        }
        .sortBy(_.date)
        .reverse
      val blogRss = Rss.generate(blogPosts)
      val blogOverview = Html.blogOverview(blogPages)

      //////////////////////
      // PROCESSING TALKS //
      //////////////////////
      scribe.info("putting together talks page")
      val talks =
        // A little weird but we simply want to grab talks out of here because we're special casing it.
        lists.collectFirst { case talks: Talks =>
          talks
        }.get // TODO quick and dirty since we just refactored all of this.. probaly move this up later
      val talksPage = Html.talksOverview(talks)

      //////////////////////
      // PROCESSING LISTS //
      //////////////////////
      scribe.info("putting together lists page")
      val listsOverview = Html.listsOverview(lists)
      val listPages = lists.map(list => (list.id -> Html.listPage(list)))

      ///////////////////////
      // PROCESSING ABOUT ///
      ///////////////////////
      scribe.info("putting together about page")
      val aboutPage = Html.aboutPage()

      //////////////
      // CLEANING //
      //////////////
      scribe.info("""cleaning "site" dir""")
      val keep = Set(
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

      ////////////////
      // GENERATION //
      ////////////////
      blogPages.foreach { post =>
        scribe.info(s"writing blog/${post.urlify}.html")
        os.write(
          os.Path(
            Constants.SITE_DIR / "blog" / (post.urlify + ".html"),
            os.pwd
          ),
          post.content,
          createFolders = true
        )
      }

      listPages.foreach { (id, content) =>
        scribe.info(s"writing lists/${id}.html")
        os.write(
          os.Path(
            Constants.SITE_DIR / "lists" / (id + ".html"),
            os.pwd
          ),
          content,
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

      scribe.info("writing lists.html")
      os.write(
        os.Path(Constants.SITE_DIR / "lists.html", os.pwd),
        listsOverview.render
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

    result match
      case Left(value)  => scribe.error(value)
      case Right(value) => scribe.info("Successfully built site")

  end buildSite

  private def getBlogPosts(
      path: os.Path
  ): Either[String, Seq[BlogPost]] =
    scribe.info(s"Fetching blogs from ${path.baseName}")
    for
      blogs <- Try(os.list(path)).toEither.left.map(_.getMessage)
      blog <- blogs.map(BlogPost.fromPath).sequence
    yield blog

  private def getLists(
      path: os.Path
  ): Either[String, Seq[SiteList]] =
    scribe.info(s"Fetching lists from ${path.baseName}")
    for
      lists <- Try(os.list(path)).toEither.left.map(_.getMessage)
      list <- lists.map(SiteList.fromPath).sequence
    yield list
end Main
