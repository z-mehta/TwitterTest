package controllers;

import actors.UserActor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import play.api.mvc.AnyContentAsJson;
import play.libs.F.Promise;
import play.libs.Json;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.WebSocket;
import actors.UserActor;


import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import scala.collection.immutable.ListMap;
import utils.Streams;
import views.html.index;
import views.html.*;

import static java.util.stream.Collectors.*;
import static utils.Streams.stream;

public class Tweets extends Controller {

    public static Result index() {

        WebSocket.withActor(UserActor::props);
        return ok(views.html.index.render("TweetMap"));
    }

    public static Promise<Result> search(String query) {

        return fetchTweets(query)
                .map(jsonNode -> ok(jsonNode));
    }

    public static Result Timeline(String name) {

        //System.out.println(name);
       String token= "AAAAAAAAAAAAAAAAAAAAAHB%2F4wAAAAAAI69NlJD0CNk9SlQRy697nPF5oJQ%3DVYBnbQFjiGCL2WIXFuH3QJmOrGNmEO6kCjcXwZkdL7Z3sZHPhM";
       Promise<WSResponse> responsePromise = WS.url("https://api.twitter.com/1.1/users/show.json")
               .setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8")
               .setHeader("Authorization", "Bearer "+token)
               .setQueryParameter("screen_name", name)
               .setQueryParameter("count", "10").get()  ;

       Promise<JsonNode> a=responsePromise
                .filter(response -> response.getStatus() == Http.Status.OK)
                .map(response -> response.asJson());
        JsonNode jn=a.get(1000);

        String image=jn.get("profile_image_url_https").asText();
        String uname=jn.get("name").asText();
        String description=jn.get("description").asText();
        String following=jn.get("friends_count").asText();
        String followers=jn.get("followers_count").asText();

        Promise<WSResponse> responsePromise1 =WS.url("https://api.twitter.com/1.1/statuses/user_timeline.json")
                .setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8")
                .setHeader("Authorization", "Bearer "+token)
                .setQueryParameter("screen_name", name)
                .setQueryParameter("count", "10")
                .get()  ;

        Promise<JsonNode> a1=responsePromise1
                .filter(response -> response.getStatus() == Http.Status.OK)
                .map(response -> response.asJson());

        List<String> text= new ArrayList<>();
        for(JsonNode node : a1.get(1000)) {
            text.add( node.path("text").asText());
        }

        WebSocket.withActor(UserActor::props);

        return ok(views.html.user.render(image,uname,description,following,followers,text) );

        //can also map using method references - WSResponse::asJson*/
      // return responsePromise
        //       .filter(response -> response.getStatus() == Http.Status.OK)
          //     .map(response -> response.asJson())
            //   .recover(Tweets::errorResponse);

    }

    public static Result Location(String name) {

        //System.out.println(name);
        String token= "AAAAAAAAAAAAAAAAAAAAAHB%2F4wAAAAAAI69NlJD0CNk9SlQRy697nPF5oJQ%3DVYBnbQFjiGCL2WIXFuH3QJmOrGNmEO6kCjcXwZkdL7Z3sZHPhM";

        Promise<WSResponse> responsePromise1 = WS.url("https://api.twitter.com/1.1/search/tweets.json")
                .setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8")
                .setHeader("Authorization", "Bearer "+token)
                .setQueryParameter("count","10")
                .setQueryParameter("statuses.user.location",name)
                   .setQueryParameter("q", name).get()  ;

        Promise<JsonNode> a1=responsePromise1
                .filter(response -> response.getStatus() == Http.Status.OK)
                .map(response -> response.asJson());

        List<String> text= new ArrayList<>();
        Map<String,String> ltweet= new LinkedHashMap<>();
        for(JsonNode node : a1.get(1000).path("statuses")) {
            ltweet.put(node.path("text").asText(),node.path("user").path("location").asText());
//            System.out.println(node.path("user").path("location").asText());
        }
        //System.out.println(text);
        WebSocket.withActor(UserActor::props);

        return ok(views.html.location.render(name,  ltweet) );

    }

    public static Result words(String name) {

        //System.out.println(name);
        String token= "AAAAAAAAAAAAAAAAAAAAAHB%2F4wAAAAAAI69NlJD0CNk9SlQRy697nPF5oJQ%3DVYBnbQFjiGCL2WIXFuH3QJmOrGNmEO6kCjcXwZkdL7Z3sZHPhM";

        Promise<WSResponse> responsePromise1 = WS.url("https://api.twitter.com/1.1/search/tweets.json")
                .setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8")
                .setHeader("Authorization", "Bearer "+token)
                .setQueryParameter("count","100")
                .setQueryParameter("q", name)
                .get()  ;

        Promise<JsonNode> a1=responsePromise1
                .filter(response -> response.getStatus() == Http.Status.OK)
                .map(response -> response.asJson());

        List<String> text= new ArrayList<>();
        List<String> utext= new ArrayList<>();

        for(JsonNode node : a1.get(10000).path("statuses")) {
//            System.out.println(node.path("user").path("location").asText());
            text.add(node.path("text").asText());
            utext.add(node.path("user").path("screen_name").asText());

        }
        //System.out.println(text.size());
        List<String> text1=text.stream().map(w -> w.split(" "))
                .flatMap(Arrays::stream)
                //.distinct()
                .collect(Collectors.toList());

        //counting happy emoticons
        Long happycount=text1.stream().filter(
          s -> s.contains("\uD83D\uDE0A") || s.contains("\uD83D\uDE02") || s.contains("\uD83D\uDE03")
                  || s.contains("\uD83D\uDE04") || s.contains(":)") || s.contains(":-)")
        ).count();
        //System.out.println(happycount);

        //counting sad emoticons
        Long sadcount=text1.stream().filter(
                s ->s.contains("\uD83D\uDE23") || s.contains("\uD83D\uDE29") || s.contains("\uD83D\uDE13")
                        || s.contains("\uD83D\uDE20")    || s.contains("\uD83D\uDE21")  || s.contains("\uD83D\uDE22")
                        || s.contains("\uD83D\uDE1E") || s.contains("\uD83D\uDE2D") || s.contains(":(")
                        || s.contains(":-(")
        ).count();
        //System.out.println(sadcount);

        //counting neutral emoticons
        Long neutralcount=text1.stream().filter(
                s -> s.equals(":-|")  || s.equals(":|")
        ).count();
        //System.out.println(neutralcount);


        //ratio
        Double total= Double.valueOf(happycount+sadcount+neutralcount);
        Double happyratio=(happycount/total)*100;
        Double sadratio=(sadcount/total)*100;

        //System.out.println(total+"h"+happyratio+"sad"+sadratio);
        String sentiment;
        if(happyratio>=70){sentiment= ":-)";}
        else if(sadratio>=70){sentiment=":-(";}
        else{sentiment=":-|";}
        //System.out.println("sen"+sentiment);

        //System.out.println(text1.size());


        Map<String, Integer> collect =
                text1.stream().collect(groupingBy(Function.identity(), summingInt(e -> 1)));

        LinkedHashMap<String, Integer> countByWordSorted = collect.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (v1, v2) -> {
                            throw new IllegalStateException();
                        },
                        LinkedHashMap::new
                ));
        //System.out.println(countByWordSorted);



        countByWordSorted.remove(" ");
        countByWordSorted.remove("");

        //System.out.println(text1);


        WebSocket.withActor(UserActor::props);
        return ok(views.html.words.render(text,countByWordSorted,utext,name,sentiment) );


    }

    public static Result stats(String words){
        //System.out.println(words);
        String words1=words.replace("{","")
                .replaceAll("}","");

        //System.out.println(words1);
        Long total=Stream.of(words1)
                .map(w->w.split(", "))
                .flatMap(Arrays::stream)
                .distinct().count();


       WebSocket.withActor(UserActor::props);
        return ok(views.html.stats.render( words1,total ));
    }

    public static Result words1(){
        WebSocket.withActor(UserActor::props);
    return ok(views.html.words1.render());}


    public static WebSocket<JsonNode> ws() {
        //create the UserActor using the static UserActor props method.
        //the withActor method passes an out ActorRef that is wired to the
        //WebSocket out channel.
        //Use this out as the out parameter for UserActor
        return WebSocket.withActor(out -> UserActor.props(out));
    }


    /**
     * Fetch the latest tweets and return the Promise of the json results.
     * This fetches the tweets asynchronously and fulfills the promise when the results are returned.
     * The results are first filtered and only returned if the result status was OK.
     * Then the results are mapped (or transformed) to JSON.
     * Finally a recover is added to the Promise to return an error Json if the tweets couldn't be fetched.
     *
     * @param query
     * @return
     */
    public static Promise<JsonNode> fetchTweets(String query1) {
        String token= "AAAAAAAAAAAAAAAAAAAAAHB%2F4wAAAAAAI69NlJD0CNk9SlQRy697nPF5oJQ%3DVYBnbQFjiGCL2WIXFuH3QJmOrGNmEO6kCjcXwZkdL7Z3sZHPhM";
        //"http://twitter-search-proxy.herokuapp.com/search/tweets"
        Promise<WSResponse> responsePromise = WS.url("https://api.twitter.com/1.1/search/tweets.json")
                .setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8")
                .setHeader("Authorization", "Bearer "+token)
                .setQueryParameter("count","10")
                .setQueryParameter("q", query1).get()  ;



        //can also map using method references - WSResponse::asJson
        return responsePromise
                .filter(response -> response.getStatus() == Http.Status.OK)
                .map(response -> response.asJson())
                .recover(Tweets::errorResponse);
    }


    /**
     * The error response when the twitter search fails.
     *
     * @param ignored
     * @return
     */
    public static JsonNode errorResponse(Throwable ignored) {
        return Json.newObject().put("error", "Could not fetch the tweets");
    }
}
