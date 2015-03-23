(function(){
	
	//defines the AngularJS app as a module
	angular.module('houseApp', [])
	
	//defines the $HTTP factory, one of the 3 service types
	.factory('myFactory', [ '$http', function($http) {
		return $http( {
			method : 'GET',
			url : 'xsp/houses'
		});
	} ])
	
	//provies the controller to the app, which handles the interaction of data (model) with the view (a la MVC)
	.controller('HouseCtrl', function($scope, $filter, myFactory) {
		
		//defines filter/search/etc. vars
		$scope.pageQty = 5; //detectPhone() ? 10 : 30;
		$scope.curPage = 0;
		
		//calculates the number of results
		$scope.numberOfPages = function() {
			return Math.ceil($scope.housesOfWesteros.length / $scope.pageQty) || 0;
		}
		
		//defines a boolean var
		$scope.showSearch = false;
		
		$scope.housesOfWesteros = [];
		//the factory is returning the promise of the $http, so handle success/error here
		myFactory.success( function(data, status, headers, config) {
			$scope.housesOfWesteros = data.dataAr;
			//$scope.predicate = "JobNum";
			//$scope.reverse = false;
		}).error( function(data, status, headers, config) {
			$scope.housesOfWesteros = null;
			console.log("data: " + data);
			console.log("status: " + status);
			console.log("headers: " + headers);
			console.log("config: " + JSON.parse(config));
		})
		 .then( function(){
			//angular.element('div.screenMask').css('visibility','hidden');
		});
		
	})
	
	// we already use the limitTo filter built-in to AngularJS,
	// this is a custom filter for startFrom
	.filter('startFrom', function() {
		return function(input, start) {
			start = +start; //parse to int
			return input.slice(start);
		}
	})
	//This directive allows us to pass a function in on an enter key to do what we want.
	.directive('ngEnter', function () {
	    return function (scope, element, attrs) {
	        element.bind("keydown keypress", function (event) {
	            if(event.which === 13) {
	                scope.$apply(function (){
	                    scope.$eval(attrs.ngEnter);
	                });
	                event.preventDefault();
	            }
	        });
	    };
	});

})();