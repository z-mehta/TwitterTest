var app = angular.module('tweetMapApp', []);

app.factory('Twitter', function($http, $timeout) {

    var ws = new WebSocket("ws://localhost:9000/ws");

    var twitterService = {
        tweets: [],
        query: function (query) {
            ws.send(JSON.stringify({query: query}));
        }
    };

    ws.onmessage = function(event) {
        $timeout(function() {
            twitterService.tweets = JSON.parse(event.data).statuses;
        });
    };

    return twitterService;
});

app.controller('Search', function($scope, $http, $timeout, Twitter) {

    $scope.search = function() {
        Twitter.query($scope.query);
    };

});

app.controller('Tweets', function($scope, $http, $timeout, Twitter) {

    $scope.tweets=[] ;

    $scope.$watch(
        function() {
            return Twitter.tweets;
        },
        function(tweets) {
            $scope.tweets = tweets;

            console.log(tweets);
        },
        $scope.showClient = function(tweet) {
            $location.path('#/Timeline/' + tweet.user.screen_name);
        }
    );

});


