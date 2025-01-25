package com.unknownnpc.media.extractor

import com.unknownnpc.media.extractor.model.{CustomCookie, Extension, ExtractorPayload, Result}
import org.openqa.selenium.{By, JavascriptExecutor, WebElement}

import java.net.URL
import scala.annotation.tailrec
import scala.jdk.CollectionConverters.*
import scala.util.Try

private[extractor] class SeleniumImageInCenterExtractor(val customCookies: Seq[CustomCookie])
  extends Extractor[Option[ExtractorPayload]] with SeleniumWebDriverLike:

  override def extract(url: URL): Result[Option[ExtractorPayload]] =
    openPage(url, customCookies) { (driver, _) =>
      val jsExecutor = driver.asInstanceOf[JavascriptExecutor]
      val centralElement = findCentralElement(jsExecutor)
      centralElement.flatMap(findNearestImageTailRec).map { img =>
        val src = img.getDomAttribute("src")
        logger.info(s"The nearest image to the center has src: $src")
        ExtractorPayload(Seq(new URL(src)), Extension.JPEG)
      }
    }

  private def findCentralElement(jsExecutor: JavascriptExecutor): Option[WebElement] =
    Option(
      jsExecutor.executeScript(
        """return document.elementFromPoint(window.innerWidth / 2, window.innerHeight / 2);"""
      ).asInstanceOf[WebElement]
    )

  private def findNearestImageTailRec(startElement: WebElement): Option[WebElement] =
    @tailrec
    def search(elements: List[WebElement], visited: Set[WebElement]): Option[WebElement] =
      elements match
        case Nil => None
        case head :: tail =>
          if head.getTagName.toLowerCase == "img" then Some(head)
          else
            val children = head.findElements(By.xpath("./*")).asScala.toList
            val parent = Try(head.findElement(By.xpath(".."))).toOption
            val nextElements = (children ++ parent).filterNot(visited.contains)
            search(tail ++ nextElements, visited + head)

    search(List(startElement), Set.empty)
