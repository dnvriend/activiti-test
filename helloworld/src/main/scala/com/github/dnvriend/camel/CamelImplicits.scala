/*
 * Copyright 2015 Dennis Vriend
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.dnvriend.camel

import org.apache.camel.Exchange

import scala.util.Try
import scala.collection.JavaConversions._

object CamelImplicits {
  implicit class CamelExchangeImplicits(val exchange: Exchange) extends AnyVal {
    /**
     * Sets the header on the in exchange
     */
    def setHeader(name: String, value: AnyRef): Unit =
      exchange.getIn.setHeader(name, value)

    /**
     * Sets the headers of the in exchange
     */
    def setHeaders(headers: Map[String, AnyRef]): Unit =
      exchange.getIn.setHeaders(headers)

    /**
     * Gets the header from the in exchange
     */
    def headerOpt[T](header: String): Option[T] =
      Try(exchange.getIn.getHeader(header).asInstanceOf[T]).toOption

    /**
     * Gets all the headers from the in exchange
     * @return
     */
    def headers: Map[String, AnyRef] =
      exchange.getIn.getHeaders.toMap

    /**
     * Get the body from the in exchange
     */
    def bodyOpt[T]: Option[T] =
      Try(exchange.getIn.getBody.asInstanceOf[T]).toOption

    /**
     *
     */
    def props = exchange.getProperties.toMap

    /**
     *
     */
    def dump: String = {
      s"""
         |Exchange(
         |body=${bodyOpt[String].getOrElse("")}
         |headers=$headers
         |properties=$props
         |)
       """.stripMargin
    }
  }
}
