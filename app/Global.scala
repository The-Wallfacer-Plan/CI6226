import play.api._
import play.api.mvc.{RequestHeader, Result}

import scala.concurrent.Future

object Global extends GlobalSettings {
  override def onError(request: RequestHeader, ex: Throwable): Future[Result] = {
    Logger.error("=" * 80)
    ex.printStackTrace()
    Logger.error("=" * 80)
    super.onError(request, ex)
  }
}