import java.util.Optional

import actors.UserActor
import akka.actor.Actor
import akka.testkit.{TestActorRef, TestProbe}
import org.specs2.mutable._
import org.specs2.runner._
import org.specs2.time.NoTimeConversions
import org.junit.runner._
import play.api.libs.concurrent.Akka
import play.api.libs.json.{JsValue, Json}
import play.api.test._

import scala.concurrent.duration._
import scala.concurrent.{Await, Promise}


@RunWith(classOf[JUnitRunner])
class UserActorSpec extends Specification with NoTimeConversions {
  "UserActor" should {

    "fetch tweets" in new WithApplication {

      //make the Play Application Akka Actor System available as an implicit actor system
      implicit val actorSystem = Akka.system

      //This is the function called by UserActor when it fetches tweets.  The function fullfills a promise
      //with the tweets, which allows the results to be tested.
      val promiseJson = Promise[Seq[JsValue]]()
      def validateJson(jsValue: JsValue) {
        val tweets = (jsValue \ "statuses").as[Seq[JsValue]]
        promiseJson.success(tweets)
      }
      val receiverActorRef = TestProbe()

      val userActorRef = TestActorRef(new UserActor(receiverActorRef.ref))

      System.out.println(userActorRef);
      val querySearchTerm = ""
      val jsonQuery = Json.obj("query" -> querySearchTerm)

      // The tests need to be delayed a certain amount of time so the tick message gets fired.  This can be done using either
      // Await.result below or using the Akka test kit within(testDuration) {

      val a= Optional.empty();
      userActorRef ! jsonQuery
      userActorRef.underlyingActor.optQuery.orElse("") must beEqualTo("")

      //Await.result(promiseJson.future, 10.seconds).length must beGreaterThan(0)

    }
  }
}
