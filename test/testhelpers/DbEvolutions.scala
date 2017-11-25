package testhelpers

import org.scalatestplus.play.{AppProvider, PlaySpec}
import play.api.db.DBApi
import play.api.db.evolutions.Evolutions
import play.api.test.Injecting

trait DbEvolutions extends PlaySpec with AppProvider with Injecting {

  def withEvolutions[T](block: => T): T = {
    val dbApi = inject[DBApi]
    val db = dbApi.database("default")
    Evolutions.withEvolutions(db)(block)
  }

}
