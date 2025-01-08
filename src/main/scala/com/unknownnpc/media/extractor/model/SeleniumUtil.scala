package com.unknownnpc.media.extractor.model

import org.openqa.selenium.{JavascriptExecutor, WebElement}

object SeleniumUtil:

  def isElementInViewport(jsExecutor: JavascriptExecutor, element: WebElement): Boolean =
    jsExecutor.executeScript(
      """
          var rect = arguments[0].getBoundingClientRect();
          return (
            rect.top >= 0 &&
            rect.left >= 0 &&
            rect.bottom <= (window.innerHeight || document.documentElement.clientHeight) &&
            rect.right <= (window.innerWidth || document.documentElement.clientWidth)
          );
        """.stripMargin, element
    ).asInstanceOf[Boolean]
