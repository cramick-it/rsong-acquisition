package coop.rchain.utils

import java.io.{BufferedInputStream, FileInputStream}
import coop.rchain.utils.HexUtil._
import coop.rchain.domain.{Err, OpCode}
import scala.util._

object FileUtil {

  val fileFromClasspath: String => Either[Err, String] = fileName => {
    val stream = getClass.getResourceAsStream(fileName)
    Try(
      scala.io.Source.fromInputStream(stream).getLines.reduce(_ + _ + "\n")
    ) match {
      case Success(s) =>
        stream.close
        Right(s)
      case Failure(e) =>
        stream.close
        Left(Err(OpCode.contractFile, e.getMessage))
    }
  }
  def readFileAsByteArray(fileName: String): Array[Byte] = {
    val bis = new BufferedInputStream(new FileInputStream(fileName))
    Stream.continually(bis.read).takeWhile(-1 !=).map(_.toByte).toArray
  }

  def logDepth(s: String): String = {
    val threshold = 1024
    if (s.length <= threshold)
      s""""$s""""
    else {
      val mid = s.length / 2
      val l = logDepth(s.substring(0, mid))
      val r = logDepth(s.substring(mid))
      s"""(\n$l\n++\n$r\n)"""
    }
  }

  def asHexConcatRsong(filePath: String): Either[Err, String] = {
    Try {
      (readFileAsByteArray _
        andThen bytes2hex _
        andThen logDepth) (filePath)
    } match {
      case Success(s) =>
        Right(s)
      case Failure(e) =>
        Left(
          Err(OpCode.rsongHexConversion,
            s"${e.getMessage}"))
    }
  }
}
