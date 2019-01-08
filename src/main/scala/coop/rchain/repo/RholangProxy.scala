package coop.rchain.repo


import coop.rchain.domain.{Err, ErrorCode}
import com.google.protobuf.empty._
import com.google.protobuf.{ByteString, Int32Value, StringValue}
import io.grpc.{ManagedChannel, ManagedChannelBuilder}
import com.typesafe.scalalogging.Logger
import coop.rchain.casper.protocol.{DeployData, DeployServiceGrpc}
import coop.rchain.casper.protocol.DeployServiceGrpc.DeployServiceBlockingStub
import coop.rchain.utils.FileUtil

import scala.util._

object RholangProxy {

  private final val MAXGRPCSIZE = 1024 * 1024 * 500000

  def apply(host: String, port: Int): RholangProxy = {

    val channel =
      ManagedChannelBuilder
        .forAddress(host, port)
        .maxInboundMessageSize(MAXGRPCSIZE)
        .usePlaintext(true)
        .build

    new RholangProxy(channel)
  }

}

class RholangProxy(channel: ManagedChannel) {

  private lazy val grpc: DeployServiceBlockingStub =
    DeployServiceGrpc.blockingStub(channel)
  private lazy val log = Logger[RholangProxy]

  def shutdown = channel.shutdownNow()

  def deploy(source: String): Either[Err, String] = {
    val resp = grpc.doDeploy(DeployData(
        user = ByteString.EMPTY,
        timestamp = System.currentTimeMillis(),
        term = source,
        phloLimit = Integer.MAX_VALUE
      ))

    if (resp.success)
      Right(resp.message)
    else Left(Err(ErrorCode.grpcDeploy, resp.message, Some(source)))
  }

  val deployFromFile: String => Either[Err, String] = path =>
    for {
      c <- FileUtil.fileFromClasspath(path)
      d <- deploy(c)
    } yield d

  def proposeBlock: Either[Err, String] =
    Try(   grpc.createBlock(Empty()) ) match {
      case Success(response) if response.success =>
        Right(response.message)
      case Success(response) if ! response.success =>
        log.error(s"grpc error. error return: ${response}")
        Left(Err(ErrorCode.grpcPropose, response.message, None))
      case Failure(e) =>
        println(e)
        Left(Err(ErrorCode.grpcPropose, e.getMessage, None))
    }

}
