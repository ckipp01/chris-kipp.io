---
title: Claude Code, Metals, and NVIM
date: 2025-08-13
description: A look at a terminal based AI powered workflow with claude code and
Metals in Neovim
---

# An overview of using Claude Code, Metals, and NVIM

I hated AI assisted development for a long time.

I remember when Copilot was first released and people lost their minds. This was
apparently the beginning of the end for human developers. The next couple years
new tools came out constantly promising a revolution in developer productivity
and replacement... I hated it. All my initial attempts at using these tools were
awful. Either I felt like I wasn't in control, the editor integrations for nvim
were terrible, or I was just unsatisfied with the results. When new promising
editors were released that were more AI-centric I couldn't find it in myself to
pull away from a setup that I felt so invested and productive in.

I started a new job earlier this year that is quite AI-centric in their approach
to development, not in an overpowering hype-driven way, but in a _this should
make you more productive... use it_ sort of way. While skeptical at first, I've
found in the past few months that I've landed on a flow developing Scala that
I'm quite happy with that allows me to stay in my comfortable nvim/tmux/terminal
environment while benefiting from assistance that still allows me to feel like
I'm in control and boosts my productivity.

## My Setup

I still live in the terminal for development. On a daily basis developing Scala
I'm using [Metals](https://github.com/scalameta/metals) through
[nvim-metals](https://github.com/scalameta/nvim-metals). I heavily use [claude
code](https://github.com/anthropics/claude-code) with Metals as an MCP server,
and that's what I'd like to talk about and illustrate here. If you're not
familiar with MCP servers, the basic idea is that they expose a set of tools
that can be used from your agent when the deem it appropriate.

As of Metals [v1.5.3](https://scalameta.org/metals/blog/2025/05/13/strontium)
Metals can act as an [MCP Server](https://modelcontextprotocol.io/overview).
This matters because it can greatly speed up your workflow for some of the most
common things you'll find claude doing:

- make sure it quickly and accurately finds what it needs in your code base
- making sure you code compiles after it changes it
- make sure you tests pass after your changes

I get giddy about protocols and things working together, and I love the way that
this ends up working on the hood. Let's assume a couple things in the following
flow:

1. You're using your build tool as your server (let's say sbt). This matters
    because whether you're doing work in your editor, running things from the
    tool from the command line, or having Metals compile your code for you via the
    MCP tools, your code is always in the same compilation state. I'll illustrate
    this down below further. If you didn't know there is a setting to tell
    Metals to default to this if your build tool supports it instead of relying on
    bloop. In your metals settings just set `defaultBspToBuildTool` to `true`. I
    recommend you do this regardless of whether your using Metals as an MCP server
    or not.
2. You're starting Metals as an MCP server. You need to set `startMcpServer` to
   `true` in your settings for this.
3. This example uses claude code, so in order for Metals to write the correct
    config file so claude picks it up when you start it, you'll want to set
    `mcpClient` to `claude` in your settings.

While I use other MCP tools at work, we'll focus on this flow to illustrate the
benefits of Metals here. If you have everything setup like I do above when you
start Metals by opening your project you should be greeted with a nice little
message like this:

```text
LSP[metals][Info] Metals MCP server started on port: 64402. Refresh connection if needed.
```

And then if you start `claude` in the root of that same directory you should
see a little prompt with the message:

```text
New MCP server found in .mcp.json: metals 
```

And then you need to respond if you want to allow it or not. This choice is
saved then for your workspace.

It knows to start this because you'll notice a newly create `.mcp.json` file at
the root of your project:

```json
{
  "mcpServers": {
    "metals": {
      "url": "http://localhost:54640/sse",
      "type": "sse"
    }
  }
}
```

## Letting Metals do its thing

for these examples i'll use the [metals](https://github.com/scalameta/metals)
codebase, which isn't huge, but it illustrates this well. let's pretend that you
are using another editor that doesn't use bsp to edit your code meaning that
while you're editing your code it's fine, but if you switch to sbt, you need to
re-compile your code. the same can be said for using bsp with bloop instead of
sbt as well. even though your code is fully compiled with metals and with bloop,
when you drop down into sbt you need to recompile your code. in my opinion this
is one of the biggest benefits of defaulting to your build tool to be your build
server: you don't need to pay that cost of compiling twice. so down below, what
you're seeing is claude issuing a command to compiling your code with sbt.

```text
⏺ The compilation completed but with some errors at the end. Here are the results:

  Total compilation time: 48.052 seconds (48s)

  The compilation mostly succeeded - it compiled all the main modules including:
  - mtags-interfaces
  - sbt-metals
  - mtags-shared
  - mtags-java
  - mtags
  - metals (main module)
  - tests modules
  - metals-docs
  - metals-bench

  The timing breakdown shows:
  - Real time: 48.052 seconds
  - User CPU time: 131.41 seconds
  - System CPU time: 7.94 seconds
  - CPU utilization: 289% (indicating parallel compilation)
```

However realistically part of your code will have already been compiled, but so
incrementally compilation will have kicked in and it will be a bit faster than
this. I'll have claude just add a single method to a file and then recompile and
report back.

```text
⏺ Incremental compilation time: 7.307 seconds (7s)

  The incremental compilation was much faster! Here's the comparison:

  - Full compilation (first run): 48.052 seconds
  - Incremental compilation (after small change): 7.307 seconds

  The incremental compilation only needed to recompile:
  - 1 Scala source in the metals module (the Time.scala file I changed)
  - 3 Scala sources in the metals module (likely dependent files)
  - 1 Scala source in the tests/unit module (probably a test that uses Time)

  This demonstrates SBT's incremental compilation working effectively - it only recompiled the changed file and its dependents, rather than the entire codebase, resulting in
  an 85% reduction in compile time.
```

Now realistically it's fair to say that it'll be changing more code than this.
So we'll use 7 seconds as the bottom datapoint to prove the point that every
time claude is dropping down to issue a bash command to re-compile your code
it's going to at least take this much time.

Now if you're using Metals as your LSP server with MCP enabled and it's hooked
up to claude every change that claude is making to your codebase ends up being
registered and accounted for (although this does see to have slightly
differently behavior depending on your editor for some reason if you read the
issue [here](https://github.com/scalameta/metals/issues/7662)). In general
Metals doesn't just rely on notifications from the LSP client about file changes
but it also has it's own file watcher. When Metals changes a file even without
any LSP notification being sent this basic flow will happen:


 1. OS detects file change → PathWatcher receives event
 2. File filter check → watchFilter(path) validates if file should trigger events
 3. Event queued → FileWatcherEvent.createOrModify(path) added to BlockingQueue
 4. Callback triggered → didChangeWatchedFiles(event) called
 5. onCreate invoked → onCreate(path) in MetalsLspService.scala
 6. Compilation triggered → compilers.didChange(path, false) in MetalsLspService.scala

 So as claude is making changes your Metals state is consistently compiling and
 being updated to match what's happening on disk. Due to this if you get to a
 point where claude tries to compile your code it's already fully compiled and
 should basically return almost instantaneous with either no errors letting you
 know your code is fine or with diagnostics about what is wrong. The MCP logs
 for this looks like this:

```json
[Trace - 18:16:29]  Sending request
{"method":"tools/call","params":{"name":"compile-full","arguments":{},"_meta":{"claudecode/toolUseId":"toolu_01ENHHfWxBMUZXTWFxEyASyg"}},"jsonrpc":"2.0","id":2}
[Trace - 18:16:29]  Received response
event: message

[Trace - 18:16:29]  Received response
data: {"jsonrpc":"2.0","id":2,"result":{"content":[{"type":"text","text":"Compilation successful."}],"isError":false}}
```

And if you have issues:

```json
[Trace - 18:17:08]  Sending request
{"method":"tools/call","params":{"name":"compile-full","arguments":{},"_meta":{"claudecode/toolUseId":"toolu_01DJYsYhGJbh6KQm2nbZ9nWX"}},"jsonrpc":"2.0","id":3}
[Trace - 18:17:08]  Received response
event: message

[Trace - 18:17:08]  Received response
data: {"jsonrpc":"2.0","id":3,"result":{"content":[{"type":"text","text":"Compilation failed with errors:\nmetals/src/main/scala/scala/meta/internal/metals/watcher/FileWatcherEvent.scala L13-L13:\n'=' expected."}],"isError":false}}
```

Again, in both of these scenarios the compile command was basically a no-op and
Metals was able to return immediately with information so that claude can either
move forward with validation knowing nothing is wrong, or to start and fix
whatever issue it introduced. The time this alone saves in a long editing
session is immense.

This same concept can be applied for testing. If your codebase is already fully
compiled then starting a test should also basically be instantaneous.

```text
> Can you run the ScalaVersionsSuite test with Metals

⏺ metals - test (MCP)(testClass: "tests.ScalaVersionsSuite")
  ⎿  
     tests.ScalaVersionsSuite:
       + idempotent-minor-release passed
     … +55 lines (ctrl+r to expand)

⏺ All 54 tests in ScalaVersionsSuite passed successfully. The test suite completed in 42ms with
  no failures.
```

Behind the scenes the request in MCP looks like this:

```json
[Trace - 18:20:43]  Sending request
{"method":"tools/call","params":{"name":"test","arguments":{"testClass":"tests.ScalaVersionsSuite"},"_meta":{"claudecode/toolUseId":"toolu_01BXjFLMVkLjAEQuwwjAobSh"}},"jsonrpc":"2.0","id":4}
[Trace - 18:20:43]  Received response
event: message

[Trace - 18:20:43]  Received response
data: {"jsonrpc":"2.0","id":4,"result":{"content":[{"type":"text","text":"\ntests.ScalaVersionsSuite:\n  + idempotent-minor-release passed\n  + retain-pre-release-version passed\n  + drop-typelevel-vendor-suffix passed\n  + recommended-future passed\n  + recommended-212 passed\n  + recommended-211 passed\n  + recommended-213 passed\n  + future-213 passed\n  + not-future-213 passed\n  + future-212 passed\n  + not-future-212 passed\n  + not-future-211 passed\n  + future-214 passed\n  + future-315 passed\n  + 2.12.11-comapatible-with-2.12.5 passed\n  + 2.12.5-not-compatible-with-2.12.11 passed\n  + 2.12.7-compatible-with-2.12.5 passed\n  + 2.12.5-not-compatible-with-2.12.7 passed\n  + 2.12.11-compatible-with-2.11.12 passed\n  + 2.11.12-not-compatible-with-2.12.11 passed\n  + 0.9.10-not-compatible-with-0.10.0-M4 passed\n  + 0.9.10-not-compatible-with-0.10.0-RC2 passed\n  + recommended-3 passed\n  + compare-3.0.0-M1<=3.0.0-M2 passed\n  + compare-3.0.0-M2>=3.0.0-M1 passed\n  + compare-3.0.0-RC1<=3.0.0-RC2 passed\n  + compare-3.0.0-RC2>=3.0.0-RC1 passed\n  + compare-3.0.0-RC1<=3.0.0 passed\n  + compare-3.0.0>=3.0.0-RC1 passed\n  + compare-3.0.0-M1<=3.0.0 passed\n  + compare-3.0.0>=3.0.0-M1 passed\n  + compare-3.0.0-RC1<3.0.0 passed\n  + compare-3.0.0>3.0.0-RC1 passed\n  + compare-3.0.0-M1<3.0.0 passed\n  + compare-3.0.0>3.0.0-M1 passed\n  + compare-3.0.0-M1<3.0.0-RC1 passed\n  + compare-3.0.0-RC1>3.0.0-M1 passed\n  + compare-RC1<=RC1-SNAPSHOT passed\n  + compare-RC2>=RC1-SNAPSHOT passed\n  + compare-RC1<RC2-SNAPSHOT passed\n  + compare-RC2>RC1-SNAPSHOT passed\n  + compare-NIGTLY passed\n  + not-future-3-M1 passed\n  + not-future-3-M2 passed\n  + not-future-RC passed\n  + smth-library_2.13-21.2.0-sources.jar passed\n  + scala3-compiler_3-3.0.1-RC2-bin-20210310-4af1386-NIGHTLY-sources.jar passed\n  + scala3-library_3-3.1.0-RC1.jar passed\n  + scala-library-2.13.1.jar passed\n  + cool4.4_2.13-3.0.jar passed\n  + scala3-library_3-3.0.0-sources.jar passed\n  + munit_3-0.7.29-sources.jar passed\n  + tested-3.0-sources.jar passed\n  + newer-RC passed\nExecution took 42ms\n54 tests, 54 passed, 0 failed, 0 skipped\n"}],"isError":false}}
```

The cool (or not so cool if you hate how many moving pieces there are) thing is
that the Debug Adapter Protocol (DAP) is also being used here behind the scenes.
When this request comes into Metals the flow is:

 The flow:
 1. Claude Request → MCP (mcp__metals__test tool)
 2. MCP → Metals (McpTestRunner.runTests)
 3. Metals → DAP (TestSuiteDebugAdapter creation)
 4. DAP → Build Server (BSP test execution request)
 5. Build Server executes tests via sbt/mill/whatever build server you're using
 6. Build Server → DAP (test results)
 7. DAP → Metals (formatted results)
 8. Metals → MCP (test summary)
 9. MCP → Claude (final response)

Or visually:

```text
┌─────────┐    ┌─────────┐    ┌─────────┐    ┌─────────┐    ┌──────────────┐
│ Claude  │───▶│   MCP   │───▶│ Metals  │───▶│   DAP   │───▶│ Build Server │
│ Request │    │Tool Call│    │  LSP    │    │ Adapter │    │     (BSP)    │
└─────────┘    └─────────┘    └─────────┘    └─────────┘    └──────────────┘
     ▲              ▲              ▲              ▲                  │
     │              │              │              │                  │
     │              │              │              │                  ▼
     │              │              │              │         ┌──────────────┐
     │              │              │              │         │ Test Run     │
     │              │              │              │         │   (sbt/etc)  │
     │              │              │              │         └──────────────┘
     │              │              │              │                  │
     │              │              │              │                  │
┌─────────┐    ┌─────────┐    ┌─────────┐    ┌─────────┐             │
│ Claude  │◀───│   MCP   │◀───│ Metals  │◀───│   DAP   │◀────────────┘
│Response │    │Response │    │ Results │    │ Results │
└─────────┘    └─────────┘    └─────────┘    └─────────┘

```

These two feature alone have saved me a ton of time. There are also more tools
that can be used in a variety of ways. If you're using claude you can view these
via `/mcp` and selecting metals. The existing tools as of today are:

1. compile-file
2. compile-module
3. compile-full
4. test
5. glob-search
6. typed-glob-search
7. inspect
8. get-docs
9. get-usages
10. import-build
11. find-dep
12. list-modules
13. format-file

Many of these can be used either as alternatives to things like web-search for
finding deps, hooks for formatting, or finding actual usages of symbols across
your codebase in a way that can not only speed up your workflow, but provide you
a more accurate one.

To summarize, if you use Metals and you use a tool like claude, use Metals as an
MCP serer. It will save you some time.
