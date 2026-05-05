package io.kipp.site

enum Series:
  case MusicOfTheMonth, Lately

object Series:
  def fromString(s: String): Option[Series] = s.trim.toLowerCase match
    case "music of the month" => Some(MusicOfTheMonth)
    case "lately"             => Some(Lately)
    case _                    => None

  extension (s: Series)
    def name: String = s match
      case MusicOfTheMonth => "Music of the Month"
      case Lately          => "Lately"
