import models.common.Helper._
import play.api.http.HttpErrorHandler
import play.api.mvc.RequestHeader
import play.api.mvc.Results._

import scala.concurrent.Future

class ErrorHandler extends HttpErrorHandler {
  def onClientError(request: RequestHeader, statusCode: Int, message: String) = {
    Future.successful(
      Status(statusCode)("client error:\n" + message)
    )
  }

  def onServerError(request: RequestHeader, exception: Throwable) = {
    Future.successful {
      InternalServerError(s"server error:\n${getStackTrack(exception)}")
    }
  }
}
