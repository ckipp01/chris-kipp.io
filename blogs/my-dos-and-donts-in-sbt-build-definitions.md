---
title: My Dos and Donts in sbt Build Definitions
date: 2021-12-19
description: These are rules I like to follow when writing my build definitions using sbt.
---

# My Dos and Don'ts in sbt Build Definitions

Over the past couple of years I've been able to dig around a lot of sbt build
files. Whether [sbt](https://www.scala-sbt.org/index.html) is your Scala build
tool of choice or not, you'll likely need to deal with it at some time or
another. sbt baffled me when I first got started. The simple things were
actually pretty simple, but anything else sort of forces you to get a
better-than-surface-level understanding of sbt. Depending on that level of
understanding, the team writing that build definition, or even the type of
project your writing it for provides for a nice smörgåsbord of examples out
there. All sorts of fun and crazy stuff. Because of this, I thought I'd make a
list of Dos and Don'ts I typically try to follow when writing build definitions
in sbt. These opinions were formed with a mixture of open-source and
professional work, annoyances with incredibly complex builds, and probably some
straight-up misunderstandings on my part since sbt still baffles me at times.

This list isn't exhaustive, it's highly opinionated, and is tainted by specific
experiences and projects I've been a part of. More than anything, the goal of
this is to hopefully spark a conversation in your team about build definitions,
how you write them, and best practices to follow. Sometimes the best practice is
simply having your team all on the same page and doing things the same way.

So without further ado, here are my personal Dos and Don'ts

- [Don't make separate `build.sbt` files for your different modules](#dont-make-seperate-build-files-for-your-different-modules)
- [Do stay away from bare settings](#do-stay-away-from-bare-settings)
- [Don't let your settings leak into modules they don't need to be in](#dont-let-your-settings-leak)
- [Do regularly go over your entire build definition](#do-regularly-go-over-your-entire-build-definition)
- [Don't shade things unless you absolutely have to](#dont-sahde-things-unless-you-absolutely-have-to)
- [Do stick to `lazy val`s everywhere](#do-stick-to-lazy-vals-everwhere)
- [Don't ever hardcode constants](#dont-ever-hardcode-constants)
- [Do stay away from crazy meta meta build stuff](#do-stay-away-from-crazy-meta-meta-build-stuff)
- [Don't put stuff in `project/` unless it is a collection of something or is "standalone"](#dont-put-stuff-in-project-unless-its-standalont-or-a-collection)
- [Do have a conversation with your team about standards and consistency](#do-have-a-conversation-with-your-team-about-standards-and-consistency)
- [Don't make your own way to do something if sbt already provides a way](#dont-make-your-own-way-to-do-something-if-sbt-already-provides-a-way)
- [Do document it when you do weird shit](#do-document-it-when-you-do-weird-shit)
- [Don't add stuff to your build definition you don't understand](#dont-add-stuff-to-your-build-definition-you-dont-understand)


## [Don't make separate `build.sbt` files for your different modules](#dont-make-seperate-build-files-for-your-different-modules)

Like everything, there seems to be trends in build definitions. One trend I saw
for a while (and you may still use it) is to keep a separate build file for
every project in your build. Meaning you'll likely have a structure like this:

```
.
├── a
│  ├── build.sbt
│  └── src
├── b
│  ├── build.sbt
│  └── src
├── build.sbt
├── c
│  ├── build.sbt
│  └── src
├── d
│  ├── build.sbt
│  └── src
└── project
   └── build.properties
```

My experience with this type of setup is that you end up jumping between your
root `build.sbt` and your project-specific `build.sbt` and probably even some
`project/*.scala` to figure out where your stuff is defined and where you need
to change it. The projects I've seen use this style of multiple `build.sbt`
files often make heavy use of `project/*.scala|*.sbt` files making the situation
even worse. In theory I like this idea, but in practice I hate it. I actually
don't see this nearly as often anymore with new projects. Just stick to a single
`build.sbt` file in the root of your directory which contains all the settings
for each separate project. It makes things easier to find, and then your entire
build definition is basically contained in one place.

_Taken from the [sbt
docs](https://www.scala-sbt.org/1.x/docs/Multi-Project.html)_

> We recommend putting all project declarations and settings in the root
> build.sbt file in order to keep all build definition under a single file.
> However, it’s up to you.

## [Do stay away from bare settings](#do-stay-away-from-bare-settings)

My beef with bare settings can be illustrated with a very minimal build
definition. Let's say we start a new project with an extremely minimal setup:

```scala
scalaVersion := "3.1.0"

lazy val root = (project in file("."))
  .settings(
    name := "bare-settings"
  )
```

Just to make sure we're all on the same page `scalaVersion` is the bare setting
here. Now this works fine as long as root is your only project. _However_ more
than likely you'll eventually want to expand this to have another project. Let's
pretend we'd like to add a `domain` module. So we do that like so:


```scala
scalaVersion := "3.1.0"

lazy val root = (project in file("."))
  .settings(
    name := "bare-settings"
  )

lazy val domain = project
```

Now, you might see the issue with the immediately if you're familiar with sbt
and how it's various scopes work, but keep in mind that most newcomers to Scala
won't, and they will immediately be met with confusion when they discover the
following:

```
sbt:bare-settings> projects
[info] In file:/Users/ckipp/Documents/scala-workspace/bare-settings/
[info]     domain
[info]   * root
sbt:bare-settings> scalaVersion
[info] 3.1.0
sbt:bare-settings> domain/scalaVersion
[info] 2.12.14
```

If you don't understand why `domain` is on 2.12.14 when you set `scalaVersion`
to 3.1.0, it's because you're getting bit by an assumption that a bare settings
should be carried over to your subprojects. There is an interesting discussion
[here](https://github.com/sbt/sbt/issues/6217) where [Eugene
Yokota](https://twitter.com/eed3si9n) proposes to deprecate and eventually
remove bare settings in hope to avoid this confusion. It explains a bit of the
history behind them, ideas to change the default behavior to be less confusing,
and also contains some arguments _for_ having them. It's worth a read.

The example up above is minimal, but you can see how once your project becomes
larger and you have numerous bare settings how confusing this could become. The
main argument for them is to help someone new to sbt since just using
`scalaVersion` is easier than `ThisBuild / scalaVersion`. That's fair, since the
latter also forces knowledge of `ThisBuild`. So the question really is, does the
default behavior of bare settings make sense? Again, read the linked thread
above for a much more detailed conversation on this.

My takeaway and personal experience is to avoid bare settings completely. The
time spent explaining to your team why will be time better spent than a new
teammate banging their head against the wall when they are bit by it later on if
you don't and you use bare settings.

## [Don't let your settings leak into modules they don't need to be in](#dont-let-your-settings-leak)

A common approach you'll see in a lot of builds is making use of common
settings. You'll often see something like the following:


```scala
lazy val api = project
  .settings(commonSettings)
  .dependsOn(domain)

lazy val domain = project
  .settings(commonSettings)

lazy val commonSettings = Seq(
  scalaVersion := "3.1.0",
  libraryDependencies += "com.lihaoyi" %% "cask" % "0.8.0",
  scalacOptions += "-explain"
)
```

Most of the time having some sort of common settings is a good thing, but many
times you'll end up with settings that are leaking into other projects that you
don't actually want. In the above example there is no reason cask needs to be a
dependency of domain, but it is. You might think this would never actually
happen since it's so obvious you don't need a HTTP framework as a dependency in
your domain module, but I've seen this happen time and time again. I've even
guilty of it at times. This can lead to bloating your dependencies for a project
causing you to rely on things you don't need, having invalid `scalacOptions` for
projects, or just build settings being applied in places you don't want them,
and maybe don't even realize they are being applied. Be extra careful with
common settings and ensure that what you have in there truly are meant to be
share every place that is using them. Again, this seems obvious in such a
minimal example, but add hundreds of lines of sbt code to this and you'll
quickly see the problem.

## [Do regularly go over your entire build definition](#do-regularly-go-over-your-entire-build-definition)

This actually relates to the above point about being careful with your common
settings. I've been amazed at the amount of times I've been looking through a
build file and thought "What is this doing in here, this was for an issue fixed
_long_ ago". It seems that we never refactor build definitions. We put them
together, add some hacks, and then leave them until we need to add another hack.
We forget that our build definition is also code, ripe for refactoring, and
arguably will always have stuff left over that doesn't need to be in there. This
also relates to other points I'll make about ensuring that when you add
something to a build definition to avoid a bug, work around an issue, etc, you
should also add a note as to what it's doing and why you're doing it.

When you change something in your build, take a bit of time, walk through it,
make sure things are up to date, make sure exclusions that are there still need
to be there, dependency overrides still need to be there, and other random
settings you copied off Stack Overflow to overcome some issue still need to be
there. This helps keep those build definitions nice and tidy and avoids the "Is
this still relevant" question I so often ask when looking at build files.

## [Don't shade things unless you absolutely have to](#dont-sahde-things-unless-you-absolutely-have-to)

I hesitated to put this in here since it's maybe more specific to just
dependency management, but I thought it helped reinforce the idea of keeping
things simple. Shading at times is necessary and even wise to ensure you don't
force a transitive dependency on a downstream user, but in those situations you
are adding a layer of complexity to your build definition that will cause others
to have to spend the time understanding why it was done. An argument could be
made that if this is clearly documented _why_ then it's no issue, but I've often
found that when shading is used you get a single line comment at most. Sometimes
this is enough, but in other cases I've found hundreds of lines of code related
to shading with no explanation. Understand that this can be almost impossible to
decipher without the full backstory of why it was done.

My default rule is to avoid shading unless you absolutely have to do it and the
benefit outweighs any complexity and confusion you may be adding to your build
definition.

## [Do stick to `lazy val`s everywhere](#do-stick-to-lazy-vals-everwhere)

This one may seem obvious to some, but it's also a commonly asked question. The
most simple answer is that to avoid having to define everything before you use
it, use lazy vals. The below example is something you'll see fairly often. Now
if you start removing `lazy` from various places you will break stuff since if
you have a project that isn't lazy and tries to access another project before
it's defined or commons settings before it's defined you will run into issues.

```scala
ThisBuild / scalaVersion := "3.1.0"

lazy val V = new {
  val cask = "0.8.0"
}

lazy val api = project
  .settings(commonSettings)
  .settings(
    libraryDependencies += "com.lihaoyi" %% "cask" % V.cask
  )
  .dependsOn(domain)

lazy val domain = project
  .settings(commonSettings)

lazy val commonSettings = Seq(
  scalacOptions += "-explain"
)

```

To keep it simple for myself, every time I use a val at the top level in my sbt
file, I make it lazy. One related thing I've seen in build files that _isn't_
needed is nested lazy modifies like:


```scala
lazy val V = new {
   lazy val cask = "0.8.0"
}
```

## [Don't ever hardcode constants](#dont-ever-hardcode-constants)

This is actually a best practice taken right [from the
docs](https://www.scala-sbt.org/1.x/docs/Best-Practices.html#Don%E2%80%99t+hard+code).
Since it's in the docs I'll directly quote from there and use the same example.

> Don’t hard code constants, like the output directory target/. This is
> especially important for plugins. A user might change the target setting to
> point to build/, for example, and the plugin needs to respect that. Instead,
> use the setting, like:

```scala
myDirectory := target.value / "sub-directory"
```

## [Do stay away from crazy meta meta build stuff](#do-stay-away-from-crazy-meta-meta-build-stuff)

The amount of projects that actually need this is incredibly small, so maybe
you'll never see this. However, I've come across projects with
`project/project/project/build.sbt` which is ludicrous. The mental gymnastics
that a user needs to go through to understand what's happening here can give
them nightmares. Are there actual use cases for including logic in your sbt
meta-build? Absolutely, but I question if you ever need to go another layer
deeper. Most users won't understand what's happening or how you even interact or
see settings defined there.

If you're a Metals user you may be thinking "Wait a second, Metals creates a
`project/project/metals.sbt`... you hypocrite!". If you're using Metals and
Bloop as your build server, Metals needs to export your build to Bloop. The only
way to do that is to go one layer up and add the `sbt-bloop` plugin. So if you
have stuff in your `project/*.sbt` we need to add a `metals.sbt` in your
`project/project/`. If you have something in your  `project/project/*.sbt`, we
would also need to add the plugin in your `project/project/project/` dir. You
get the idea. This goes to show that in reality there _are_ some use cases for
this, but they are very few. If you can, avoid it.

## [Don't put stuff in `project/` unless it is a collection of something or is "standalone"](#dont-put-stuff-in-project-unless-its-standalont-or-a-collection)

Another rule I like to follow is to not include things in `project/` unless it's
something that is "standalone" or a collection. A couple examples of these.

At work we use a private nexus, and when you publish sbt plugins maven style, it
doesn't create a `maven-metadata.xml` file. NOTE: That it _does_ create one for
external plugins that you are mirroring, but not ones you publish yourself. This
ends up being problematic when you run Scala Steward, since under the hood
coursier needs a `maven-metadata.xml` or an index that it can scan, but in this
scenario has neither. You can see the context for what I explained
[here](https://github.com/coursier/coursier/issues/1874). However, the way we
got around this issue was to create a custom release step for
[sbt-release](https://github.com/sbt/sbt-release) which created and/or updated
the metadata file and was simply called during the release process. We stuck in
in `/project/CustomReleaseStep.scala`. It's only referenced once from the
`build.sbt` file, and apart from that never touched, never inherited from, and
has no impact on the rest of the build definition. This is a great example of
something that is "standalone" and belongs there.

The other time I like to put stuff in the `project/` is when it's a collection
of sorts. This can be a collection of dependencies that you're using in your
build, a collection of maintainers, or something similar. These are also great
things to stick in `project/` to not take up a ton of space in your `build.sbt`.

Apart from those two things, I like to stick everything else in my `build.sbt`.
I dislike when projects have a bunch of `project/*.scala` files with complex
logic that conditionally set keys making it very difficult to understand where
something is being set.

## [Do have a conversation with your team about standards and consistency](#do-have-a-conversation-with-your-team-about-standards-and-consistency)

This might be the most important one on the list. Most of the time you don't
work in isolation meaning that others will be touching your build definition.
The benefit of having everyone on your team on the same page far outweighs all
the other best practices since everyone hopefully understands the patterns
you're following, the reasons you're doing them, and is more quickly able to
edit and iterate on them. If you've never had a discussion with your team about
your build definition, what standards you want to follow, and document those,
you should. If you don't you'll end up with build definitions that are wildly
different from one another, build definitions that have mixed ways of doing
things, and ultimately creating technical debt the moment you add something to
your build.

## [Don't make your own way to do something if sbt already provides a way](#dont-make-your-own-way-to-do-something-if-sbt-already-provides-a-way)

It might be tempting to try and come up with your own solutions when you don't
like the "sbt" way of doing things. For the sake of an illustration let's
pretend we hate the way it's currently done to have cross-versioned sources. You
have some Scala 2 code and some Scala 3 code you want side by side. You don't
like the default `src/main/scala-<scala binary version>/` being the way it
includes Scala-version specific sources so you decide to come up with your own
"better" solution.

```scala
Compile / sources := {
  (Compile / sources).value.filter(_.getName().endsWith("2"))
}
```

That was easy, now it can quickly pick up all my Scala 2 sources, I just need to
make sure they end with Scala 2, and then my Scala 3 ones can end with 3. You
now just broke away from the expected norm for anyone familiar with sbt. What
they expect to work won't, and you force the user to dig into what's going on to
add an extra layer of complexity they must understand just to do something that
sbt can already do. This can be incredibly frustrating for contributors to a
project that just want to quickly contribute something, but are first forced to
understand the intricacies of your Frankenstein build definition.

Are there exceptions to this rule? Probably.

## [Do document it when you do weird shit](#do-document-it-when-you-do-weird-shit)

Picture this, you've been struggling all week on getting something to work. You
feel like you've read the entire sbt docs site, all the sbt code you could
actually understand, and then you finally come up with a solution to your
bespoke problem. It's wild, it's crazy, and it makes perfect sense to you in
that moment. For the sake of anyone coming across that code in the future, if
it's absolutely necessary to keep it, document it. Document what you tried, why
it didn't work, why you chose to do whatever you did, and over explain it. I've
seen projects do wild stuff, and that undocumented wild stuff was enough to make
them not even attempt to work on the project, even if it desperately needs it.

## [Don't add stuff to your build definition you don't understand](#dont-add-stuff-to-your-build-definition-you-dont-understand)

At times it's tempting to copy some code into your build definition, confirm it
works, and then call it a day. I encourage you not to do this. Mainly because
having parts of your build definition that you don't understand is a liability
if you need to change it but don't understand what you're changing. I've
personally found the Scala tooling community incredibly helpful and open to
questions of all levels. I've asked a lot of sbt questions in the
[sbt/sbt](https://gitter.im/sbt/sbt) gitter chat, and they almost always got
answers.

## [Conclusion](#conclusion)

After reading this you may totally disagree with some of these rules, and that's
absolutely fine. Think about them, discuss them with your team, and be
consistent in the way you apply them while also understanding there are always
exceptions to the rules. Thanks for reading.
