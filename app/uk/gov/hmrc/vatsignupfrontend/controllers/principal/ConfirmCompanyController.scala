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
import uk.gov.hmrc.vatsignupfrontend.SessionKeys._
import uk.gov.hmrc.vatsignupfrontend.config.ControllerComponents
import uk.gov.hmrc.vatsignupfrontend.config.auth.AdministratorRolePredicate
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.CompanyNameJourney
import uk.gov.hmrc.vatsignupfrontend.controllers.AuthenticatedController
import uk.gov.hmrc.vatsignupfrontend.views.html.principal.confirm_company

import scala.concurrent.Future

@Singleton
class ConfirmCompanyController @Inject()(val controllerComponents: ControllerComponents)
  extends AuthenticatedController(AdministratorRolePredicate, featureSwitches = Set(CompanyNameJourney)) {

  val show: Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      val optCompanyName = request.session.get(companyNameKey).filter(_.nonEmpty)
      Future.successful(
        optCompanyName match {
          case Some(companyName) =>
            val changeLink = routes.CaptureCompanyNumberController.show().url
            Ok(confirm_company(
              companyName = companyName,
              postAction = routes.ConfirmCompanyController.submit(),
              changeLink = changeLink
            ))
          case _ =>
            Redirect(routes.CaptureCompanyNumberController.show())
        }
      )
    }
  }

  val submit: Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      Future.successful(Redirect(routes.AgreeCaptureEmailController.show()))
    }
  }
}