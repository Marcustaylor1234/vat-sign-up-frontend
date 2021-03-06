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

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.retrieve.EmptyRetrieval
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.config.mocks.MockControllerComponents

import scala.concurrent.Future

class SignInWithDifferentDetailsSpec extends UnitSpec with GuiceOneAppPerSuite with MockControllerComponents {

  object TestSignInWithDifferentDetailsController extends SignInWithDifferentDetailsController(mockControllerComponents)

  lazy val testGetRequest = FakeRequest("GET", "/sign-in-with-different-details")

  "Calling the show action of the Sign in with different details controller" should {
    "return OK" in {
      mockAuthAdminRole()
      val request = testGetRequest

      val result = TestSignInWithDifferentDetailsController.show(request)
      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }
  }

}
