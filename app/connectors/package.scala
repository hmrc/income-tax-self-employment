/*
 * Copyright 2025 HM Revenue & Customs
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

import models.connector.IntegrationContext
import play.api.libs.json.Writes
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReads}

import scala.concurrent.{ExecutionContext, Future}

package object connectors {

  def get[Resp: HttpReads](http: HttpClient, context: IntegrationContext)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Resp] = {
    val reads = implicitly[HttpReads[Resp]]
    http.GET[Resp](context.url)(reads, context.enrichedHeaderCarrier, ec)
  }

  def getWithHeaders[Resp: HttpReads](http: HttpClient, context: IntegrationContext, additionalHeaders: Seq[(String, String)])(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): Future[Resp] = {

    val headersWithExtras = hc.withExtraHeaders(additionalHeaders: _*)

    val reads = implicitly[HttpReads[Resp]]
    http.GET[Resp](context.url)(reads, headersWithExtras, ec)
  }

  def post[Req: Writes, Resp: HttpReads](http: HttpClient, context: IntegrationContext, body: Req)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): Future[Resp] = {
    val reads  = implicitly[HttpReads[Resp]]
    val writes = implicitly[Writes[Req]]
    http.POST[Req, Resp](context.url, body)(writes, reads, context.enrichedHeaderCarrier, ec)
  }

  def put[Req: Writes, Resp: HttpReads](http: HttpClient, context: IntegrationContext, body: Req)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): Future[Resp] = {
    val reads  = implicitly[HttpReads[Resp]]
    val writes = implicitly[Writes[Req]]
    http.PUT[Req, Resp](context.url, body)(writes, reads, context.enrichedHeaderCarrier, ec)
  }

  def delete[Resp: HttpReads](http: HttpClient, context: IntegrationContext)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Resp] = {
    val reads = implicitly[HttpReads[Resp]]
    http.DELETE[Resp](context.url)(reads, context.enrichedHeaderCarrier, ec)
  }
}
