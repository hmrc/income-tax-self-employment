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

package services.answers

import models.common.JourneyName
import models.common.JourneyName.{IndustrySectors, TravelExpenses, VehicleDetails}
import models.database.IndustrySectorsDb
import models.database.expenses.travel.{TravelExpensesDb, VehicleDetailsDb}
import play.api.Logging
import play.api.libs.json._
import uk.gov.hmrc.http.InternalServerException

import javax.inject.{Inject, Singleton}
import scala.annotation.tailrec
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class JourneyAnswerValidationService @Inject() (implicit ec: ExecutionContext) extends Logging {

  def validate(section: JourneyName, json: JsValue): Future[Either[InvalidSection, ValidSection]] =
    section match {
      case IndustrySectors => Future(validate[IndustrySectorsDb](json))
      case TravelExpenses  => Future(validate[TravelExpensesDb](json))
      case VehicleDetails  => Future(validate[CollectionSection[VehicleDetailsDb]](json))
      case unknown =>
        logger.error(s"Attempted to validate an unsupported section: ${unknown.toString}")
        throw new InternalServerException(s"Attempted to validate an unsupported section: ${unknown.toString}")
    }

  def validateIndex(section: JourneyName, json: JsValue): Future[Either[InvalidSection, ValidSection]] =
    section match {
      case VehicleDetails => Future(validate[VehicleDetailsDb](json))
      case unknown =>
        logger.error(s"Attempted to validate an unsupported collection section: ${unknown.toString}")
        throw new InternalServerException(s"Attempted to validate an unsupported collection section: ${unknown.toString}")
    }

  private def validate[A](json: JsValue)(implicit format: Format[A]): Either[InvalidSection, ValidSection] =
    json.validate[A] match {
      case JsSuccess(value, _) =>
        val sanitizedJson = removeInvalidFields(Json.toJson(value), getInvalidFields(value))
        Right(ValidSection(sanitizedJson))
      case JsError(errors) =>
        Left(InvalidSection(errors.map(_._1.toString()).toSeq))
    }

  private def getInvalidFields[A](value: A)(implicit writes: Writes[A]): Seq[String] = {
    val expectedFields = value.getClass.getDeclaredFields.toSeq.map(_.getName)
    val json           = Json.toJson(value)

    json.productElementNames.filterNot(expectedFields.contains(_)).toSeq
  }

  @tailrec
  private def removeInvalidFields(json: JsValue, invalidFields: Seq[String]): JsValue =
    invalidFields.headOption match {
      case Some(field) =>
        val updatedJson = json.as[JsObject] - field
        removeInvalidFields(updatedJson, invalidFields.tail)
      case None => json
    }

}

case class CollectionSection[T](values: Seq[T])

object CollectionSection {
  implicit def format[T](implicit format: Format[T]): Format[CollectionSection[T]] = Json.format[CollectionSection[T]]
}

case class InvalidSection(errors: Seq[String]) {
  def asString: String = errors.mkString(", ")
}

case class ValidSection(validatedModel: JsValue)
