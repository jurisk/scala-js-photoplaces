package utils

import akka.stream.scaladsl.{Flow, Sink}
import akka.stream.stage.{GraphStageLogic, GraphStageWithMaterializedValue, InHandler, OutHandler}
import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import akka.util.ByteString
import cats.data.Xor
import io.circe._
import play.api.Logger
import play.api.http.Status._
import play.api.http._
import play.api.libs.iteratee.Execution.Implicits.trampoline
import play.api.libs.streams.Accumulator
import play.api.mvc._

import scala.concurrent.{Future, Promise}
import scala.util.control.NonFatal

trait CirceSupport {

  implicit def contentTypeOf_Json(implicit codec: Codec): ContentTypeOf[Json] = {
    ContentTypeOf(Some(ContentTypes.JSON))
  }

  implicit def writableOf_Json(implicit codec: Codec): Writeable[Json] = {
    Writeable(a => codec.encode(a.noSpaces))
  }

  object circe {

    import BodyParsers._

    @inline def DefaultMaxTextLength: Int = parse.DefaultMaxTextLength

    val logger = Logger(BodyParsers.getClass)

    def json[T: Decoder]: BodyParser[T] = json.mapM { json =>
      implicitly[Decoder[T]].decodeJson(json) match {
        case Xor.Left(e) => Future.failed(e)
        case Xor.Right(t) => Future.successful(t)
      }
    }

    def json: BodyParser[Json] = json(DefaultMaxTextLength)

    def json(maxLength: Int): BodyParser[Json] = parse.when(
      _.contentType.exists(m => m.equalsIgnoreCase("text/json") || m.equalsIgnoreCase("application/json")),
      tolerantJson(maxLength),
      createBadResult("Expecting text/json or application/json body", UNSUPPORTED_MEDIA_TYPE)
    )

    def tolerantJson[T: Decoder]: BodyParser[T] = tolerantJson.mapM { json =>
      implicitly[Decoder[T]].decodeJson(json) match {
        case Xor.Left(e) => Future.failed(e)
        case Xor.Right(t) => Future.successful(t)
      }
    }

    def tolerantJson: BodyParser[Json] = tolerantJson(DefaultMaxTextLength)

    def tolerantJson(maxLength: Int): BodyParser[Json] = {
      tolerantBodyParser[Json]("json", maxLength.toLong, "Invalid Json") { (request, bytes) =>
        parser.parse(new String(bytes.toArray, "UTF-8")).toEither
      }
    }

    private def createBadResult(msg: String, statusCode: Int = BAD_REQUEST): RequestHeader => Future[Result] = { request =>
      LazyHttpErrorHandler.onClientError(request, statusCode, msg)
    }

    private def tolerantBodyParser[A](name: String, maxLength: Long, errorMessage: String)(parser: (RequestHeader, ByteString) => Either[Error, A]): BodyParser[A] =
      BodyParser(name + ", maxLength=" + maxLength) { request =>
        import play.api.libs.iteratee.Execution.Implicits.trampoline

        enforceMaxLength(request, maxLength, Accumulator(
          Sink.fold[ByteString, ByteString](ByteString.empty)((state, bs) => state ++ bs)
        ) mapFuture { bytes =>
          parser(request, bytes) match {
            case Right(r) => Future.successful(Right(r))
            case Left(NonFatal(e)) =>
              logger.debug(errorMessage, e)
              createBadResult(errorMessage + ": " + e.getMessage)(request).map(Left(_))
            case Left(fatal) ⇒ throw fatal
          }
        })
      }

    def enforceMaxLength[A](request: RequestHeader, maxLength: Long, accumulator: Accumulator[ByteString, Either[Result, A]]): Accumulator[ByteString, Either[Result, A]] = {
      val takeUpToFlow = Flow.fromGraph(new TakeUpTo(maxLength))
      Accumulator(takeUpToFlow.toMat(accumulator.toSink) { (statusFuture, resultFuture) =>
        import play.api.libs.iteratee.Execution.Implicits.trampoline
        val defaultCtx = play.api.libs.concurrent.Execution.Implicits.defaultContext

        statusFuture.flatMap {
          case MaxSizeExceeded(_) =>
            val badResult = Future.successful(()).flatMap(_ => createBadResult("Request Entity Too Large", REQUEST_ENTITY_TOO_LARGE)(request))(defaultCtx)
            badResult.map(Left(_))
          case MaxSizeNotExceeded => resultFuture
        }
      })
    }

    class TakeUpTo(maxLength: Long) extends GraphStageWithMaterializedValue[FlowShape[ByteString, ByteString], Future[MaxSizeStatus]] {

      private val in = Inlet[ByteString]("TakeUpTo.in")
      private val out = Outlet[ByteString]("TakeUpTo.out")

      override def shape: FlowShape[ByteString, ByteString] = FlowShape.of(in, out)

      override def createLogicAndMaterializedValue(inheritedAttributes: Attributes): (GraphStageLogic, Future[MaxSizeStatus]) = {
        val status = Promise[MaxSizeStatus]()
        var pushedBytes: Long = 0

        val logic = new GraphStageLogic(shape) {
          setHandler(out, new OutHandler {
            override def onPull(): Unit = {
              pull(in)
            }
            override def onDownstreamFinish(): Unit = {
              status.success(MaxSizeNotExceeded)
              completeStage()
            }
          })
          setHandler(in, new InHandler {
            override def onPush(): Unit = {
              val chunk = grab(in)
              pushedBytes += chunk.size
              if (pushedBytes > maxLength) {
                status.success(MaxSizeExceeded(maxLength))
                // Make sure we fail the stream, this will ensure downstream body parsers don't try to parse it
                failStage(new MaxLengthLimitAttained)
              } else {
                push(out, chunk)
              }
            }
            override def onUpstreamFinish(): Unit = {
              status.success(MaxSizeNotExceeded)
              completeStage()
            }
            override def onUpstreamFailure(ex: Throwable): Unit = {
              status.failure(ex)
              failStage(ex)
            }
          })
        }

        (logic, status.future)
      }
    }

    class MaxLengthLimitAttained extends RuntimeException(null, null, false, false)
  }
}
