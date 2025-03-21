/*
 * Copyright 2023 HM Revenue & Customs
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

package connectors.data

import models.common.TaxYear.asTys
import models.connector.api_1871.BusinessIncomeSourcesSummaryResponse
import play.api.libs.json.Json
import testdata.CommonTestData

trait Api1871Test extends CommonTestData {
  val downstreamUrl = s"/income-tax/income-sources/${asTys(testTaxYear)}/$testNino/$testBusinessId/self-employment/biss"

  val successResponseRaw: String =
    s"""{
      |   "incomeSourceId": "${testBusinessId.value}",
      |   "totalIncome": 200,
      |   "totalExpenses": 200,
      |   "netProfit": 200,
      |   "netLoss": 200,
      |   "totalAdditions": 200,
      |   "totalDeductions": 200,
      |   "accountingAdjustments": 200,
      |   "taxableProfit": 200,
      |   "taxableLoss": 200
      |}
      |""".stripMargin

  // I really want some space where when we have some issue we can share with CSD, and then have people that come and answer on that solution. Not just waiting for all hands or Q&A sessions.
  // 18:39
  // "It's just when you're in your, it just feels, feels hard to connect to a practise when you when you're just doing your 9 to 5.
  //
  // 18:51
  // Does that make sense because you're so engrossed in live stuff, you don't have time to, you know, to flip over and look at what's going on on in the corp world because you're so client and looking what's going on live.
  //
  // 19:05
  // I think that's where the disconnect is. "

  // 22:25 -24:18 -  What brings you joy?
  // 'Yeah, it is, it is the, it's the people.
  //
  // And it has been and it's, it's actually, you know, going back to your original thing of, you know, the criticism to do with how can the practise know what we do when it should be if you, if you just, if you look at the feedback, like just gotten from ITL.
  // You know, like, and I've said this to, I said this to my reviewer a couple of days ago and about two months ago as well.
  // I said, I don't want the recognition in the form of like some finance or a pay increase or the next band. I want the recognition from my peers to say, OK, you know, this is what you delivered and this is what you've done, or thank you for doing that. And, and Matt and other seniors on our team, when we do do that, they do give us that appraise. And I think that's what makes it, do you understand, it, as hard as it gets, as bad as it gets, you know that the team will never let you down. And I've been here for four years and I've not, I think as a junior coming in probably two years ago, watching this team, the way in which they operate, it's still, you know, I say to Aaron and James like it'll probably take another 3-4 lifetimes before I ever become like them.
  // But yeah, that really set that precedence.
  // And there's some real, real role model behaviour of how you should be conducting yourself, how you should be working, the manner in which you should be working.
  // So this isn't just said like this isn't just like, oh, this what your team leader or the engineers expect? They actually, and every day they approach that job like that.
  // So you can help but not think, damn, I need to get on this bandwagon quickly and start working like them.  And then when you see what they're doing, that's a whole different kettle of fish as well. "
  //

  // 30:26 - 32:00
  // Speaking of work, how do you organise yourself ... in terms of your priorities?
  // 30:34
  // 'Oh, I think I've got better with that as, as I've, as I've grown older.  Does that make sense? And, and yeah, and having children, you know, that, that kind of helps prioritise work a bit more because, I think you start putting into, you know, what's important, what needs to be delivered, Where's the focus?  Where's the lens at? What does the client want? You know, these things are, these things all have to be done first.
  // If, if the batch has failed, that needs to be looked at, can't be left till this afternoon.
  // So it's all about, you know, the importance of what needs to be delivered on that specific day or that specific week and then prioritising it. Yeah.
  // And my, my day starts fairly, fairly early.
  // You know, that's not because of work, that's to do with my personal life.
  // But I find that, you know, if you're logged in early, early hours, then you're kind of ahead of the, the noise. Does that make sense?
  // Rather walking into something, The expectation from my SDMS is that we give them an update before a quarter past 8, and that's only for their next call at a quarter past 8. But you know, this isn't, this isn't something that's forced upon the team.
  // It's just the ask and, and it's the least I could do for the people that I work with, you know, and this goes back to like that relationship, you know, they hold their end of the bargain. "
  val successResponse = Json.parse(successResponseRaw).as[BusinessIncomeSourcesSummaryResponse]
}
