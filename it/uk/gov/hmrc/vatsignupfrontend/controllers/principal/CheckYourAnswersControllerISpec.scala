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

import java.time.LocalDate

import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.{FeatureSwitching, KnownFactsJourney}
import uk.gov.hmrc.vatsignupfrontend.helpers.IntegrationTestConstants._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.StoreVatNumberStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers}
import uk.gov.hmrc.vatsignupfrontend.models.BusinessEntity.BusinessEntitySessionFormatter
import uk.gov.hmrc.vatsignupfrontend.models.{DateModel, SoleTrader}

class CheckYourAnswersControllerISpec extends ComponentSpecBase with CustomMatchers with FeatureSwitching {

  override def beforeEach(): Unit = enable(KnownFactsJourney)

  override def afterEach(): Unit = disable(KnownFactsJourney)

  val testDate: DateModel = DateModel.dateConvert(LocalDate.now())

  "GET /check-your-answers" should {
    "return an OK" in {
      stubAuth(OK, successfulAuthResponse())

      val res = get("/check-your-answers",
        Map(
          SessionKeys.vatNumberKey -> testVatNumber,
          SessionKeys.vatRegistrationDateKey -> Json.toJson(testDate).toString(),
          SessionKeys.businessPostCodeKey -> Json.toJson(testBusinessPostCode).toString(),
          SessionKeys.businessEntityKey -> BusinessEntitySessionFormatter.toString(SoleTrader)
        )
      )

      res should have(
        httpStatus(OK)
      )
    }
  }

  "POST /check-your-answersr" when {
    "store vat is successful" should {
      "redirect to capture your details" in {
        stubAuth(OK, successfulAuthResponse())
        stubStoreVatNumberSuccess(testBusinessPostCode, testDate)

        val res = post("/check-your-answers",
          Map(
            SessionKeys.vatNumberKey -> testVatNumber,
            SessionKeys.vatRegistrationDateKey -> Json.toJson(testDate).toString(),
            SessionKeys.businessPostCodeKey -> Json.toJson(testBusinessPostCode).toString(),
            SessionKeys.businessEntityKey -> BusinessEntitySessionFormatter.toString(SoleTrader)
          )
        )()

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CaptureYourDetailsController.show().url)
        )
      }
    }
    "store vat returned known facts mismatch" should {
      "redirect to could not confirm business" in {
        stubAuth(OK, successfulAuthResponse())
        stubStoreVatNumberKnownFactsMismatch(testBusinessPostCode, testDate)

        val res = post("/check-your-answers",
          Map(
            SessionKeys.vatNumberKey -> testVatNumber,
            SessionKeys.vatRegistrationDateKey -> Json.toJson(testDate).toString(),
            SessionKeys.businessPostCodeKey -> Json.toJson(testBusinessPostCode).toString(),
            SessionKeys.businessEntityKey -> BusinessEntitySessionFormatter.toString(SoleTrader)
          )
        )()

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CouldNotConfirmBusinessController.show().url)
        )
      }
    }
    "store vat returned invalid vat number" should {
      "redirect to invalid vat number" in {
        stubAuth(OK, successfulAuthResponse())
        stubStoreVatNumberInvalid(testBusinessPostCode, testDate)

        val res = post("/check-your-answers",
          Map(
            SessionKeys.vatNumberKey -> testVatNumber,
            SessionKeys.vatRegistrationDateKey -> Json.toJson(testDate).toString(),
            SessionKeys.businessPostCodeKey -> Json.toJson(testBusinessPostCode).toString(),
            SessionKeys.businessEntityKey -> BusinessEntitySessionFormatter.toString(SoleTrader)
          )
        )()

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.InvalidVatNumberController.show().url)
        )
      }
    }
    "store vat returned ineligible vat number" should {
      "redirect to cannot use service" in {
        stubAuth(OK, successfulAuthResponse())
        stubStoreVatNumberIneligible(testBusinessPostCode, testDate)

        val res = post("/check-your-answers",
          Map(
            SessionKeys.vatNumberKey -> testVatNumber,
            SessionKeys.vatRegistrationDateKey -> Json.toJson(testDate).toString(),
            SessionKeys.businessPostCodeKey -> Json.toJson(testBusinessPostCode).toString(),
            SessionKeys.businessEntityKey -> BusinessEntitySessionFormatter.toString(SoleTrader)
          )
        )()

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CannotUseServiceController.show().url)
        )
      }
    }
    "store vat returned already signed up" should {
      "redirect to already signed up" in {
        stubAuth(OK, successfulAuthResponse())
        stubStoreVatNumberAlreadySignedUp(testBusinessPostCode, testDate)

        val res = post("/check-your-answers",
          Map(
            SessionKeys.vatNumberKey -> testVatNumber,
            SessionKeys.vatRegistrationDateKey -> Json.toJson(testDate).toString(),
            SessionKeys.businessPostCodeKey -> Json.toJson(testBusinessPostCode).toString(),
            SessionKeys.businessEntityKey -> BusinessEntitySessionFormatter.toString(SoleTrader)
          )
        )()

        res should have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.AlreadySignedUpController.show().url)
        )
      }
    }
  }

  "Making a request to /check-your-answers when not enabled" should {
    "return NotFound" in {
      disable(KnownFactsJourney)

      stubAuth(OK, successfulAuthResponse())

      val res = get("/check-your-answers")

      res should have(
        httpStatus(NOT_FOUND)
      )

    }
  }

}
