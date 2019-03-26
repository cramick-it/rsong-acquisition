package coop.rchain.repo

import com.typesafe.scalalogging.Logger
import coop.rchain.domain._
import coop.rchain.utils.FileUtil
import coop.rchain.utils.FileUtil._
import scala.util._
import io.circe.generic.auto._
import io.circe.syntax._

object RSongRepo {
  def apply(proxy: RholangProxy): RSongRepo = new RSongRepo(proxy)
}

class RSongRepo(val proxy: RholangProxy) {
  val log = Logger[RSongRepo]

  def asRholang(asset: RSongJsonAsset) = {
    log.info(s"name to retrieve song: ${asset.id}")
    s"""@["Immersion", "store"]!(${asset.assetData}, ${asset.jsonData}, "${asset.id}")"""
  }


  val deployFromFile: String => Either[Err, String] = path =>
    for {
      c <- FileUtil.fileFromClasspath(path)
      d <- proxy.deploy(c)
    } yield d

  def proposeBlock: Either[Err, String] = proxy.proposeBlock

  def loadAsset(assetId: String,
                assetPath: String,
                metadata: SongMetadata): Either[Err, String] = {
    val asset = RSongJsonAsset(id = assetId,
      assetData =
        asHexConcatRsong(s"$assetPath").toOption.get,
      jsonData = metadata.asJson.toString)
    (asRholang _ andThen proxy.deploy _) (asset)
  }
}

