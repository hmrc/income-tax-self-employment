import wartremover.Wart

object WartRemoverSettings {

  lazy val warts: Seq[Wart] = Seq(
    Wart.ArrayEquals,
    Wart.ExplicitImplicitTypes,
    Wart.MutableDataStructures
    // Not yet ready to enable it
    //    Wart.Enumeration
    //    Wart.LeakingSealed,
    //    Wart.OptionPartial,
    //    Wart.Throw,
  )
}
