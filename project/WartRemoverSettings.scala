import wartremover.Wart

object WartRemoverSettings {

  lazy val warts: Seq[Wart] = Seq(
    Wart.ArrayEquals,
    Wart.ExplicitImplicitTypes,
    Wart.MutableDataStructures,
    Wart.Null,
    //    Wart.OptionPartial, // Not yet ready to enable it
    //    Wart.Enumeration, // Not yet ready to enable it
    //    Wart.Throw, // Not yet ready to enable it
    //    Wart.LeakingSealed, // Not yet ready to enable it
  )
}
