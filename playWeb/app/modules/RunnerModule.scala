package modules

import com.google.inject.AbstractModule
import zio.Unsafe
import ebarrientos.deckStats.run.ZioRunner
import ebarrientos.deckStats.run.ZioRunnerDefault

class RunnerModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[ZioRunner]).toInstance(runnerInstance())
  }

  private def runnerInstance(): ZioRunnerDefault = {
    Unsafe.unsafe { implicit unsafe =>
      new ZioRunnerDefault()
    }
  }
}

