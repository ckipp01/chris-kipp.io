//> using scala "3.1.1"
//> using lib "org.scala-lang::scala3-compiler:3.1.1"

import java.io.PrintWriter
import java.io.File

import dotty.tools.dotc.config.ScalaSettings

@main def showSettings() =
  val pw = new PrintWriter(new File("scala3-scalac-options.txt"))
  pw.write("scala 3 scalac options for 3.1.1\n")
  pw.write("--------------------------------\n")
  val allSettings = new ScalaSettings().allSettings
  allSettings.map { setting =>
    pw.write(s"${setting.name}\n")
    pw.write(s"\t${setting.description}\n")
  }
  pw.close


