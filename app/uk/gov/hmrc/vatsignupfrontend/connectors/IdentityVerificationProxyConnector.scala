/*
 * Copyright 2018 HM Revenue & Customs
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

package uk.gov.hmrc.vatsignupfrontend.connectors

import javax.inject.{Inject, Singleton}

import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
import uk.gov.hmrc.vatsignupfrontend.config.AppConfig
import uk.gov.hmrc.vatsignupfrontend.controllers.principal.routes
import uk.gov.hmrc.vatsignupfrontend.httpparsers.IdentityVerificationProxyHttpParser._
import uk.gov.hmrc.vatsignupfrontend.models.UserDetailsModel

import scala.concurrent.Future

@Singleton
class IdentityVerificationProxyConnector @Inject()(val http: HttpClient,
                                                   val applicationConfig: AppConfig) {

  private[connectors] def startIdentityVerificationRequest(userDetails: UserDetailsModel): JsObject = {
    val redirectionUrl = applicationConfig.baseUrl + routes.IdentityVerificationCallbackController.continue().url
    Json.obj(
      "origin" -> "mtd-vat",
      "confidenceLevel" -> 200,
      "completionURL" -> redirectionUrl,
      "failureURL" -> redirectionUrl,
      "userData" -> Json.obj(
        "firstName" -> userDetails.firstName,
        "lastName" -> userDetails.lastName,
        "dateOfBirth" -> userDetails.dateOfBirth.toLocalDate.toString,
        "nino" -> userDetails.nino
      )
    )
  }

  def start(userDetails: UserDetailsModel)(implicit hc: HeaderCarrier): Future[IdentityVerificationProxyResponse] =
    http.POST[JsObject, IdentityVerificationProxyResponse](
      applicationConfig.identityVerificationStartUrl,
      startIdentityVerificationRequest(userDetails)
    )

}


