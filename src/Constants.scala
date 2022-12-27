package io.kipp.site

object Constants:
  // I don't like this, but you apparently can't list the files in a resourcePath
  // from os.lib. So we assume this sort of forces us to ensure that we run commands
  // from the root of the project, not in src, not anywhere else.
  final val BLOG_DIR = os.Path("./blog/", os.pwd)
  final val SITE_DIR = os.Path("./site/", os.pwd)
  final val TALKS_FILE = os.Path("./talks.yml", os.pwd)
