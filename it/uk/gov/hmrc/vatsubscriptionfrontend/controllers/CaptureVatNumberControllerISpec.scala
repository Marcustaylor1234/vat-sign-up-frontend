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

import play.api.http.Status._
import uk.gov.hmrc.vatsubscriptionfrontend.forms.VatNumberForm
import uk.gov.hmrc.vatsubscriptionfrontend.helpers.{ComponentSpecBase, CustomMatchers}
import uk.gov.hmrc.vatsubscriptionfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsubscriptionfrontend.helpers.IntegrationTestConstants._

class CaptureVatNumberControllerISpec extends ComponentSpecBase with CustomMatchers {
  "GET /vat-number" should {
    "return an OK" in {
      stubAuth(OK, successfulAuthResponse)

      val res = get("/vat-number")

      res should have(
        httpStatus(OK)
      )
    }
  }

  "POST /vat-number" should {
    "return a redirect" in {
      stubAuth(OK, successfulAuthResponse)

      val res = post("/vat-number")(VatNumberForm.vatNumber -> testVatNumber)

      res should have(
        httpStatus(SEE_OTHER),
        redirectUri(routes.ConfirmVatNumberController.show().url)
      )
    }
  }
}