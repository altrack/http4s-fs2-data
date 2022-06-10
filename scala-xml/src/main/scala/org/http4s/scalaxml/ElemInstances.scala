/*
 * Copyright 2014 http4s.org
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

package org.http4s
package scalaxml

import cats.effect.Concurrent
import cats.syntax.all._
import fs2.data.xml.XmlException
import fs2.data.xml.scalaXml._
import org.http4s.Charset.`UTF-8`
import org.http4s.headers.`Content-Type`

import java.io.StringWriter
import javax.xml.parsers.SAXParserFactory
import scala.xml.Elem
import scala.xml.XML

trait ElemInstances {
  @deprecated("0.23.12", "This factory is no longer used")
  protected def saxFactory: SAXParserFactory

  implicit def xmlEncoder[F[_]](implicit charset: Charset = `UTF-8`): EntityEncoder[F, Elem] =
    EntityEncoder
      .stringEncoder[F]
      .contramap[Elem] { node =>
        val sw = new StringWriter
        XML.write(sw, node, charset.nioCharset.name, true, null)
        sw.toString
      }
      .withContentType(`Content-Type`(MediaType.application.xml).withCharset(charset))

  /** Handles a message body as XML.
    *
    * TODO Not an ideal implementation.  Would be much better with an asynchronous XML parser, such as Aalto.
    *
    * @return an XML element
    */
  implicit def xml[F[_]](implicit F: Concurrent[F]): EntityDecoder[F, Elem] = {
    import EntityDecoder._
    decodeBy(MediaType.text.xml, MediaType.text.html, MediaType.application.xml) { msg =>
      DecodeResult {
        msg.bodyText
          .through(fs2.data.xml.events())
          .through(fs2.data.xml.dom.documents)
          .head
          .compile
          .lastOrError
          .map {
            _.docElem match {
              case e: Elem => Right(e)
              case _ => Left(MalformedMessageBodyFailure("Invalid XML"))
            }
          }
          .recover { case ex: XmlException =>
            Left(MalformedMessageBodyFailure("Invalid XML", Some(ex)))
          }
          .widen
      }
    }
  }
}
