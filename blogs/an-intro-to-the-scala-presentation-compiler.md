---
title: An intro to the Scala presentation compiler
date: 2021-07-15
description: An introduction to what the Scala presentation compiler is and an example of how to use it.
---

# An intro to the Scala Presentation Compiler

"Completions are powered by the presentation compiler". I remember the first
time I read this phrase. As someone that was fairly new to Scala, new to working
in tooling, and had never touched the internals of a compiler before, this
phrase made absolutely no sense to me. I had no idea what the presentation
compiler was, where it was located, and definitely no idea of how to use it. To
be honest, I was also a bit afraid to even ask those questions. Often when we
see phrases like "it's simple" or "it's easy", it really just means "I'm
familiar with this".

A lot of topics in tooling seem to be less covered than those of hot libraries
or general programming paradigms, which totally makes sense. There is a much
smaller group of people working in this area. However, it does then make it a
bit harder to get started. Thankfully, the Scala tooling ecosystem is packed
full of friendly helpful people that have always been more than helpful whenever
I've gotten stuck or had no clue where to start with something. Plus a lot of
the Scala documentation is incredibly useful and underappreciated. Like this
page on
[Trees](https://docs.scala-lang.org/overviews/reflection/symbols-trees-types.html#trees),
which will be relevant for what I show down below.

The goal for this post is so that hopefully when someone in the future hears a
phrase about the presentation compiler and has no idea what it is, this can
serve as introduction to what it is, where to find it, and as an example of how
to use it. When I first started searching around for these answers I came across
a few useful sources that I'll reference since they helped me:

- [The Scala Presentation
    Compiler](http://scala-ide.org/docs/dev/architecture/presentation-compiler.html),
    which gives a brief overview of how the Eclipse Scala IDE used the Scala
    presentation compiler.
- [Building an IDE with the Scala Presentation Compiler: A Field
    Report](https://ensime.blogspot.com/2010/08/building-ide-with-scala-presentation.html),
    which gives a great overview of how Ensime used the Scala presentation
    compiler.
- [This stack overflow
    post](https://stackoverflow.com/questions/16538791/scala-presentation-compiler-minimal-example),
    which gives a great minimal standalone example of how to use the
    presentation compiler.
- [Integrating IDEs with Dotty, The Experimental Scala Compiler by Guillaume
    Martres](https://www.youtube.com/watch?v=R77XYOtuomE), where Guillaume does
    a great job at explaining how IDEs can interact with Dotty.

## [What is the presentation compiler](#what-is-the-presentation-compiler)

The above mentioned post by the Scala IDE teams defines the Presentation
compiler as follows:

> The Scala IDE for Eclipse uses the Scala Presentation Compiler, a faster
> asynchronous version of the Scala Compiler. The presentation compiler only
> runs the phases up until and including the typer phase, that is, the first 4
> of the 27 scala compilation phases.

This is a really great minimal explanation of what the Scala Presentation
Compiler is. It's specifically made for usage from IDE-like tools that need a
compiler that is:

- Asynchronous
- Interruptible at every point
- Can do targeted typechecking
- Stops after a specific point (type-checking) and provide a partial result

Martin Odersky actually explains this a bit _way back in 2011_ when giving a
[talk](https://www.youtube.com/watch?v=qqQNqIy5LdM&t=1320s) about what's coming
in Scala 2.10. You may also be wondering where this thing is in the Scala code
base?

- For Scala 3 you can find it located [here in the interactive
    package](https://github.com/lampepfl/dotty/tree/master/compiler/src/dotty/tools/dotc/interactive)
- For Scala 2 it's [here in the interactive
    package](https://github.com/scala/scala/tree/2.13.x/src/interactive/scala/tools/nsc/interactive)

Also keep in mind that the compiler internals are completely different between 2
and 3, meaning that the presentation compiler API is completely different. So
for tooling that interfaces with the presentation compiler, they'll need to
account for this. For example that's why in the Metals
[mtags](https://github.com/scalameta/metals/tree/main/mtags/src/main/scala-3/scala/meta/internal/pc)
module, which is a module to abstract over compiler access, you'll see that
Scala 3 has a completely different implementation.

## [What uses the presentation compiler](#what-uses-the-presentation-compiler)

If you haven't already gotten this from looking at where these articles are
coming from, you'll notice that _many_ of the IDE-like tools in the Scala
Ecosystem use or have used the Scala Presentation Compiler.

- [Scala IDE for Eclipse](http://scala-ide.org/)
- [The Ensime project](https://ensime.github.io/)
- [Metals, the Scala Language Server](https://scalameta.org/metals/)
- [Ammonite](https://ammonite.io/)

You may have noticed that [IntelliJ](jetbrains.com/idea/) is missing from that
list. That's because IntelliJ actually uses its own implementation of the Scala
Presentation Compiler. That's why it's possible to see subtle differences with
diagnostics showing in IntelliJ when they don't show when compiling with the
actual Scala compiler.

Many of these projects use the presentation compiler to accomplish very similar
goals, and that is to provide the common features that you'd expect from an
editor. One of those common features is completions. For example you can see a
lot of the functionality for this in the aptly named
[Completion.scala](https://github.com/lampepfl/dotty/blob/master/compiler/src/dotty/tools/dotc/interactive/Completion.scala)
file in the Scala 3 codebase. The presentation compiler can do the majority of
the things you'd expect to see in an editor like, get the hover information on a
symbol, find the references of a symbol, or go to the definition site of a
symbol. A really nice example of all this can be found in the
[`DottyLanguageServer.scala`](https://github.com/lampepfl/dotty/blob/master/language-server/src/dotty/tools/languageserver/DottyLanguageServer.scala),
which was an embedded
[LSP](https://microsoft.github.io/language-server-protocol/) server for Scala 3
as the team was developing Dotty. The presentation compiler can be used for a
whole variety of things, and I'll give a very specific example of a minimal
feature that was just added to Metals in
[0.10.5](https://scalameta.org/metals/blog/2021/07/14/tungsten#add-support-for-textdocumentselectionrange)
down below.

## [A minimal example of using the presentation compiler](#a-minimal-example-of-using-the-presentation-compiler)

One thing I've found in the past is that there isn't a lot of standalone
examples out there of using the presentation compiler. It's not that surprising,
since why would there be, but recently while working on a new feature for
Metals, I thought it was a perfect small example of how the presentation
compiler could be used to implement an editor feature that is maybe a little
less common than the text-book examples of completions, definitions, or
references. If you'd like to follow along or play with the code I'm about to
show, there are examples for both Scala 2 and 3 located
[here](https://github.com/ckipp01/presentation-compiler-examples). In the
examples down below we'll focus on Scala 3.

The feature I wanted to implement in Metals was the [LSP
`textDocument/selectionRange`](https://microsoft.github.io/language-server-protocol/specifications/specification-current/#textDocument_selectionRange).
This feature allows you to more intelligently expand a selection. This is a
useful feature when you maybe want to quickly select everything inside of a
parameter, select an entire method, or really just anything one level above
where you are. Here is a quick gif showing what I mean:

![selection range example](https://i.imgur.com/1TSIvfI.gif)

If you were to look at the LSP communication for this request, you'd see the
response from the server would look something like this for the above example:

```json
[Trace - 00:07:29 AM] Sending response 'textDocument/selectionRange - (70)'. Processing request took 8ms
Result: [
  {
    "range": {
      "start": {
        "line": 2,
        "character": 14
      },
      "end": {
        "line": 2,
        "character": 28
      }
    },
    "parent": {
      "range": {
        "start": {
          "line": 2,
          "character": 6
        },
        "end": {
          "line": 2,
          "character": 29
        }
      },
      "parent": {
        "range": {
          "start": {
            "line": 1,
            "character": 2
          },
          "end": {
            "line": 2,
            "character": 29
          }
        },
        "parent": {
          "range": {
            "start": {
              "line": 0,
              "character": 0
            },
            "end": {
              "line": 2,
              "character": 29
            }
          }
        }
      }
    }
  }
]

```

Let's see how we can come up with these ranges using the presentation compiler.

We'll start with the very basic piece of code you saw in the gif. We can either
read this in from a file, or just store it as a string. For now just storing it
in a string is easiest, so we'll do that.

```scala
val ourScalaCode = """|object Main:
                      |  @main def hello: Unit =
                      |    println("Hello world!")
                      |""".stripMargin
```

I'll use [Coursier](https://get-coursier.io/) to fetch the actual scala3-library
to pass in when I create an instance of the `InteracativeDriver`. If you don't
do this dotc (dotty compiler) will yell at you to ensure that the compiler core
libraries are on the classpath. So we fetch them, and then we create the
`InteracativeDriver` aka the Scala 3 presentation compiler like so:

```scala

val fetch = Fetch.create()

fetch.addDependencies(
  Dependency.of("org.scala-lang", "scala3-library_3", "3.0.0")
)

val extraLibraries = fetch
  .fetch()
  .asScala
  .map(_.toPath())
  .toSeq

val driver = new InteractiveDriver(
  List(
    "-color:never",
    "-classpath",
    extraLibraries.mkString(File.pathSeparator)
  )
)
```

Keep in mind that this is meant to be a very minimal example with everything in
one place. Everything that we're doing is living in the same file. So if this
was a real world project we'd care a little more about where and how we are
managing this driver. For now, we're just creating it for a single run.

Next we'll take `ourScalaCode` from up above and create a virtual source file
out of it. We'll then take it and then run it. We can also verify here that
everything is valid with our code. For example we can assert that the run hasn't
returned any diagnostics.

```scala
val filename = "Example.scala"
val uri = java.net.URI.create(s"file:///$filename")

val sourceFile = SourceFile.virtual(filename, ourScalaCode)

val diagnostics = driver.run(
  uri,
  sourceFile
)

assert(diagnostics.isEmpty)
```

At this point, we already have everything we need to dive into the compiler tree
for the code that has just ran. We an access the entire untyped tree by doing
the following:

```scala
val tree: Option[Tree[Untyped]] =
  driver.currentCtx.run.units.headOption.map(_.untpdTree)
```

Instead, remember we are trying to get the selection ranges starting at a given
point in this code. So we'll pretend that the cursor is in the current position
(the char surrounded by <<>>) when we are going to trigger the expand selection
of the `textDocument/selectionRange` feature.

```scala
object Main:
  @main def hello: Unit =
    println("H<<e>>llo world!")
```

The offset for this position should be 55. So we'll create this position to use
in a bit. For some of the future calls we are about to make we'll also need a
context provided, so you'll also see that given. I'll share this code snippet
and then we'll dissect exactly what's going on.

```scala
val pos: SourcePosition = new SourcePosition(sourceFile, Spans.Span(55))

given ctx: Context = driver.currentCtx

val trees: List[dotty.tools.dotc.ast.tpd.Tree] =
  Interactive.pathTo(driver.openedTrees(uri), pos)
```

Firstly, the `driver.openedTrees(uri)` does a lookup and returns the full top
level trees that were discovered in the file during the run. The `pathTo` method
is an extremely useful method that returns the reverse path to the node in the
tree that has the closest enclosing position on the given position. This list of
trees is exactly what we are looking for to create our selection ranges. Note
that this actual returns a typed tree. We don't actually need a typed tree for
this, and if you look at the Scala 2 example, we do this same thing, but with an
untyped tree. In Metals currently it's done this way mainly due to how easy it
is, but it will probably change in the future work on an untyped tree.

Now that we have the trees we need, we'll map over them to collect the start and
end positions of the trees, and then only take the unique ones. There are
situations where multiple nodes may have the same range, but we discard any
duplicates.

If you're curious about the duplicates, here is a minimal example. Given a small
for comprehension like this:

```scala
 val total = for {
   a <- Some(1)
 } yield a
```

The AST for this would have some nodes that have the same ranges:

```scala
Apply(
  Select(Apply(Ident(Some), List(Literal(Constant(1)))), flatMap), // <-- This range
  List(
    Function(
      List(ValDef(Modifiers(8192L, , List()), a, <type ?>, <empty>)),
      Apply(
        Select(Apply(Ident(Some), List(Literal(Constant(2)))), map), // <-- Same as this range
        ...
      )
    )
  )
)
```

If the above looks foreign to you, the [Reflection Overview
Page](https://docs.scala-lang.org/overviews/reflection/overview.html) in the
docs is a great place to look.

So, once we have these positions, we'll zip them up with their index and
print out the code snippets that correspond with the "ranges".

```scala
val ranges = trees.map { tree =>
  (tree.sourcePos.start, tree.sourcePos.end)
}.distinct

ranges.zipWithIndex
  .foreach { case ((start, end), index) =>
    val selection = ourScalaCode.slice(start, end)
    pprint
      .copy(colorLiteral = fansi.Color.Blue)
      .pprintln(s"Selection Range: ${index + 1}")
    pprint.pprintln(selection)
  }
```

And finally, we should have the selections that we've been working at getting!
If you watch the gif up above again you should see selection ranges correspond
starting at the smallest enclosing node going all the way up the tree until you
have the entire source file covered.

```scala
"Selection Range: 1"
"\"Hello world!\""

"Selection Range: 2"
"println(\"Hello world!\")"

"Selection Range: 3"
"""@main def hello: Unit =
    println("Hello world!")"""

"Selection Range: 4"
"""object Main:
  @main def hello: Unit =
    println("Hello world!")"""
```

## [Conclusion](#conclusion)

Hopefully this helps give a somewhat clear picture of a real-world use case of
the presentation compiler. If you curious about more complex examples of how
this would be set up in a project, or how presentation compiler features could
be used in real applications, I'll drop a few links down below of places in code
to dig through. As always, if you're interested in getting involved in Scala
tooling but are unsure where to start, don't hesitate to reach out. Thanks for
stopping by.

### [Some code to dig through](#some-code-to-dig-through)

- [`SelectionRanges.scala`](https://github.com/ckipp01/presentation-compiler-examples/blob/main/scala-3-presentation-compiler-examples/src/SelectionRanges.scala)
    The runnable example code shown above. There is also a Scala 2 example in
    the same repo.
- [`ScalaPresentationCompiler.scala`](https://github.com/scalameta/metals/blob/main/mtags/src/main/scala-3/scala/meta/internal/pc/ScalaPresentationCompiler.scala)
    An overview of everything Metals uses the Scala 3 presentation compiler for.
- [`DottyLanguageServer.scala`](https://github.com/lampepfl/dotty/blob/master/language-server/src/dotty/tools/languageserver/DottyLanguageServer.scala)
    The built-in LSP server in the Scala 3 codebase.
- [`Compiler.scala`](https://github.com/com-lihaoyi/Ammonite/blob/master/amm/compiler/src/main/scala-3/ammonite/compiler/Compiler.scala)
    Shows how Ammonite accesses the Scala 3 presentation compiler.
