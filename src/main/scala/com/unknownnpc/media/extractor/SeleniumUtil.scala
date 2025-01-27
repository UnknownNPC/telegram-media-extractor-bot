package com.unknownnpc.media.extractor

import com.typesafe.scalalogging.StrictLogging
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.devtools.v131.network.Network
import org.openqa.selenium.devtools.v131.network.model.RequestWillBeSent
import org.openqa.selenium.{By, JavascriptExecutor, WebElement}

import java.util.Optional
import java.util.concurrent.CopyOnWriteArrayList
import scala.annotation.tailrec
import scala.jdk.CollectionConverters.*
import scala.util.Try

object SeleniumUtil extends StrictLogging:

  def isElementVerticallyPartiallyInViewportAndHorizontallyFullyInViewport(jsExecutor: JavascriptExecutor, element: WebElement): Boolean =
    jsExecutor.executeScript(
      """
          var rect = arguments[0].getBoundingClientRect();
          var windowHeight = window.innerHeight || document.documentElement.clientHeight;
          var windowWidth = window.innerWidth || document.documentElement.clientWidth;

          return (
            rect.bottom > 0 &&
            rect.top < windowHeight &&
            rect.left >= 0 &&
            rect.right <= windowWidth
          );
      """.stripMargin, element
    ).asInstanceOf[Boolean]


  def findTagInScreenCenter(tagName: String, driver: ChromeDriver): Option[WebElement] =

    val jsExecutor = driver.asInstanceOf[JavascriptExecutor]

    def getCentralElement: Option[WebElement] =
      Option(
        jsExecutor.executeScript(
          """return document.elementFromPoint(window.innerWidth / 2, window.innerHeight / 2);"""
        ).asInstanceOf[WebElement]
      )

    def findClosestElementByTag(tagName: String, startElement: WebElement): Option[WebElement] =
      @tailrec
      def search(elements: List[WebElement], visited: Set[WebElement]): Option[WebElement] =
        elements match
          case Nil => None
          case head :: tail =>
            if head.getTagName.toLowerCase == tagName then Some(head)
            else
              val children = head.findElements(By.xpath("./*")).asScala.toList
              val parent = Try(head.findElement(By.xpath(".."))).toOption
              val nextElements = (children ++ parent).filterNot(visited.contains)
              search(tail ++ nextElements, visited + head)

      search(List(startElement), Set.empty)

    def isElementInCenter(element: WebElement): Boolean =
      val centerX = jsExecutor.executeScript("return Number(window.innerWidth / 2);").asInstanceOf[Number].doubleValue()
      val centerY = jsExecutor.executeScript("return Number(window.innerHeight / 2);").asInstanceOf[Number].doubleValue()

      val rect = element.getRect
      val elementLeft = rect.getX
      val elementTop = rect.getY
      val elementRight = elementLeft + rect.getWidth
      val elementBottom = elementTop + rect.getHeight

      val topLeft = (elementLeft, elementTop)
      val topRight = (elementRight, elementTop)
      val bottomLeft = (elementLeft, elementBottom)
      val bottomRight = (elementRight, elementBottom)

      logger.info(s"Element bounds (pixels): top-left=$topLeft, top-right=$topRight, bottom-left=$bottomLeft, bottom-right=$bottomRight")
      logger.info(s"Center point: x=$centerX, y=$centerY")

      centerX >= elementLeft && centerX <= elementRight && centerY >= elementTop && centerY <= elementBottom

    getCentralElement
      .flatMap(element => findClosestElementByTag(tagName, element))
      .filter(isElementInCenter)

  def runUrlsListenerScanner(urlIncludePredicate: String => Boolean): ChromeDriver => CopyOnWriteArrayList[String] = driver =>
    val segmentedUrls = new CopyOnWriteArrayList[String]()

    val devTools = driver.getDevTools
    devTools.createSession()
    devTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()))

    devTools.addListener(Network.requestWillBeSent(), new java.util.function.Consumer[RequestWillBeSent] {
      override def accept(request: RequestWillBeSent): Unit = {
        val urlStr = request.getRequest.getUrl
        if urlIncludePredicate(urlStr) then
          segmentedUrls.add(urlStr)
      }
    })
    segmentedUrls
