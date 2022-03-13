---
title: The Debug Adapter Protocol and Scala
date: 2022-03-13
description: A description of how the Debug Adapter Protocol works with Metals and Scala.
---

# The Debug Adapter Protocol and Scala. How it all works together.

In the last couple years in Scala you've seen a surge in usage of various
tooling protocols. A growing number of developers are at least familiar with the
Language Server Protocol (LSP), especially if you're a
[Metals](https://scalameta.org/metals/) user or were an early user of Dotty when
it had a built-in LSP server. You've also more than likely been exposed to the
Build Server Protocol (BSP) even if was just in passing by seeing
[sbt](https://www.scala-sbt.org/) create a `.bsp/` directory in your Scala
workspace. Another popular protocol is the Debug Adapter Protocol, which again
you may have rubbed up against if you're using Metals. I've seen a lot of
questions about the how DAP works with Metals and this one actually has a few
more moving parts than the others to make it all work together. So, I wanted to
jot down some notes both to ensure I understand all the moving parts, to serve
as a detailed explanation of sorts of how it all works together, and to
hopefully help you to as well.

Keep in mind that this will differ a bit per client. Client here may also be a
bit ambiguous since we'll be talking about Metals client extensions and DAP
clients, so I'll try to always differentiate them by saying "Metals client" or
"DAP client" _even though they may be the same thing_.

Also keep in mind that this won't really be a technical explanation of _how_
things like expressions evaluation or breakpoints actually work, but more of an
overview of all the moving pieces to better understand what happens between all
of your tools when you trigger a `run` or `debug`.

## What is DAP

If you're familiar with the goals of the LSP, then you're already familiar with
some of the goals of DAP. Basically, don't re-implement all the debugger
functionality over and over for every new language and tool that wants to
implement debugging. Taken from the [DAP
website](https://microsoft.github.io/debug-adapter-protocol/):

> Adding a debugger for a new language to an IDE or editor is not only a
> significant effort, but it is also frustrating that this effort can not be
> easily amortized over multiple development tools, as each tool uses different
> APIs for implementing the same feature.
> The idea behind the Debug Adapter Protocol (DAP) is to abstract the way how
> the debugging support of development tools communicates with debuggers or
> runtimes into a protocol. Since it is unrealistic to assume that existing
> debuggers or runtimes adopt this protocol any time soon, we rather assume that
> an intermediary component - a so called Debug Adapter - adapts an existing
> debugger or runtime to the Debug Adapter Protocol.

> The Debug Adapter Protocol makes it possible to implement a generic debugger
> for a development tool that can communicate with different debuggers via Debug
> Adapters. And Debug Adapters can be re-used across multiple development tools
> which significantly reduces the effort to support a new debugger in different
> tools.

This description also brings up an important point of the actual Debug Adapter
since in many languages that do have existing debugger interfaces built right
into the language or platform, like the [Java Debug
Interface](https://docs.oracle.com/en/java/javase/17/docs/api/jdk.jdi/module-summary.html),
which is what [java-debug](https://github.com/microsoft/java-debug) uses, which
is what
[scala-debug-adapter](https://github.com/scalacenter/scala-debug-adapter) uses.
However, we'll get further into that down below. All that to say, the goal is
that in a situation where a VS Code user wants to transition to another editor,
_like Neovim_, they can still have the same basic debugging experience as long
as that new client has a DAP client implementation either built in or available
as a plugin. The same can be said for the server side if a Metals users decides
to use Bloop as their build server or sbt as their build server, they can share
a common implementation of the server side of the protocol and not have to fully
re-implement it twice. The server side example is exactly what the
[scala-debug-adapter](https://github.com/scalacenter/scala-debug-adapter) is
for.

## Two different types of clients

I mentioned it up above, but also want to reiterate it here. Different language
server clients may handle the DAP client part differently. Like in the case of
VS Code, the DAP client is straight up just included in the editor. You can see
an example of this in the
[scalameta/metals-vscode](https://github.com/scalameta/metals-vscode/blob/main/src/scalaDebugger.ts)
extension. Notice that the imports are coming right from `vscode`. This offers
an extremely tight integration that is totally abstracted away for the user.
There are other things that can then be built on top of that like the VS Code
Test Explorer API that [Metals recently added support
for](https://scalameta.org/metals/blog/2022/01/12/aluminium#support-test-explorer-api).
Under the hood, the communication for these are still going through DAP. I like
to think of these as "extensions" to DAP similar to LSP extensions that aren't
necessarily part of the protocol, but follow the same pattern and even re-use
parts that _are_ part of the protocol. These then require more work for other
clients to implement, and they aren't expected to work out of the box like other
DAP features. For now, we won't focus on any of these, and we'll just stick to
the core DAP features.

The other way this can look in a client is when your language server client
doesn't natively implement a DAP client, but has you use an extension to
implement this. You can see an example of this in
[scalameta/nvim-metals](https://github.com/scalameta/nvim-metals/blob/32a37ce2f2cdafd0f1c5a44bcf748dae6867c982/lua/metals/setup.lua#L109-L168).
Notice that in `setup_dap` function the first thing we actually do is require
[mfussenegger/nvim-dap](https://github.com/mfussenegger/nvim-dap) which is a
fantastic plugin that implements the client portion of the protocol for Neovim.

So, whether you're using VS Code with a built-in DAP client integration or using
Neovim and a plugin like `nvim-dap`, the core client functionality should be
largely the same. Moving forward all of examples will assume the second setup
using `nvim-dap`, since that's what I'm most familiar with.

## How does everything get set up

I don't want this to necessarily be a "getting started with nvim-dap" guide, as
there are already guides out there, plus the
[docs](https://github.com/mfussenegger/nvim-dap/blob/master/doc/dap.txt) for
`nvim-dap` are pretty detailed. Instead, I want to focus on _how_ this all works
together specifically when using `nvim-metals`. Most of this will be
transferable to other Metals extensions that support DAP as well.

Let's start with a simple piece of code:

```scala
@main def dapExample() =
  println("hello people interested in DAP")
```

If you have `nvim-dap` installed and you open a Scala project with a main method
you should see code lenses appear on your main method. In `nvim-metals` it will
look like this:

![some scala code with code lens](/images/code-lens.png)

The first question we need to answer is "how did these get here?" and then "how
does this actually trigger a run or debug of my code?". Behind the scenes what
actually happens is that Metals will have communicated with your build server
and gotten any main methods in your build target via a
[`buildTarget/scalaMainClasses`](https://build-server-protocol.github.io/docs/extensions/scala.html#scala-main-classes-request)
request and cached those results. Then when the LSP request comes to metals for
the `textDocument/codeLens` Metals looks through the
[SemanticDB](https://scalameta.org/docs/semanticdb/guide.html) for the current
document and looks for any main methods. If it finds them, it compares them to
the cached that were retrieved earlier, and then creates code lenses for them
with special commands attached to them. 

Here are some illustrations of the above.

_Example of the `buildTarget/scalaMainClasses` request and response to the build server_

```json
[Trace - 10:50:29 AM] Sending request 'buildTarget/scalaMainClasses - (7)'
Params: {
  "targets": [
    {
      "uri": "file:/Users/ckipp/Documents/scala-workspace/sanity/Sanity/test/?id\u003dSanity.test"
    },
    {
      "uri": "file:/Users/ckipp/Documents/scala-workspace/sanity/Sanity/?id\u003dSanity"
    }
  ]
}

[Trace - 10:50:29 AM] Received response 'buildTarget/scalaMainClasses - (7)' in 6ms
Result: {
  "items": [
    {
      "target": {
        "uri": "file:/Users/ckipp/Documents/scala-workspace/sanity/Sanity/?id\u003dSanity"
      },
      "classes": [
        {
          "class": "dapExample",
          "arguments": [],
          "jvmOptions": [],
          "environmentVariables": []
        }
      ]
    },
    {
      "target": {
        "uri": "file:/Users/ckipp/Documents/scala-workspace/sanity/Sanity/test/?id\u003dSanity.test"
      },
      "classes": []
    }
  ]
}
```

_Example of what the SemanticDB for our coded snippet looks like. Notice the
first occurrence which is `scala/main#`. Once found we'd get the symbol for that
occurrence and then check it against what was returned above._

```proto
Sanity/src/example/Hello.scala
------------------------------

Summary:
Schema => SemanticDB v4
Uri => Sanity/src/example/Hello.scala
Text => empty
Language => Scala
Symbols => 3 entries
Occurrences => 3 entries

Symbols:
_empty_/Hello$package. => final package object _empty_ extends Object { self: _empty_.type => +2 decls }
_empty_/Hello$package.dapExample(). => @main method dapExample(): Unit
_empty_/dapExample# => final class dapExample extends Object { self: dapExample => +2 decls }

Occurrences:
[0:1..0:5) => scala/main#
[0:10..0:20) <= _empty_/Hello$package.dapExample().
[1:2..1:9) => scala/Predef.println(+1).

```

_Example of the code lens request and response._

```json
[Trace - 07:22:25 PM] Received request 'textDocument/codeLens - (92)'
Params: {
  "textDocument": {
    "uri": "file:///Users/ckipp/Documents/scala-workspace/sanity/Sanity/src/example/Hello.scala"
  }
}

[Trace - 07:22:25 PM] Sending response 'textDocument/codeLens - (92)'. Processing request took 1ms
Result: [
  {
    "range": {
      "start": {
        "line": 0,
        "character": 1
      },
      "end": {
        "line": 0,
        "character": 5
      }
    },
    "command": {
      "title": "run",
      "command": "metals-run-session-start",
      "arguments": [
        {
          "targets": [
            {
              "uri": "file:/Users/ckipp/Documents/scala-workspace/sanity/Sanity/?id\u003dSanity"
            }
          ],
          "dataKind": "scala-main-class",
          "data": {
            "class": "dapExample",
            "arguments": [],
            "jvmOptions": [],
            "environmentVariables": []
          }
        }
      ]
    }
  },
  {
    "range": {...},
    "command": {
      "title": "debug",
      "command": "metals-debug-session-start",
      "arguments": [...]
    }
  }
]
```

While the above is to generate the `run` and `debug` code lenses, more or less
the same process happens for the `test` and `test-debug` lenses as well. The
commands that are attached to the code lenses are LSP client commands that need
to be implemented by the client. As you can probably guess, the two commands
`metals-run-session-start` starts just a normal run and a
`metals-debug-session-start` starts a debug session. How that's done however
differs a bit by client. Since we're focusing on `nvim-metals` and `nvim-dap`
I'll outline a bit of what is happening behind the scenes to tie everything
together.

Either of the commands will end up calling this function:

```lua
local function debug_start_command(no_debug)
  return function(cmd, _)
    dap.run({
      type = "scala",
      request = "launch",
      name = "from_lens",
      noDebug = no_debug,
      metals = cmd.arguments,
    })
  end
end
```

With `nvim-dap` there are two main concepts around configuration that are
important to grasp. The first is the adapter configuration. This is a table
given to `nvim-dap` per language (although the keys are actually arbitrary, just
think of it per language) that basically tells `nvim-dap` if it should launch a
debug adapter and if so how, or if it should connect to a running debugger and
if so where. This configuration can be a table with these details or a function
that takes a callback and a configuration. In the case of `nvim-metals` we use
the latter with the callback, which will be explained further below. The second
configuration that is relevant here is the debugee configuration which is the
configuration for your application you'll be debugging. So if you're familiar
with VS Code think of this as your `launch.json`. `nvim-dap` can actually work
using a `launch.json`, but we won't focus on that here.

So in the above `debug_start_command` function the table being passed into
`dap.run()` is your debugee configuration. The `type` is a reference to the
adapter entry that matches this key, the `request` is either `attach` or
`launch` indicating whether the debug-adapter should launch or attach to a
debuggee, the `name` is a human readable name for the configuration (which we'll
revisit), and the `noDebug` is whether or not debug mode should be enabled. If
this is `true` breakpoints will be ignored. Finally, the `metals` key isn't part
of the spec here and will actually be removed before being passed to `nvim-dap`.
However we use it to be able to forward the arguments from the code lens to the
adapter configuration.

So where is the adapter configuration? `nvim-metals` fully handles the adapter
configuration for you. The main reason for this is that before we can actually
launch everything we need some information from Metals about how to connect to
the debugger. In order to get this, we again utilize LSP to get this information
before we actually start any DAP communication. Keep in mind we've already dove
into quite a few things, but no DAP communication has even started yet. This is
where the callback being part of the adapter configuration comes into play. When
we setup the adapter configuration it looks something like this:


```lua
dap.adapters.scala = function(callback, config)
  local uri = vim.uri_from_bufnr(0)
  local arguments = {}

  if config.name == "from_lens" then
    arguments = config.metals
  else
    local metals_dap_settings = config.metals or {}

    arguments = {
      path = uri,
      runType = metals_dap_settings.runType or "run",
      args = metals_dap_settings.args,
      jvmOptions = metals_dap_settings.jvmOptions,
      env = metals_dap_settings.env,
      envFile = metals_dap_settings.envFile,
    }
  end

  execute_command({
    command = "metals.debug-adapter-start",
    arguments = arguments,
  }, function(_, _, res)
    if res then
      local port = util.split_on(res.uri, ":")[3]

      callback({
        type = "server",
        host = "127.0.0.1",
        port = port,
        enrich_config = function(_config, on_config)
          local final_config = vim.deepcopy(_config)
          final_config.metals = nil
          on_config(final_config)
        end,
      })
    end
  end)
end
```

Let's walk through this. We first check the `config.name` and if it's
`from_lens` we know that this whole process was started from triggering a code
lens, so we grab everything in the metals key and set that to `arguments` that
will actually be sent to metals along with the `metals.debug-adapter-start`
command. Let's ignore the `else` branch if the name isn't `from_lens` since
we're focusing on the code lens example here. The `execute_command` function
will send the LSP command to Metals which will then over BSP tell the build
server to start the debug server. Your debug server is (or if it's not it should
be) using
[scalacenter/scala-debug-adapter](https://github.com/scalacenter/scala-debug-adapter)
to start and manage the debug server. Then the `res` that is returned via BSP
will have the information we need, mainly the `host` and `port` of the server
that has already been started. This is then forwarded over LSP back to
`nvim-metals`. The `type` here is now set to `server` since there is already a
debugger running that we just want to connect to. The `enrich_config` takes in
the debugee configuration that we created before and strips the `metals` key out
since it's no longer relevant for the actual run, and not part of DAP.

At this point is when DAP communication actually starts. However, there is a
fair amount of stuff that has already happened. To recap all of this, here is a
diagram showing what we've all covered.

![all communication between nvim-metals, nvim-dap, and
metals](/images/dap-setup.svg)

## The actual DAP communication

So after all of the set up above happens you're ready to actual have some DAP
communication. At this point in `nvim-metals` `nvim-dap` pretty much fully takes
over on the client side and communicates directly with the running debugger
which was started by your build server. Given the code we had earlier the
general flow of DAP communication isn't that interesting since we don't have any
breakpoints set, we aren't doing expression evaluation, conditionals etc. To
outline what the communication looks like between DAP client and DAP server,
here is another diagram.

![dap communication](/images/dap-communication.svg)

While the diagram above isn't actually exhaustive it contains the main events
that get the point across of how the DAP server and client are communicating.
The communication here can also become much more complicated when start adding
in breakpoints. For example, let's pretend our code is slightly different than
what we had above with a single breakpoint set:

```scala
@main def dapExample() =
  val greeting = "hello people interested in DAP"
  println(greeting) // breakpoint set on this line
```

If we set the breakpoint above where mentioned, it will pause at that point. In
`nvim-dap` you can execute a `.scopes` command in the debug REPL and you'll see
the variables in scope returned. The communication between the DAP server and
client for this looks like so:

```json
[Trace][03:21:33 PM] Sent request:
{
  "type": "request",
  "seq": 9,
  "command": "variables",
  "arguments": {
    "variablesReference": 3
  }
}
[Trace][03:21:33 PM] Received response:
{
  "type": "response",
  "seq": 14,
  "request_seq": 9,
  "command": "variables",
  "success": true,
  "body": {
    "variables": [
      {
        "name": "greeting",
        "value": "\"hello people interested in DAP\"",
        "type": "String",
        "variablesReference": 4,
        "namedVariables": 0,
        "indexedVariables": 0
      },
      {
        "name": "this",
        "value": "Hello$package$@183",
        "type": "Hello$package$",
        "variablesReference": 5,
        "namedVariables": 0,
        "indexedVariables": 0
      }
    ]
  }
}
```

You can see all sorts of examples of the communication that can take place if
you look through the types of events in the [specification
documentation](https://microsoft.github.io/debug-adapter-protocol/specification).

## Without code lenses

So let's revisit the function from up above that we gave to
`dap.adapters.scala` and now take a look at the `else` branch that we ignored
before. One thing you may have noticed with the code lens is that it's
fully handled by `nvim-metals` without really a great way to maybe set some
arguments that you'd like to pass into your `run` or maybe some specific
jvmOptions. The recommended way to do this with `nvim-metals` is to pre-define
your debugee configuration. Let's say you wanted to trigger a run with a
specific argument and also a specific env file. You could defined a
configuration like this:

```lua
dap.configurations.scala = {
  {
    type = "scala",
    request = "launch",
    name = "Run with arg and env file",
    metals = {
      runType = "runOrTestFile",
      args = { "myArg" },
      envFile = "path/to/.env",
    },
  },
```

NOTE: again, some of this is `nvim-dap` specific, so if you're trying to follow
along, make sure to read through the
[docs](https://github.com/mfussenegger/nvim-dap/blob/master/doc/dap.txt) and
also take a look at the full [example
configuration](https://github.com/scalameta/nvim-metals/discussions/39) for
`nvim-metals` users.

So remembering the function from up above, the `metals` key here won't actually
get passed into `nvim-dap` at all, but is instead used to send the correct stuff
to Metals to ensure the debug server is set up correctly and the correct things
like args are taken into account. So now when you'd trigger a `dap.continue()`
(it's called continue but will start a session if none exists) you'll see this
configuration as an option to use to start the process under `Run with arg and
env file`. If you have 3 different ones defined, maybe one with args, one
without, and one specifically for tests, you'd see those three to choose from.
Here is an example of what mine looks like locally when I trigger it:

![example of my run configurations](/images/run-configurations.png)

The communication here is slightly different than when using the code lens since
at this point we're triggering the process to start, but we don't _actually_
know for sure if we're in a file that even has a main method. So some of the
same steps from above happen, but instead the order is a bit different. The
`runType` key here is special functionality in Metals to search the current
document you're in to see if there are any many methods or tests that it can
run. If there is, it then goes ahead and runs it, or returns you a picker to
select which you'd like if there are multiple mains or test suites to choose
from. Here is a diagram showing the changed order of things when you trigger a
`run` this way.

![debug discover communication](/images/debug-discovery.svg)

## I'm amazed this all works

Even without going into the technical details of how each part of this works,
it's incredible that it even does with the amount of moving parts, different
protocols involved, and different tools being utilized. It speaks to the amount
of work that goes on by many different people working in different projects to
ensure when you click `run` in your editor, you can run your code. Hopefully it
also explains a bit why when something may not be working _exactly_ how you want
it to, there is a lot that can go wrong in various places. There's a lot of
extra things related to DAP that we haven't hit on in here that could each be a
post of their own.

I hope this was insightful. Thanks for reading along.
