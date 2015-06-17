(function(){
	
	//defines the AngularJS app as a module
	angular.module('houseApp', ['ui.router']) //'ngAnimate'

	//ui-router config
	.config(
		function($stateProvider, $urlRouterProvider){
			
			$urlRouterProvider.otherwise('/houses');
			
			$stateProvider
				.state('houses', {
					url: '/houses',
					templateUrl: 'partials/houseList.html',
					controller: 'HouseListCtrl'
				})
				.state('houses.item', {
					url: '/:item',
					templateUrl: 'partials/house.html',
					controller: 'OneHouseCtrl'
				});
	})

	/*
	 *	Factories
	 */
	
	//defines the $HTTP factory, one of the 3 service types
	.factory('houseCollectionFactory', [ '$http', function($http) {
		return $http( {
			method : 'GET',
			url : 'xsp/houses'
		});
	} ])

	.factory('houseFactory', [ '$http', function($http){
		return function(id){
			return $http( {
				method : 'GET',
				url : 'xsp/houses/'+id
			});
		}
	}])

	/*
	 *	Controllers
	 */

	//navigation controller
	.controller('NavCtrl', function($scope, $location){
		$scope.isActive = function(route) {
	    	return route === $location.path();
	    }
	})
	
	//provies the controller to the app, which handles the interaction of data (model) with the view (a la MVC)
	.controller('HouseListCtrl', function($scope, $state, $http, $filter, houseCollectionFactory) {
		
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
		houseCollectionFactory
			.success( function(data, status, headers, config) {
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

		$scope.removeHouse = function(unid){
			$http( {
				method : 'DELETE',
				url : 'houses/'+unid
			})
			.success( function(data, status, headers, config){
				console.log("successfully deleted house with id: "+unid);
			})
			.error( function(data, status, headers, config){
				//might as well say something
				console.log("poop");
			})
			.then( function(){
				$state.go('houses',{reload: true});
			});
		};
		
	})
	
	.controller('OneHouseCtrl', function($scope, $state, $stateParams, $http, houseFactory){
		// check for empty ID
		var tmpItm = $stateParams.item;
		console.log("unid: "+tmpItm);
		var re = /^[0-9A-Za-z]{32}$/;
		//var re = /\d/;
		if( tmpItm == null || tmpItm == undefined || (!tmpItm || !tmpItm.trim()) || !re.test(tmpItm) ){
			$state.go('houses');
		}

		$scope.editForm = false;
		$scope.canEditForm = false;
		$scope.myHouse = {};
		var fieldNames = [];
		houseFactory($stateParams.item)
			.success(function(data, status, headers, config) {
				$scope.myHouse = data;
				$scope.canEditForm = true;
				angular.forEach($scope.myHouse,function(value,key){
					if( key!="unid" ){
						fieldNames.push(key);
					}
				});
			})
			.error(function(data, status, headers, config) {
				console.log("status: "+status);
				console.log("data: "+data);
				console.log("headers: "+headers);
				console.log("config: "+JSON.parse(config));
			});
		$scope.setFormEditable = function() {
			if( $scope.canEditForm == true ){
				$scope.editForm = true;
			}
		}
		$scope.clearCancelForm = function() {
			$state.go('houses');
		}

		$scope.saveHouseForm = function(){
			var tmpOb = { "unid": $scope.myHouse.unid };
			//console.log("checking field names: "+fieldNames.toString());
			angular.forEach(fieldNames, function(fldNm){
				if( $scope.houseForm[fldNm].$dirty === true ){
					var tmpVal = $scope.myHouse[fldNm];
					//console.log("updated field: "+fldNm+" with value: "+tmpVal);
					tmpOb[fldNm] = tmpVal;
				}
			});
			
			$http( {
				method : 'PUT',
				url : 'houses/'+$scope.myHouse.unid,
				data: JSON.stringify(tmpOb)
			})
				.success( function(data, status, headers, config){
					console.log("successfully updated house with unid: "+$scope.myHouse.unid);
				})
				.error( function(data, status, headers, config){
					//might as well say something
					console.log("poop");
				})
				.then( function(){
					$state.go('houses',{reload: true});
				});
			
			//console.log("Simulated PUT complete with object to send: "+JSON.stringify(tmpOb));
		}

	})

	/*
	 *	Filters
	 */
	
	// we already use the limitTo filter built-in to AngularJS,
	// this is a custom filter for startFrom
	.filter('startFrom', function() {
		return function(input, start) {
			start = +start; //parse to int
			return input.slice(start);
		}
	})

	/*
	 *	Directives
	 */

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
	})

	/**
	 * A generic confirmation for risky actions.
	 * Usage: Add attributes: ng-really-message="Are you sure"? ng-really-click="takeAction()" function
	 */
	.directive('ngReallyClick', [function() {
	    return {
	        restrict: 'A',
	        link: function(scope, element, attrs) {
	            element.bind('click', function() {
	                var message = attrs.ngReallyMessage;
	                if (message && confirm(message)) {
	                    scope.$apply(attrs.ngReallyClick);
	                }
	            });
	        }
	    }
	}]);

})();