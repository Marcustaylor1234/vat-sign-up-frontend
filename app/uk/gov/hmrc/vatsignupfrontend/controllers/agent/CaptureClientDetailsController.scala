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

package uk.gov.hmrc.vatsignupfrontend.controllers.agent

import javax.inject.{Inject, Singleton}

import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.ControllerComponents
import uk.gov.hmrc.vatsignupfrontend.config.auth.AgentEnrolmentPredicate
import uk.gov.hmrc.vatsignupfrontend.controllers.AuthenticatedController
import uk.gov.hmrc.vatsignupfrontend.forms.UserDetailsForm._
import uk.gov.hmrc.vatsignupfrontend.views.html.agent.client_details
import uk.gov.hmrc.vatsignupfrontend.utils.SessionUtils._

import scala.concurrent.Future

@Singleton
class CaptureClientDetailsController @Inject()(val controllerComponents: ControllerComponents)
  extends AuthenticatedController(AgentEnrolmentPredicate) {

  val show: Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      Future.successful(
        Ok(client_details(userDetailsForm.form, routes.CaptureClientDetailsController.submit()))
      )
    }
  }

  val submit: Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      userDetailsForm.bindFromRequest.fold(
        formWithErrors =>
          Future.successful(
            BadRequest(client_details(formWithErrors, routes.CaptureClientDetailsController.submit()))
          ),
        userDetails =>
          Future.successful(
            Redirect(routes.ConfirmClientDetailsController.show()).addingToSession(SessionKeys.userDetailsKey, userDetails)
          )
      )
    }
  }

}
