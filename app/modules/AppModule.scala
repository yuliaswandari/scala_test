package modules

import play.api.inject.{Binding, Module}
import play.api.{Configuration, Environment}
import repositories.{KecamatanRepository, KabupatenRepository, KunjunganRepository}

class AppModule extends Module {
  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = {
    Seq(
      bind[KabupatenRepository].toSelf,
      bind[KecamatanRepository].toSelf,
      bind[KunjunganRepository].toSelf
    )
  }
}
