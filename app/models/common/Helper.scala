package models.common

import java.io.{IOException, PrintWriter, StringWriter}
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.{FileVisitResult, Files, Path, SimpleFileVisitor}

import models.common.Config._
import play.api.Logger

object Helper {

  def getStackTrack(exception: Throwable): String = {
    val sw = new StringWriter()
    val pw = new PrintWriter(sw)
    exception.printStackTrace(pw)
    sw.toString
  }

  def recursivelyDelete(dir: Path): Unit = {
    if (!Files.exists(dir)) return
    Files.walkFileTree(dir, new SimpleFileVisitor[Path] {
      override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
        Files.delete(file)
        FileVisitResult.CONTINUE
      }

      override def postVisitDirectory(dir: Path, exc: IOException): FileVisitResult = {
        Files.delete(dir)
        FileVisitResult.CONTINUE
      }
    })
  }


  def selectionSort(textMap: scala.collection.mutable.Map[String, Long], topN: Int): Array[TopEntryTy] = {
    Logger.info(s"sorting the result, size=${textMap.size}")
    val array = Array.fill[TopEntryTy](topN)(0L -> null)
    var i = 0
    while (i < array.length) {
      var current: TopEntryTy = 0L -> null
      for (entry <- textMap) {
        if (current._1 < entry._2) {
          current = entry._2 -> entry._1
        }
      }
      array(i) = current
      textMap -= current._2
      i += 1
    }
    for (a <- array; if a._1 != 0) yield a
  }

  class LError(val msg: String) extends Exception(msg)

}
