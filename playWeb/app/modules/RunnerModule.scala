package modules

import com.google.inject.AbstractModule
import controllers.ZioRunner
import controllers.ZioRunnerDefault

class RunnerModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[ZioRunner]).to(classOf[ZioRunnerDefault])
  }
}

