package com.unknownnpc.media.extractor.model

import org.openqa.selenium.{JavascriptExecutor, WebElement}

object SeleniumUtil:

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

