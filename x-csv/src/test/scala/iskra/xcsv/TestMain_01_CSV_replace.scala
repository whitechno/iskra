package iskra.xcsv

// x-csv / Test / runMain iskra.xcsv.TestMain_01_CSV_replace
object TestMain_01_CSV_replace extends App {

  val target      = """\",""""
  val replacement = """",""""
  println(target + " -> " + replacement)

  val from1 = """"vvob - cemastea additional ip\","accesskenya group ltd""""
  val to1   = from1.replace(target, replacement)
  println(from1 + " -> " + to1)

}
