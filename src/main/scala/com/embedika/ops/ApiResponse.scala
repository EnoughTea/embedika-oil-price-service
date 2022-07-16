package com.embedika.ops

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

import akka.http.scaladsl.marshalling.{Marshal, Marshaller}
import akka.http.scaladsl.model.*
import akka.http.scaladsl.server.Directives.{complete, onSuccess}
import akka.http.scaladsl.server.Route


/** Something that can convert result of an API call to a HTTP response */
trait ApiResponse {
  val httpStatusCode: StatusCode

  def toHttpResponse(implicit cpuEc: CpuExecutionContext): Future[HttpResponse]
}


/** Helper class to easily respond with HTTP responses from API logic. */
object ApiResponse {
  def ok(message: String = ""): MessageResponse = MessageResponse(message, StatusCodes.OK)

  def notFound(message: String = ""): MessageResponse = MessageResponse(message, StatusCodes.NotFound)

  def badRequest(message: String = ""): MessageResponse = MessageResponse(message, StatusCodes.BadRequest)

  def entity[T](entity: T, httpStatusCode: StatusCode = StatusCodes.OK)(implicit
      marshaller: Marshaller[T, RequestEntity]
  ): EntityResponse[T] =
    EntityResponse(entity, httpStatusCode)

  def error(throwable: Throwable): MessageResponse = error(throwable.getMessage)

  def error(message: String = ""): MessageResponse = MessageResponse(message, StatusCodes.InternalServerError)

  def completeWith(eventualApiResponse: Future[ApiResponse])(implicit ec: CpuExecutionContext): Route = {
    val eventualApiResponseWithWrappedErrors = eventualApiResponse recover { case NonFatal(e) => ApiResponse.error(e) }
    onSuccess(eventualApiResponseWithWrappedErrors)(apiResponse => complete(apiResponse.toHttpResponse))
  }
}


/** Represents a response with a marshalled entity (to JSON, usually). */
final case class EntityResponse[T](entity: T, httpStatusCode: StatusCode = StatusCodes.OK)(implicit
    marshaller: Marshaller[T, MessageEntity]
) extends ApiResponse {
  override def toHttpResponse(implicit cpuEc: CpuExecutionContext): Future[HttpResponse] =
    toMessageEntity() map (HttpResponse(httpStatusCode, Nil, _))

  def toMessageEntity()(implicit ec: ExecutionContext): Future[MessageEntity] =
    Marshal(entity).to[MessageEntity]
}


/** Represents plain response. */
final case class MessageResponse(message: String, httpStatusCode: StatusCode = StatusCodes.OK) extends ApiResponse {
  override def toHttpResponse(implicit cpuEc: CpuExecutionContext): Future[HttpResponse] =
    Future.successful(HttpResponse(httpStatusCode, Nil, message))
}
