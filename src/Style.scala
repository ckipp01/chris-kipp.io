package io.kipp.site

import scalatags.stylesheet.*
import scalatags.Text.all.*
import scalatags.Text.styles2

object Style extends CascadingStyleSheet:
  initStyleSheet()

  val cream = "rgb(252, 243, 217)"
  val gray = "rgb(106, 112, 110)"
  val black = "rgb(0, 0, 0)"

  val cascasdeRoot = cls(
    fontSize := 18,
    minHeight := "100%",
    body(
      height := "100vh",
      background := cream
    ),
    header(
      display.flex,
      flexDirection.column,
      justifyContent.center,
      alignItems.center,
      flexShrink := 0,
      marginTop := "5px",
      Selector("nav")(
        display.flex,
        justifyContent.center,
        flexWrap.wrap,
        a(
          fontSize := "1.75rem",
          margin := 10
        )
      )
    ),
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
    ),
    p(marginBottom := 15),
    ul(marginBottom := 15, marginLeft := 15),
    ol(marginBottom := 15, marginLeft := 15),
    table(
      marginBottom := "5px 5px 15px 5px",
      padding := 5,
      tr(borderBottom := "1px solid")
    ),
    th(fontWeight.bold, margin := 5, padding := 5),
    td(margin := 5, padding := 5),
    a(
      borderBottomWidth := "2px",
      borderBottomStyle.dashed,
      borderColor := black,
      &.hover(
        background := black,
        color := cream
      )
    ),
    i(fontStyle.italic),
    blockquote(
      fontSize.small,
      borderLeftWidth := 3,
      borderLeftStyle.solid,
      borderLeftColor := black,
      marginLeft := 5,
      p(paddingLeft := 5, lineHeight := "1.25rem")
    ),
    Selector("main")(
      maxWidth := 750,
      margin := "10px auto",
      padding := "10px",
      lineHeight := "1.75rem",
      li(
        marginLeft := 20,
        listStyleType.circle
      ),
      em(fontStyle.italic),
      img(display.block, margin.auto, maxWidth := "100%"),
      iframe(margin.auto, display.block)
    ),
    footer(
      display.flex,
      justifyContent.center,
      margin := "5px 0",
      flexShrink := 0,
      a(
        width := 40,
        borderBottomStyle.none,
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
    ),
    li(
      marginLeft := "20px",
      listStyleType.circle
    ),
    pre(margin := "15px 0", overflow.auto),
    code(
      fontFamily := "monospace",
      lineHeight := "1rem"
    ),
    Selector("codeblock")(
      fontFamily := "monospace",
      lineHeight := "1rem"
    )
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

  val wrapper = cls(
    minHeight := "100%",
    margin.auto
  )

  val maxAndCenter = cls(
    maxWidth := 750,
    margin := "5px auto"
  )

  val overview = cls(
    li(margin := 10),
    span(paddingRight := 10, fontStyle.italic, opacity := 0.5),
    a(fontSize := "1.25rem")
  )

  val blogListing = cls(
    display.flex,
    flexDirection.column,
    paddingBottom := 10
  )

  val `youtube-container` = cls(
    position.relative,
    paddingBottom := "56.25%",
    height := 0,
    overflow.hidden,
    iframe(
      position.absolute,
      left := 0,
      right := 0,
      width := "100%",
      height := "100%"
    ),
    `object`(
      position.absolute,
      left := 0,
      width := "100%",
      height := "100%"
    ),
    embed(
      position.absolute,
      left := 0,
      width := "100%",
      height := "100%"
    )
  )

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
               |  html { font-family: 'Inter', sans-serif; }
               |  @supports (font-variation-settings: normal) {
               |    html { font-family: 'Inter var', sans-serif; }
               |  }
               |
               |*:focus {
               |  border: 1px dotted black;
               |}""".stripMargin
