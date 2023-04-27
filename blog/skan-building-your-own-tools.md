---
title: skan and building your own tools
date: 2023-04-27
description: A short article on a recent tool I built for myself, and how I enjoyed the process.
---

# skan and building your own tools

![overview of skan](/images/skan-overview.png)

I've recently been spending some time building my own little tool to track my
TODOs, [skan][skan]. While building this, I've re-learned some lessons that I
learned early only in my programming journey and some even played a large role
in my decision to continue to pursue programming.

The lessons are actually quite simple. They might not all be applicable to you,
and that's ok. We're all different, and we all learn differently. Hopefully
you'll still enjoy this, and maybe get some insight into the tools I used for
this project.

## There's power in making your ideas reality

When you first start programming (at least in more modern times) you undoubtedly
go through a phase where you're following tutorials, learning the basics of the
stack you're using, and just exploring what's possible. This is a great way
to get introduced to something, in fact I actually was in the process of doing
this with go right before I started this project. I was following a [video
series](https://www.youtube.com/playlist?list=PLLLtqOZfy0pcFoSIeGXO-SOaP9qLqd_H6)
on building a CLI kanban board with [Bubble
Tea](https://github.com/charmbracelet/bubbletea), which is a really rad TUI
library for go. However, there comes a time where you _must_ move on from the
tutorials and start building your own ideas, and when you do that, magical
things happen. I've even found now that after you start building stuff on your
own, finishing tutorials gets pretty difficult because you (well, at least I do)
want to veer off immediately in your own direction and try your own things. The
reason for that, again at least for me, is that it's an incredible feeling to
build something from scratch -- to take an idea in your mind and make it a
reality. Early on when I was learning to program I was part of a community
called [Merveilles](https://merveilles.town/about) and they embody this idea,
that you can take your ideas and make them reality. Even if no one else cares,
no one else will use them, you care, and that's enough. Simply the act of
thinking of an idea and seeing it through fruition is a really powerful thing.
I've built lots of little tools early on that made me feel so engaged and
powerful when I was on my computer. Over the years I've gotten away from that,
focused on building tools for other people, and forgot how good it feels to
build something for myself. [skan][skan] has reminded me of how good it feels to
build something for me. Building this little tool has been probably the
most fun I've had programming in quite some time.

If I was to summarize this, I'd say it's important to build your ideas, build
things just for you, and surround yourself with people that encourage you to do
it, even if it seems silly, because there's power in making your ideas reality.

## You learn more when things don't work

I'll be pretty honest, I'm not a JVM guru. Scala was the first language I truly
dove into and started to go beneath the surface, but I'm still in murky waters
when I go too deep. Since [skan][skan] is a CLI app, I didn't want to use the
JVM, which originally had me considering other languages. However, I was
intrigued when I saw [oyvindberg/tui-scala][tui-scala], especially since the
native image examples were quite snappy. [scala-cli][scala-cli] also makes it
crazy simple to produce a native image with GraalVM, so I figured I'd give it a
try. My very limited experience in the past with native images have been pretty
terrible. Getting everything set up was a chore, and then when it was ready to
go you almost always hit on cryptic initialization errors that really do force
you to understand some stuff that I've never really cared about. I was
pleasantly surprised when I was able to just run `scala-cli package
--native-image skan/` and it produced me a nice little executable. That was
until I tried running it and got:

```
Exception in thread "main" java.lang.UnsatisfiedLinkError: Native library libcrossterm.dylib (/libcrossterm.dylib) cannot be found on the classpath.
        at tui.crossterm.NativeLoader.loadPackaged(NativeLoader.java:29)
        at tui.crossterm.NativeLoader.load(NativeLoader.java:11)
        at tui.crossterm.CrosstermJni.<clinit>(CrosstermJni.java:9)
        at tui.withTerminal$.apply(withTerminal.scala:6)
        at skan.project$package$.run(project.scala:248)
        at skan.run.main(project.scala:18)
```

Thankfully, the author of [tui-scala][tui-scala] already had some of this
documented and was willing to give a helping hand. Now I have this lovely
[hack](https://github.com/ckipp01/skan/blob/bc6f583ba598335c41a1a4431835c2b0ecea2e4e/scripts/package-setup.scala)
that essentially does the following:

1. Uses coursier to fetch a jar that includes some files I need (necessary likely due to [this](https://github.com/oracle/graal/issues/5219))
2. Unpacks the jar, and based on the OS, grabs the necessary files and copies
   them to my resources folder.
3. Then when I package the app I include `--graalvm-args
   -H:IncludeResources=libcrossterm.dylib` as a flag.

Again, it's hacky, but it works. My command to actual make the image on MacOS
looks like this (inside of a Makefile):

```
package-mac:
	make prepare-for-graal
	make generate-build-info
	scala-cli --power \
		package \
		--native-image \
		--graalvm-java-version 19 \
		--graalvm-version 22.3.1 \
		--graalvm-args --verbose \
		--graalvm-args --no-fallback \
		--graalvm-args -H:+ReportExceptionStackTraces \
		--graalvm-args --initialize-at-build-time=scala.runtime.Statics$$VM \
		--graalvm-args --initialize-at-build-time=scala.Symbol \
		--graalvm-args --initialize-at-build-time=scala.Symbol$$ \
		--graalvm-args --native-image-info \
		--graalvm-args -H:IncludeResources=libcrossterm.dylib \
		--graalvm-args -H:-UseServiceLoaderFeature \
		skan/ -o out/skan
```

Notice the `make prepare-for-graal` call before the rest of the stuff from
packaging. That's what handles the setup. Again, much of this is copied from
[tui-scala][tui-scala], but because it didn't work at first it forced me to
actually look into the flags, better understand them, better understand what
[crossterm](https://github.com/crossterm-rs/crossterm) even is, and better
understand the inner-workings of [tui-scala][tui-scala]. Arguably, I wouldn't
have learned any of this if it all _just worked_. Don't misunderstand me, I do
wish it all _just worked_, but I also know that in software development many
times things won't. The skills you acquire when they don't and the knowledge you
gain while trying to understand _why_ something doesn't work is valuable.
Therefore, I think you learn more when things don't work.

## You learn to appreciate things when you don't have them anymore

If you have keen eyes you may have noticed another call up above, `make
generate-build-info`. This sort of relates to the above, but when things don't
work, you have to get creative. I'll be honest, I don't often think about tools
like [sbt-buildinfo][sbt-buildinfo]. I just use them and
move on with my day. The whole idea of source generators can just be glossed
over because they do what you want them to do, generate stuff so you can keep
coding.

Since [skan][skan] is a CLI app, you want a way to display the current version
that is being used. The way I would typically do that is with a tool like I
mentioned above, [sbt-buildinfo][sbt-buildinfo]. It's common to use, computes
the version from git, and generates a `BuildInfo.scala` file that you can use.
[scala-cli][scala-cli] has no ability to [generate code like
this](https://github.com/VirtusLab/scala-cli/issues/610). It's not until you
realize you can't do something or that something isn't supported that you truly
miss the ability to do that thing. To get around this I have another script that
ends up being ran before any compilation is ran with scala-cli. The script is
quite simple:

```scala
//> using scala "3.3.0-RC4"
//> using options "-deprecation", "-feature", "-explain", "-Wunused:all"
//> using lib "com.lihaoyi::os-lib:0.9.1"
//> using lib "com.outr::scribe:3.11.1"
//> using lib "com.github.sbt::dynver:5.0.0"

package skan.scripts

import java.util.Date

import sbtdynver.DynVer

@main def run() =
  val target = os.pwd / "skan" / ".scala-build" / "BuildInfo.scala"
  val version = DynVer.version(Date())
  scribe.info(s"Current version is ${version}")
  scribe.info(s"Generating BuildInfo.scala into ${target}")
  val buildInfo = s"""|package skan
                      |
                      |object BuildInfo:
                      |  val version = "${version}"
                      |""".stripMargin
  os.write.over(target = target, data = buildInfo, createFolders = true)
```

I uses the library portion of [sbt-dynver](https://github.com/sbt/sbt-dynver) to
compute the version based off git tags, generates a `BuildInfo.scala` file with
that version, and then copies that file into the main `skan` directory under the
`.scala-build/` directory that scala-cli creates. Then inside of my
`project.scala` I have the following:

```scala
//> using file "../.scala-build/BuildInfo.scala"
```

This line just ensures that the generated file is "sourced" and can be used just
like another file in my package.

You're probably thinking, this is brittle, hacky, and ugly. My response would be
yes, yes it is. The lesson here is that we often overlook simple tools that
cause us to not have to use hacks like this to accomplish things that other
tools may easily offer you. The `BuildInfo` example is just a small one, there
are other source generators that are much more involved and difficult to mimic,
especially with 10 lines of code.

## Build your own tools, or don't

I've said it multiple times, but [skan][skan] was such a fun little project for
me. It's not the most challenging software, most elegant, or even unique. There
are other TUI kanban boards out there with a way larger feature set, but that's
sort of the beauty of software development -- you can build whatever you want to
fit your exact needs. I encourage you to build something for yourself. Whether
it makes sense for other people, it doesn't matter. You'll probably hit on
things that don't work and learn something new in the process. You'll probably
wish you had something that X other tool had, and it'll cause you to add some
hack that causes you to appreciate that other tool. At the end, you can look
back at this tool you built for yourself and feel proud that you built something
from scratch, and it's yours. The same concept probably applies to libraries,
but I despise writing those. Maybe you despise the idea of writing your own
tool, and that's ok too.

[skan]: https://github.com/ckipp01/skan
[tui-scala]: https://github.com/oyvindberg/tui-scala
[scala-cli]: https://scala-cli.virtuslab.org/
[sbt-buildinfo]: https://github.com/sbt/sbt-buildinfo
