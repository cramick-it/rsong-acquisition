package coop.rchain

import cats.effect._
import cats.syntax.all._
import coop.rchain.utils.Globals._
import com.typesafe.scalalogging.Logger
import service.moc.MocSongMetadata.mocSongs
import repo.{RSongRepo, RholangProxy}

object Bootstrap extends IOApp {

  val rsongPath = appCfg.getString("assets.path")
  val contractPath = appCfg.getString("contract.file.name")
  val repo = RSongRepo( RholangProxy(appCfg.getString("grpc.host"), 40401) )
  val log = Logger("BootStrap")

  def run(args: List[String]): IO[ExitCode] =
    args.headOption match {
      case Some(a) if a.equals("Install") =>
        IO(installContract(contractPath)) .as(ExitCode.Success)
          .handleError(e => {
            log.error(s"RsongAcquisition Install Contract has failed with error: ${e.getMessage}")
            ExitCode.Error
          })

      case Some(a) if a.equals("Deploy") =>
        IO(installAssets(rsongPath)).as(ExitCode.Success)
          .handleError(e => {
            log.error(s"RsongAcquisition Install Assets has failed with error: ${e.getMessage}")
            ExitCode.Error
          })

      case None =>
        val r = for {
          _ <- installContract(contractPath)
          a <- installAssets(rsongPath)
        } yield (a)
        IO(r).as(ExitCode.Success)
          .handleError(e => {
          log.error(s"RsongAcquisition has failed with error: ${e.getMessage}")
          ExitCode.Error
        })
    }

  def installContract(contractFile: String) = {
      for {
        _ <- repo.deployFromFile(contractFile)
        propose <- repo.proposeBlock
      } yield (propose)
  }


  def installAssets(path: String) = {

      for {
//        _ <- loadeAsset("Broke_Immersive.izr",
//                        s"$path/Songs/Broke_Immersive.izr",
//                        mocSongs("Broke"))
//        _ <- loadeAsset("Broke_Stereo.izr",
//                        s"$path/Songs/Broke_Stereo.izr",
//                        mocSongs("Broke"))
        _ <- repo.loadAsset("Broke.jpg",
                        s"$path/Labels/Broke.jpg",
                        mocSongs("Broke"))

//        _ <- proxy.proposeBlock
//        _ <- loadeAsset("Euphoria_Immersive.izr",
//                        s"$path/Songs/Euphoria_Immersive.izr",
//                        mocSongs("Euphoria"))
//        _ <- loadeAsset("Euphoria_Stereo.izr",
//                        s"$path/Songs/Euphoria_Stereo.izr",
//                        mocSongs("Euphoria"))
//        _ <- loadeAsset("Euphoria.jpg",
//                        s"$path/Labels/Euphoria.jpg",
//                        mocSongs("Euphoria"))
//        _ <- proxy.proposeBlock
//
//        _ <- loadeAsset("Tiny_Human_Immersive.izr",
//                        s"$path/Songs/Tiny_Human_Immersive.izr",
//                        mocSongs("Tiny_Human"))
//        _ <- loadeAsset("Tiny_Human_Stereo.izr",
//                        s"$path/Songs/Tiny_Human_Stereo.izr",
//                        mocSongs("Tiny_Human"))
//        _ <- loadeAsset("Tiny Human.jpg",
//                        s"$path/Labels/Tiny Human.jpg",
//                        mocSongs("Tiny_Human"))
        propose <-repo.proposeBlock

      } yield (propose)
  }

}
