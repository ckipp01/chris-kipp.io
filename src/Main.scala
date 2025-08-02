package io.kipp.site

import scala.util.Try

import io.kipp.site.Extensions.sequence

object Main:

  @main def buildSite() =
    val result: Either[String, Unit] = for
      blogPosts <- getMarkdownPages(Constants.BLOG_DIR)
      ukrainianNotes <- getMarkdownPages(Constants.UKRAINIAN)
      topLevelPages <- getMarkdownPages(Constants.PAGES_DIR)
      lists <- getLists(Constants.LIST_DIR)
    yield
      /////////////////////
      // PROCESSING BLOG //
      /////////////////////
      scribe.info("processing the blog...")
      val blogPages = blogPosts
        .map: post =>
          scribe.info(s"putting together ${post.urlify}")
          post.copy(content = Html.blogPage(post).render)
        .sortBy(_.date)
        .reverse
      val blogRss = Rss.generate(blogPages)
      val blogOverview = Html.blogOverview(blogPages)
      /////////////////////////
      // PROCESSING Ukrainian//
      /////////////////////////
      scribe.info("processing ukrainian notes...")
      val ukrainianPages = ukrainianNotes
        .map: page =>
          scribe.info(s"putting together ${page.urlify}")
          page.copy(content = Html.blogPage(page).render)
        .sortBy(_.date)
        .reverse

      //////////////////////
      // PROCESSING TALKS //
      //////////////////////
      scribe.info("putting together talks page")
      val talks =
        // A little weird but we simply want to grab talks out of here because we're special casing it.
        lists
          .collectFirst:
            case talks: Talks => talks
          .get // TODO quick and dirty since we just refactored all of this.. probaly move this up later
      val talksPage = Html.talksOverview(talks)

      //////////////////////
      // PROCESSING LISTS //
      //////////////////////
      scribe.info("putting together lists page")
      val listsOverview = Html.listsOverview(lists)
      val listPages = lists.map(list => (list.id -> Html.listPage(list)))

      ////////////////////////////
      // PROCESSING TOP-LEVEL PAGES //
      ////////////////////////////
      scribe.info("processing top-level pages...")
      val processedTopLevelPages = topLevelPages
        .map: page =>
          scribe.info(s"putting together ${page.urlify}")
          page.copy(content = Html.blogPage(page).render)

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
      blogPages.foreach: post =>
        scribe.info(s"writing blog/${post.urlify}.html")
        os.write(
          os.Path(
            Constants.SITE_DIR / "blog" / (post.urlify + ".html"),
            os.pwd
          ),
          post.content,
          createFolders = true
        )

      ukrainianPages.foreach: page =>
        scribe.info(s"writing ukrainian/${page.urlify}.html")
        os.write(
          os.Path(
            Constants.SITE_DIR / "ukrainian" / (page.urlify + ".html"),
            os.pwd
          ),
          page.content,
          createFolders = true
        )

      listPages.foreach: (id, content) =>
        scribe.info(s"writing lists/${id}.html")
        os.write(
          os.Path(
            Constants.SITE_DIR / "lists" / (id + ".html"),
            os.pwd
          ),
          content,
          createFolders = true
        )

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

      processedTopLevelPages.foreach: page =>
        scribe.info(s"writing ${page.urlify}.html")
        os.write(
          os.Path(Constants.SITE_DIR / (page.urlify + ".html"), os.pwd),
          page.content
        )

      scribe.info("writing 404.html")
      os.write(
        os.Path(Constants.SITE_DIR / "404.html", os.pwd),
        Html.custom404().render
      )

      scribe.info("writting rss feed")
      os.write(os.Path(Constants.SITE_DIR / "rss.xml", os.pwd), blogRss.render)

    result match
      case Left(value)  => scribe.error(value)
      case Right(value) => scribe.info("Successfully built site")

  end buildSite

  private def getMarkdownPages(
      path: os.Path
  ): Either[String, Seq[MarkdownPage]] =
    scribe.info(s"Fetching markdown pages from ${path.baseName}")
    for
      pages <- Try(os.list(path)).toEither.left.map(_.getMessage)
      page <- pages
        .collect:
          case page
              if os.isFile(page) && !Constants.PAGES_TO_IGNORE.contains(
                page.baseName
              ) =>
            MarkdownPage.fromPath(page)
        .sequence
    yield page

  private def getLists(
      path: os.Path
  ): Either[String, Seq[SiteList]] =
    scribe.info(s"Fetching lists from ${path.baseName}")
    for
      lists <- Try(os.list(path)).toEither.left.map(_.getMessage)
      list <- lists
        .filterNot(_.last.contains("book"))
        .map(SiteList.fromPath)
        .sequence
    yield list
end Main
