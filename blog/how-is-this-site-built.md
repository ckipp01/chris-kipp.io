---
title: How is this site built
date: 2020-02-23
description: A short overview of how this site is built.
updated: 2021-04-04
---

# How is this site built?

If you just want to know exactly how this site is built and don't care about the
history, skip down to the [current-process](#the-current-process) section. If
not, I'll take this opportunity to actual outline the different sites that I've
had along the way leading up to this one as they've radically changed along the
way.

## Version 1

I originally had a site that was fully JavaScript powered, and built from
scratch. I even created a tiny micro-framework that I used to build it called
[gyul](https://github.com/ckipp01/gyul). I had a lot of fun with that build, but
it was a bit slow on initial load and wouldn't work for visitors that had JS
disabled. However I look back at it fondly since it was my first time truly
building something that was mine, and it also got me away from using other
blogging platforms. This also corresponded with a project I started called
`ándaga` which was a way for me to track my own free-time to try and find
patterns. Everything was integrated.

## Version 2
As I got more into Scala, I wanted to see if I could generate my site using that
instead of JS. This lead me to a collection of tools and a process that I'll
outline below.

  1. Create a log via [ándaga-cli](https://github.com/ckipp01/andaga) for a task
     that I was working on.
  2. Log gets stored into [MongoDB](https://www.mongodb.com/) by
     [ándaga-core](https://github.com/ckipp01/andaga-core), which are some
     serverless functions that were running on Vercel
  3. Nightly, I had a script that ran which dumped my DB into a JSON file. This
     JSON got committed and pushed to the
     [chronica](https://github.com/ckipp01/chronica) repo, which was the repo of
     my website.
  4. The push triggered GitHub Actions to run, which ran
     [Ammonite](https://ammonite.io), [mdoc](https://scalameta.org/mdoc/), and
     [CommonMark](https://github.com/atlassian/commonmark-java) to transform and
     enrich the markdown into html.
  5. After building the created html pages got uploaded and hosted on
     [Vercel](https://vercel.com)
  6. A similar process happened when I add a blog post and push it up manually

This was also a really fun project to set up and learn more about various tools.
However, it was also a bit of a hassle to manage if I ever wanted to change
anything. It was sort of like I built the entire system, and then if I wanted to
change something, the entire system needed to change. During this time I also
stopped logging my free time, which is a whole other story, and also just wanted
to simplify the process further. That's what lead me to my current process.

## The current process

The way that the site is currently built is quite simple. It just uses
[Pandoc](https://pandoc.org) to transform my markdown pages you see in the repo
into html pages. I have some custom html partials that include the various parts
of the site and the style. The entire thing is just built with [this
script](https://github.com/ckipp01/chris-kipp.io/blob/main/bin/make-site.sh) via
[GitHub Actions](https://github.com/ckipp01/chris-kipp.io/actions), and then
automatically deployed to [Vercel](https://vercel.com). So far I've found that
this site is incredibly fast and easy to manage. Pandoc is also a joy to work
with. That's pretty much it, and that's all I really want.

Thanks for stopping by.

Chris
