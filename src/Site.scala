package io.kipp.site

final case class Site(url: String, owner: String, topics: Set[String])

// TODO get this to work. When I try to use this I get
// Topic: DownArray,DownField(topics),DownArray,DownField(items)
// Exception in thread "main" java.lang.RuntimeException: puking when trying to go from json to your case class in sites
//enum Topic:
//  case art
//  case books
//  case `build tools`
//  case compilers
//  case computing
//  case databases
//  case food
//  case music
//  case photography
//  case scala
//  case thoughts
//  case typography
//  case web
