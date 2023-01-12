package io.kipp.site

object Extensions:

  extension [A, B](eithers: Seq[Either[A, B]])
    def sequence = eithers.partitionMap(identity) match
      case (Nil, rights)       => Right(rights)
      case (firstLeft :: _, _) => Left(firstLeft)

  extension (map: Map[String, String])(using path: os.Path)
    def getOrLeft(key: String): Either[String, String] =
      map.get(key) match
        case None        => Left(s"Missing ${key} in ${path.baseName}")
        case Some(value) => Right(value)
