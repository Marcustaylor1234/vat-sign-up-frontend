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

package uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks

import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.vatsignupfrontend.models.UserDetailsModel

object StoreCompanyNumberStub extends WireMockMethods {

  private def toJson(userDetailsModel: UserDetailsModel) = Json.obj(
    "firstName" -> userDetailsModel.firstName,
    "lastName" -> userDetailsModel.lastName,
    "nino" -> userDetailsModel.nino,
    "dateOfBirth" -> userDetailsModel.dateOfBirth.toLocalDate
  )

  def stubStoreCompanyNumber(vatNumber: String, companyNumber: String)(responseStatus: Int): Unit = {
    when(method = PUT, uri = s"/vat-sign-up/subscription-request/vat-number/$vatNumber/company-number",
      body = Json.obj("companyNumber" -> companyNumber))
      .thenReturn(status = responseStatus)
  }

  def stubStoreCompanyNumberSuccess(vatNumber: String, companyNumber: String): Unit = stubStoreCompanyNumber(vatNumber, companyNumber)(NO_CONTENT)

  def stubStoreCompanyNumberFailure(vatNumber: String, companyNumber: String): Unit = stubStoreCompanyNumber(vatNumber, companyNumber)(INTERNAL_SERVER_ERROR)

}
