package coop.rchain.repo

import coop.rchain.casper.protocol._
import coop.rchain.domain.{Err, OpCode}
import com.google.protobuf.empty._
import io.grpc.{ManagedChannel, ManagedChannelBuilder}
import coop.rchain.casper.protocol.DeployServiceGrpc.DeployServiceBlockingStub
import coop.rchain.domain.OpCode.OpCode

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

  implicit class EitherOps(val resp: coop.rchain.either.Either)  {
    def asEither: OpCode => Either[Err, String] = opcode =>
      resp match {
        case coop.rchain.either.Either(content) if content.isError =>
          Left(Err(
            opcode,
            content.error.map(x => x.messages.toString).getOrElse("No error message given!") ))
        case coop.rchain.either.Either(content) if content.isEmpty =>
          Left(Err(opcode, "No value was returned!"))
        case coop.rchain.either.Either(content) if content.isSuccess =>
          Right(content.success.head.getResponse.value.toStringUtf8)
      }
  }
}

class RholangProxy(channel: ManagedChannel) {
  import RholangProxy._

  private lazy val grpc: DeployServiceBlockingStub =
    DeployServiceGrpc.blockingStub(channel)

  def shutdown = channel.shutdownNow()

  def deploy(contract: String): Either[Err, String] =
    grpc.doDeploy(
      DeployData()
        .withTerm(contract)
        .withTimestamp(System.currentTimeMillis())
        .withPhloLimit(Long.MaxValue)
        .withPhloPrice(1L)
    ).asEither(OpCode.grpcDeploy)

  def proposeBlock: Either[Err, String] =
    grpc.createBlock(Empty()).asEither(OpCode.grpcDeploy)
}
