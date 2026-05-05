package io.kipp.site

enum Category:
  case Tooling, Scala, Music, Personal, Meta

object Category:
  def fromString(s: String): Option[Category] = s.trim.toLowerCase match
    case "tooling"  => Some(Tooling)
    case "scala"    => Some(Scala)
    case "music"    => Some(Music)
    case "personal" => Some(Personal)
    case "meta"     => Some(Meta)
    case _          => None

  extension (c: Category)
    def key: String = c.toString.toLowerCase
    def label: String = c.toString.toLowerCase
    def hue: Int = c match
      case Tooling  => 28
      case Scala    => 355
      case Music    => 260
      case Personal => 150
      case Meta     => 210
