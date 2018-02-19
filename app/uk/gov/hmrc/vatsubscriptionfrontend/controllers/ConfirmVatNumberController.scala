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

package uk.gov.hmrc.vatsubscriptionfrontend.controllers

import javax.inject.{Inject, Singleton}

import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.vatsubscriptionfrontend.SessionKeys
import uk.gov.hmrc.vatsubscriptionfrontend.config.ControllerComponents
import uk.gov.hmrc.vatsubscriptionfrontend.views.html.confirm_vat_number

import scala.concurrent.Future

@Singleton
class ConfirmVatNumberController @Inject()(val controllerComponents: ControllerComponents)
  extends AuthenticatedController {

  val show: Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      request.session.get(SessionKeys.vrn) match {
        case Some(vrn) =>
          Future.successful(
            Ok(confirm_vat_number(vrn, routes.ConfirmVatNumberController.submit()))
          )
        case _ =>
          Future.successful(
            Redirect(routes.CaptureVatNumberController.show())
          )
      }
    }
  }

  val submit: Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      Future.successful(NotImplemented)
    }
  }
}