import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import play.api.libs.json.JsValue
import play.api.test._
import play.api.test.Helpers._
import play.test

@RunWith(classOf[JUnitRunner])
class ApplicationSpec extends Specification {

  "Application" should {

    "render index template" in new WithApplication {
      val html = views.html.index("Coco")

      contentAsString(html) must contain("Coco")
    }

    "render the index page" in new WithApplication{
      val home = route(FakeRequest(GET, "/")).get

      status(home) must equalTo(OK)
      contentType(home) must beSome.which(_ == "text/html")
      contentAsString(home) must contain ("TweetMap")
    }

    "render the words1 page" in new WithApplication{
      val home = route(FakeRequest(GET, "/words1")).get

      status(home) must equalTo(OK)
      contentType(home) must beSome.which(_ == "text/html")
      contentAsString(home) must contain ("Tweet Words")
    }


    "search for tweets" in new WithApplication {

      val search = route(FakeRequest(GET,"/tweets?query=hello")).get

      status(search) must equalTo(OK)
      contentType(search) must beSome("application/json")
      (contentAsJson(search) \ "statuses").as[Seq[JsValue]].length must beGreaterThan(0)
    }

    "Timeline for tweets" in new WithApplication {

      val search = route(FakeRequest(GET,"/Timeline?name=a")).get

      status(search) must equalTo(OK)
      contentType(search) must beSome("text/html")

    }

    "Location for tweets" in new WithApplication {

      val search = route(FakeRequest(GET,"/Location?lname=india")).get

      status(search) must equalTo(OK)
      contentType(search) must beSome("text/html")
    }



    "words for tweets" in new WithApplication {

      val search = route(FakeRequest(GET,"/words?sname=india")).get

      status(search) must equalTo(OK)
      contentType(search) must beSome("text/html")
    }


    "stats for tweets" in new WithApplication {

      //val search = route(FakeRequest(GET,"/stats?words={RT=79, }")).get

      //status(search) must equalTo(OK)
      //contentType(search) must beSome("text/html")
    }



  }
}
