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

import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.ControllerComponents
import uk.gov.hmrc.vatsignupfrontend.config.auth.AdministratorRolePredicate
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.CompanyNameJourney
import uk.gov.hmrc.vatsignupfrontend.controllers.AuthenticatedController
import uk.gov.hmrc.vatsignupfrontend.forms.CompanyNumberForm._
import uk.gov.hmrc.vatsignupfrontend.httpparsers.GetCompanyNameHttpParser.{CompanyNumberNotFound, GetCompanyNameFailureResponse, GetCompanyNameSuccess}
import uk.gov.hmrc.vatsignupfrontend.services.GetCompanyNameService
import uk.gov.hmrc.vatsignupfrontend.views.html.principal.capture_company_number

import scala.concurrent.Future

@Singleton
class CaptureCompanyNumberController @Inject()(val controllerComponents: ControllerComponents,
                                               val getCompanyNameService: GetCompanyNameService
                                              )
  extends AuthenticatedController(AdministratorRolePredicate) {

  val show: Action[AnyContent] = Action.async {
    implicit request =>
      authorised() {
        Future.successful(
          Ok(capture_company_number(companyNumberForm.form, routes.CaptureCompanyNumberController.submit()))
        )
      }
  }

  val submit: Action[AnyContent] = Action.async {
    implicit request =>
      authorised() {
        companyNumberForm.bindFromRequest.fold(
          formWithErrors =>
            Future.successful(
              BadRequest(capture_company_number(formWithErrors, routes.CaptureCompanyNumberController.submit()))
            ),
          companyNumber =>
            if (isEnabled(CompanyNameJourney)) {
              getCompanyNameService.getCompanyName(companyNumber) map {
                case Right(GetCompanyNameSuccess(companyName)) =>
                  Redirect(routes.ConfirmCompanyController.show())
                    .addingToSession(
                      SessionKeys.companyNumberKey -> companyNumber,
                      SessionKeys.companyNameKey -> companyName
                    )
                case Left(CompanyNumberNotFound) =>
                  Redirect(routes.CompanyNameNotFoundController.show())
                    .removingFromSession(SessionKeys.companyNumberKey, SessionKeys.companyNameKey)
                case Left(GetCompanyNameFailureResponse(status)) =>
                  throw new InternalServerException(s"getCompanyName failed: status=$status")
              }
            } else {
              Future.successful(
                Redirect(routes.ConfirmCompanyNumberController.show()).addingToSession(SessionKeys.companyNumberKey -> companyNumber)
              )
            }
        )
      }
  }

}
