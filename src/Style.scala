package io.kipp.site

import scalatags.Text.all.*
import scalatags.Text.styles2
import scalatags.stylesheet.*

object Style extends CascadingStyleSheet:
  // TODO once https://github.com/com-lihaoyi/scalatags/pull/263 is merged and
  // released, remove this.
  override def customSheetName: Option[String] = Some("site")
  initStyleSheet()

  val cream = "rgb(252, 243, 217)"
  val gray = "rgb(106, 112, 110)"
  val black = "rgb(0, 0, 0)"

  val root = cls(
    fontSize := 18,
    minHeight := "100%"
  )

  val bodyBase = cls(
    height := "100vh",
    background := cream,
    i(fontStyle.italic),
    b(fontWeight.bold),
    hr(marginTop := 15, marginBottom := 15),
    Selector("main")(
      maxWidth := 750,
      margin := "10px auto",
      padding := "10px",
      lineHeight := "1.75rem"
    )
  )

  val linkBase = cls(
    a(
      borderBottomWidth := "2px",
      borderBottomStyle.dashed,
      borderColor := black,
      padding := 2
    )
  )

  val linkHoverBase = cls(
    a(
      &.hover(
        background := black,
        color := cream,
        borderRadius := 2
      )
    )
  )

  val headerBase = cls(
    display.flex,
    flexDirection.column,
    justifyContent.center,
    alignItems.center,
    marginTop := "5px",
    Selector("nav")(
      display.flex,
      justifyContent.center,
      flexWrap.wrap,
      a(
        fontSize := "1.75rem",
        margin := 5,
        padding := 5
      )
    ),
    linkHoverBase.splice
  )

  val headings = cls(
    h1(
      fontWeight.bold,
      fontSize := "1.5rem",
      lineHeight := "2rem",
      margin := "1rem 0"
    ),
    h2(
      fontWeight.bold,
      fontSize := "1.3rem",
      lineHeight := "2rem",
      margin := "1rem 0"
    ),
    h3(
      fontWeight.bold,
      fontSize := "1.2rem",
      lineHeight := "1.5rem",
      margin := "1rem 0"
    )
  )

  val tableBase = cls(
    table(
      margin := "5px 5px 15px 5px",
      padding := 5,
      tr(borderBottom := "1px solid")
    ),
    th(fontWeight.bold, margin := 5, padding := 5),
    td(margin := 5, padding := 5)
  )

  val footerBase = cls(
    display.flex,
    justifyContent.center,
    margin := "5px 0",
    a(
      width := 40,
      &.hover(
        background := "inherit",
        color := "inherit",
        styles2.transform := "scale(1.2)"
      )
    ),
    img(
      padding := 10,
      maxHeight := 20,
      margin := "5px 0"
    )
  )

  val activePage = cls(
    background := black,
    color := cream,
    borderRadius := 2
  )

  val iconContainer = cls(
    display.flex,
    justifyContent.center,
    a(
      width := 40
    ),
    img(
      padding := 10,
      maxHeight := 20,
      margin := "5px 0"
    )
  )

  val scaleOnHover = cls.hover(
    styles2.transform := "scale(1.1)"
  )

  val maxAndCenter = cls(
    maxWidth := 750,
    margin := "5px auto"
  )

  val largeFontOverview = cls(
    marginBottom := 20,
    span(fontStyle.italic, opacity := 0.5),
    a(fontSize := "1.25rem", padding := 2),
    p(fontSize := "1.25rem", marginBottom := 0)
  )

  val talkListing = cls(
    div(marginBottom := 20),
    span(fontStyle.italic, opacity := 0.5),
    p(fontSize := "1.25rem", marginBottom := 0),
    linkHoverBase.splice
  )

  val writing = cls(
    img(
      display.block,
      margin.auto,
      marginBottom := "10px",
      maxWidth := "100%",
      maxHeight := "500px"
    ),
    p(marginBottom := 15),
    ul(marginBottom := 15, marginLeft := 15),
    ol(marginBottom := 15, marginLeft := 15),
    blockquote(
      fontSize.small,
      borderLeftWidth := 3,
      borderLeftStyle.solid,
      borderLeftColor := black,
      marginLeft := 5,
      p(paddingLeft := 5, lineHeight := "1.25rem")
    ),
    li(
      marginLeft := 20,
      listStyleType.circle
    ),
    em(fontStyle.italic),
    iframe(margin.auto, display.block, width := "100%"),
    pre(margin := "15px 0", overflow.auto),
    code(
      fontFamily := "monospace",
      lineHeight := "1rem"
    ),
    Selector("codeblock")(
      fontFamily := "monospace",
      lineHeight := "1rem"
    ),
    linkBase.splice,
    linkHoverBase.splice,
    headings.splice
  )

  val blogListing = cls(
    display.flex,
    flexDirection.column,
    paddingBottom := 10,
    linkHoverBase.splice
  )

  val album = cls(
    display.flex,
    alignItems.center,
    flexDirection.column,
    img(maxHeight := 100, padding := 10)
  )

  val albumDescription = cls(
    flexGrow := 1
  )

  val albumRating = cls(
    whiteSpace.nowrap
  )

  val star = cls(
    height := 20
  )

  val article = cls(
    p(fontStyle.italic, opacity := 0.5)
  )

  val sites = cls(
    marginBottom := 10,
    p(fontStyle.italic, opacity := 0.5),
    span(fontStyle.italic, opacity := 0.5, fontSize := "0.8em")
  )

  val backToTop = cls(
    position.fixed,
    bottom := "30px",
    right := "30px",
    width := "48px",
    height := "48px",
    display.flex,
    alignItems.center,
    justifyContent.center,
    borderRadius := "50%",
    cursor.pointer,
    opacity := "0"
  )

  // TODO there really isn't a way to do this with scalatags
  // https://github.com/com-lihaoyi/scalatags/issues/156
  val mediaQueries = s"""@media (min-width: 768px) {
                         |  .site-album {
                         |    flex-direction: row;
                         |  }
                         |}
                         |.site-backToTop {
                         |  transition: opacity 0.3s;
                         |  background: conic-gradient(${black} var(--progress, 0%), rgba(0,0,0,0.1) var(--progress, 0%));
                         |}
                         |.site-backToTop span {
                         |  width: 40px;
                         |  height: 40px;
                         |  display: flex;
                         |  align-items: center;
                         |  justify-content: center;
                         |  background: ${cream};
                         |  color: ${black};
                         |  border-radius: 50%;
                         |  font-size: 1.1rem;
                         |  transition: background 0.2s, color 0.2s;
                         |}
                         |.site-backToTop:hover span {
                         |  background: ${black};
                         |  color: ${cream};
                         |}""".stripMargin

  // There isn't a good way to do things like @import or * with scalatags, so
  // we just do it raw here.
  val raw = s"""* {
               |  margin:0;
               |  padding:0;
               |  border:0;
               |  outline:0;
               |  border-spacing:0;
               |  text-decoration:none;
               |  font-weight:inherit;
               |  font-style:inherit;
               |  color:inherit;
               |  font-size:100%;
               |  font-family:inherit;
               |  vertical-align:baseline;
               |  list-style:none;
               |  border-collapse:collapse;
               |  -webkit-font-smoothing: antialiased;
               |  -moz-osx-font-smoothing: grayscale;
               |  }
               |
               |  @import url('https://rsms.me/inter/inter.css');
               |  html { font-family: 'Inter', sans-serif; scroll-behavior: smooth; }
               |  @supports (font-variation-settings: normal) {
               |    html { font-family: 'Inter var', sans-serif; }
               |  }
               |
               |*:focus {
               |  background: ${gray};
               |}""".stripMargin
end Style
