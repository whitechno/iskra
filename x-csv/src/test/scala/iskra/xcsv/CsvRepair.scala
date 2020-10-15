package iskra.xcsv

object CsvRepair {
  private val TARGET                 = """\",""""
  private val REPLACEMENT            = """",""""
  def repairCsv(src: String): String = src.replace(TARGET, REPLACEMENT)
}
