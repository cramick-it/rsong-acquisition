package coop.rchain.repo

import coop.rchain.domain.{Err, OpCode, RSongJsonAsset}
import org.specs2._
import coop.rchain.either.{Either => CoopE}

class RholangProxySpec extends Specification { def is =
  s2"""
      RholangProxy Specs
        coop.rchain.either.Either to Scala Either conversion   $e1

        propose test rsong succeeds $e3
    """
  import RholangProxy._

  def e1 = {
    val defaultInstance = CoopE.defaultInstance
    val computed: Either[Err, String] = CoopE.defaultInstance.asEither(OpCode.grpcDeploy)
    computed ===  Left(Err(OpCode.grpcDeploy,"No value was returned!"))
  }
  def e2 = {
    val contrct =
      """
        |new chan1, stdout(`rho:io:stdout`) in {
        |  stdout!("I'm on the screen")
        |  |
        |  chan1!("I'm in the tuplespace")
        |}
      """.stripMargin
    val rnode = RholangProxy("localhost",40401)
    val computed = rnode.deploy(contrct)
    println(s"computed deploy = ${computed}")
    computed.isLeft === false
  }

    def e3 = {
      val contrct =
        """
new metaDataMapStore, songMapStore, userMapStore, remunerate, testLog(`rho:io:stderr`) in {
  metaDataMapStore!({}) |
  songMapStore!({}) |
  userMapStore!({}) |
  contract @["Immersion", "store"](@songDataIn, @songMetadataIn, songIdOut) = {
    new songId, songDataId in {
      for (@metaDataMap <- metaDataMapStore; @songMap <- songMapStore) {
        metaDataMapStore!(
          metaDataMap.set(*songId.toByteArray(), [songMetadataIn, *songDataId.toByteArray()])) |
        songMapStore!(songMap.set(*songDataId.toByteArray(), songDataIn)) |
    new String(utf8Bytes, "UTF8")    songIdOut!(*songId.toByteArray())
      }
    }
  } |
  contract @["Immersion", "retrieveMetaData"](metaDataMapOut) = {
    for (@metaDataMap <- metaDataMapStore) {
      metaDataMapStore!(metaDataMap) |
      metaDataMapOut!(metaDataMap)
    } } }
        """
      val rnode = RholangProxy("localhost",40401)
      val deployed_resp2 =
      (RSongRepo.asRholang _ andThen rnode.deploy) (
        RSongJsonAsset(id="id-123", assetData="0x12fd98",jsonData="jsonData" )
      )
      val proposed_resp2 = rnode.proposeBlock

      println(s"####### computed deploy_resp2 = ${deployed_resp2}")
      println(s"######### computed proposed_resp2 = ${proposed_resp2}")

      deployed_resp2.isRight === true
      proposed_resp2.isRight  === true

    }
  def e_4 = {
    import coop.rchain.casper.util.comm._
    val l = ListenAt
  }
}