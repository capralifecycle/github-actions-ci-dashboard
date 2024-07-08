package test.util

/** Reads a file from the resources folder as UTF8. */
fun loadResource(filepath: String): String {
  require(!filepath.startsWith("/")) { "File path must not start with /" }

  val fileContents = object {}.javaClass.classLoader.getResource(filepath)!!.readText()

  return fileContents
}
