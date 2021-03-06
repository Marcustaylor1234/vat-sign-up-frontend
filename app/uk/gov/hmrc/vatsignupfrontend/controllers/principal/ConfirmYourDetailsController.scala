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

package uk.gov.hmrc.vatsignupfrontend.controllers.principal


import javax.inject.{Inject, Singleton}

import uk.gov.hmrc.vatsignupfrontend.Constants.skipIvJourneyValue
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.auth.core.ConfidenceLevel
import uk.gov.hmrc.auth.core.retrieve.Retrievals
import uk.gov.hmrc.http.{BadGatewayException, InternalServerException}
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.SessionKeys.{identityVerificationContinueUrlKey, userDetailsKey}
import uk.gov.hmrc.vatsignupfrontend.config.ControllerComponents
import uk.gov.hmrc.vatsignupfrontend.config.auth.AdministratorRolePredicate
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.UseIRSA
import uk.gov.hmrc.vatsignupfrontend.controllers.AuthenticatedController
import uk.gov.hmrc.vatsignupfrontend.httpparsers.{NoMatchFoundFailure, NoVATNumberFailure, StoreNinoFailureResponse}
import uk.gov.hmrc.vatsignupfrontend.models.{UserDetailsModel, UserEntered}
import uk.gov.hmrc.vatsignupfrontend.services.{IdentityVerificationService, StoreNinoService}
import uk.gov.hmrc.vatsignupfrontend.utils.SessionUtils._
import uk.gov.hmrc.vatsignupfrontend.views.html.principal.check_your_details

import scala.concurrent.Future

@Singleton
class ConfirmYourDetailsController @Inject()(val controllerComponents: ControllerComponents,
                                             storeNinoService: StoreNinoService,
                                             identityVerificationService: IdentityVerificationService)
  extends AuthenticatedController(AdministratorRolePredicate) {

  val show: Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      val optUserDetails = request.session.getModel[UserDetailsModel](userDetailsKey)

      optUserDetails match {
        case Some(userDetails) =>
          Future.successful(Ok(check_your_details(userDetails, routes.ConfirmYourDetailsController.submit())))
        case None => Future.successful(Redirect(routes.CaptureYourDetailsController.show()))
      }

    }
  }

  val submit: Action[AnyContent] = Action.async { implicit request =>
    authorised()(Retrievals.confidenceLevel) {
      confidenceLevel =>
        val optVatNumber = request.session.get(SessionKeys.vatNumberKey).filter(_.nonEmpty)
        val optUserDetails = request.session.getModel[UserDetailsModel](userDetailsKey)

        lazy val ninoSource = if (isEnabled(UseIRSA)) Some(UserEntered) else None

        (optVatNumber, optUserDetails) match {
          case (Some(vatNumber), Some(userDetails)) =>
            storeNinoService.storeNino(vatNumber, userDetails, ninoSource) flatMap {
              case Right(_) =>
                if (confidenceLevel < ConfidenceLevel.L200) {
                  identityVerificationService.start(userDetails) map {
                    case Right(response) =>
                      val redirectUrl = appConfig.identityVerificationFrontendRedirectionUrl(response.link)
                      Redirect(redirectUrl)
                        .addingToSession(identityVerificationContinueUrlKey -> response.journeyLink)
                    case Left(error) =>
                      throw new BadGatewayException(s"Failure calling identity verification: status=${error.status}")
                  }
                } else {
                  Future.successful(Redirect(routes.IdentityVerificationCallbackController.continue()).
                    addingToSession(identityVerificationContinueUrlKey -> skipIvJourneyValue))
                }
              case Left(NoMatchFoundFailure) =>
                Future.successful(Redirect(routes.FailedMatchingController.show()))
              case Left(NoVATNumberFailure) =>
                Future.failed(new InternalServerException(s"Failure calling store nino: vat number is not found"))
              case Left(StoreNinoFailureResponse(status)) =>
                Future.failed(new InternalServerException(s"Failure calling store nino: status=$status"))
            }
          case (None, _) =>
            Future.successful(Redirect(routes.YourVatNumberController.show()))
          case (_, None) =>
            Future.successful(Redirect(routes.CaptureYourDetailsController.show()))
        }
    } map (_ removingFromSession userDetailsKey)
  }

}
