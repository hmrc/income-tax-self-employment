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
import play.api.libs.json.{Json, Writes}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, StringContextOps}

import scala.concurrent.{ExecutionContext, Future}

package object connectors {

  def get[Resp: HttpReads](http: HttpClientV2, context: IntegrationContext)
                          (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Resp] = {
    val reads = implicitly[HttpReads[Resp]]

    http.get(url"${context.url}")(context.enrichedHeaderCarrier).execute(reads, ec)
  }

  def post[Req: Writes, Resp: HttpReads](http: HttpClientV2, context: IntegrationContext, body: Req)
                                        (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Resp] = {
    val reads  = implicitly[HttpReads[Resp]]
    val writes = implicitly[Writes[Req]]

    http.post(url"${context.url}")(context.enrichedHeaderCarrier)
      .withBody(Json.toJson(body)(writes))
      .execute(reads, ec)
  }

  def put[Req: Writes, Resp: HttpReads](http: HttpClientV2, context: IntegrationContext, body: Req)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): Future[Resp] = {
    val reads  = implicitly[HttpReads[Resp]]
    val writes = implicitly[Writes[Req]]

    http.put(url"${context.url}")(context.enrichedHeaderCarrier)
      .withBody(Json.toJson(body)(writes))
      .execute(reads, ec)
  }

  def delete[Resp: HttpReads](http: HttpClientV2, context: IntegrationContext)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Resp] = {
    val reads = implicitly[HttpReads[Resp]]

    http.delete(url"${context.url}")(context.enrichedHeaderCarrier)
      .execute(reads, ec)
  }
}
