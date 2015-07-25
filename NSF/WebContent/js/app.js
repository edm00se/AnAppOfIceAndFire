(function(){
	
	//defines the AngularJS app as a module
	angular.module('houseApp', ['ui.router','ngTagsInput','ngMessages']) //'ngAnimate'

	//ui-router config
	.config(
		function($stateProvider, $urlRouterProvider){
			
			$urlRouterProvider.otherwise('/about');
			
			$stateProvider
				.state('about', {
					url: '/about',
					templateUrl: 'partials/about.html'
				})
				.state('houses', {
					url: '/houses',
					templateUrl: 'partials/houseList.html',
					controller: 'HouseListCtrl'
				})
				.state('newHouse',{
					url: '/newHouse',
					templateUrl: 'partials/house.html',
					controller: 'NewHouseCtrl'
				})
				.state('houses.item', {
					url: '/:item',
					templateUrl: 'partials/house.html',
					controller: 'OneHouseCtrl'
				})
				.state('characters', {
					url: '/characters',
					templateUrl: 'partials/characterList.html',
					controller: 'CharacterListCtrl'
				})
				.state('newCharacter', {
					url: '/:item',
					templateUrl: 'partials/character.html',
					controller: 'NewCharacterCtrl'
				})
				.state('characters.item', {
					url: '/:item',
					templateUrl: 'partials/character.html',
					controller: 'OneCharacterCtrl'
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

	.factory('characterCollectionFactory', [ '$http', function($http){
		return $http( {
			method : 'GET',
			url : 'xsp/characters/'
		});
	}])

	.factory('characterFactory', [ '$http', function($http){
		return function(id){
			return $http( {
				method : 'GET',
				url : 'xsp/characters/'+id
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
				if( !data.hasOwnProperty("dataAr") ){
					//loading by non-Domino method, probably json-server; just use the response
					$scope.housesOfWesteros = data;
				}else{
					$scope.housesOfWesteros = data.dataAr;
				}
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
				console.log("error, status: "+status+"\nmessage: "+data);
			})
			.then( function(){
				$state.go('houses',{},{reload: true});
			});
		};
		
	})
	
	.controller('OneHouseCtrl', function($scope, $state, $stateParams, $http, houseFactory){
		// check for empty ID
		var tmpItm = $stateParams.item;
		console.log("unid: "+tmpItm);
		if( tmpItm.indexOf("-") > -1 ){
			tmpItm = tmpItm.replace(/-/g,"");
		}
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
				if( !data.hasOwnProperty("values") ){
					var values = {};
					for( var prop in data ){
						values[prop] = data[prop];
					}
					var tmpResp = {
						"editMode": true,
						"unid": data.unid,
						"values": values
					};
					$scope.myHouse = tmpResp;
				}else{
					$scope.myHouse = data;
				}
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
					$state.go('houses',{},{reload: true});
				});
			
		}

	})
	
	.controller('NewHouseCtrl', function($scope, $state, $stateParams, $http){

		$scope.editForm = true;
		$scope.canEditForm = true;
		$scope.myHouse = {};
		var fieldNames = [];

		$scope.myHouse = {};
		$scope.setFormEditable = function() {
			if( $scope.canEditForm == true ){
				$scope.editForm = true;
			}
		}
		$scope.clearCancelForm = function() {
			$state.go('houses');
		}

		$scope.saveHouseForm = function(){

			var nwUnid = null;
			
			$http( {
				method : 'POST',
				url : 'houses',
				data: JSON.stringify($scope.myHouse.values)
			})
				.success( function(data, status, headers, config){
					var rawUnid = headers('Location'); // ex: /xsp/houses/:unid
					if( rawUnid == null ){ //running json-server
						nwUnid = data.unid;
						console.log("successfully saved new house with unid: "+nwUnid );
					}else{
						nwUnid = rawUnid.split("/")[3];
						console.log("successfully saved new house with unid: "+nwUnid );
					}
				})
				.error( function(data, status, headers, config){
					//might as well say something
					console.log("error: "+data);
				})
				.then( function(){
					if( nwUnid!=null ){
						$state.go('houses.item',{item: nwUnid},{reload: true});
					}else{
						$state.go('houses',{},{reload: true});
					}
				});
			
		}

	})

	//provies the controller to the app, which handles the interaction of data (model) with the view (a la MVC)
	.controller('CharacterListCtrl', function($scope, $state, $http, $filter, characterCollectionFactory) {
		
		//defines filter/search/etc. vars
		$scope.pageQty = 5; //detectPhone() ? 10 : 30;
		$scope.curPage = 0;
		
		//calculates the number of results
		$scope.numberOfPages = function() {
			return Math.ceil($scope.charactersOfWesteros.length / $scope.pageQty) || 0;
		}
		
		//defines a boolean var
		$scope.showSearch = false;
		
		$scope.charactersOfWesteros = [];
		//the factory is returning the promise of the $http, so handle success/error here
		characterCollectionFactory
			.success( function(data, status, headers, config) {
				if( !data.hasOwnProperty("dataAr") ){
					//loading by non-Domino method, probably json-server; just use the response
					$scope.charactersOfWesteros = data;
				}else{
					$scope.charactersOfWesteros = data.dataAr;
				}
			}).error( function(data, status, headers, config) {
				$scope.charactersOfWesteros = null;
				console.log("data: " + data);
				console.log("status: " + status);
				console.log("headers: " + headers);
				console.log("config: " + JSON.parse(config));
			})
			 .then( function(){
				//angular.element('div.screenMask').css('visibility','hidden');
			});

		$scope.removeCharacter = function(unid){
			$http( {
				method : 'DELETE',
				url : 'characters/'+unid
			})
			.success( function(data, status, headers, config){
				console.log("successfully deleted character with id: "+unid);
			})
			.error( function(data, status, headers, config){
				//might as well say something
				console.log("error, status: "+status+"\nmessage: "+data);
			})
			.then( function(){
				$state.go('characters',{},{reload: true});
			});
		};

	})

	.controller('OneCharacterCtrl', function($scope, $state, $stateParams, $http, $filter, characterFactory){
		// check for empty ID
		var tmpItm = $stateParams.item;
		console.log("unid: "+tmpItm);
		if( tmpItm.indexOf("-") > -1 ){
			tmpItm = tmpItm.replace(/-/g,"");
		}
		var re = /^[0-9A-Za-z]{32}$/;
		//var re = /\d/;
		if( tmpItm == null || tmpItm == undefined || (!tmpItm || !tmpItm.trim()) || !re.test(tmpItm) ){
			$state.go('houses');
		}

		$scope.editForm = false;
		$scope.canEditForm = false;
		$scope.myCharacter = {};
		var fieldNames = [];

		characterFactory($stateParams.item)
			.success(function(data, status, headers, config) {
				if( !data.hasOwnProperty("values") ){
					var values = {};
					for( var prop in data ){
						if( prop.indexOf("_FL") > -1 ){
							//do nothing
						}else{
							var tmpAr = prop.split("");
							if( tmpAr[0] == tmpAr[0].toUpperCase() ){
								tmpAr[0] = tmpAr[0].toLowerCase();
								var nwProp = tmpAr.join("");
								values[nwProp] = data[prop];
							}else{
								values[prop] = data[prop];
							}
						}
					}
					var tmpResp = {
						"editMode": true,
						"unid": data.unid,
						"values": values
					};
					$scope.myCharacter = tmpResp;
				}else{
					$scope.myCharacter = data;
				}
				$scope.canEditForm = true;
				angular.forEach($scope.myCharacter.values,function(value,key){
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

		$scope.loadAbilityTags = function(query){
			//ideally return a searched set from $http w/ query val, etc.
	        //function returns array of object results
	        return $http.get('/tags/abilities.json');
		};

		// reusable char list with first + last names only
		$scope.peopleAr = [];
		var tmpCharAr = $scope.$parent.charactersOfWesteros;
		for( var i=0; i<tmpCharAr.length; i++ ){
			var myChar = tmpCharAr[i];
			$scope.peopleAr.push(myChar.charFirstName+" "+myChar.charLastName);
		}

		$scope.loadSiblingTags = function(query){
			return $scope.peopleAr;
		};

		$scope.loadParentTags = function(query){
			return $scope.peopleAr;
		};

		$scope.setFormEditable = function() {
			if( $scope.canEditForm == true ){
				$scope.editForm = true;
			}
		};

		$scope.clearCancelForm = function() {
			$state.go('characters');
		};

		$scope.saveCharacterForm = function(){

			// the ng-tags-input values need to be returned to array of literals (strings)
			var multiValueFields = ["abilities","siblings","parents","children"];

			var tmpOb = { "unid": $scope.myCharacter.unid };
			//console.log("checking field names: "+fieldNames.toString());
			angular.forEach(fieldNames, function(fldNm){
				var nmG2g = fldNm!="unid";
				var dirtyG2g = !!$scope.characterForm[fldNm].$dirty && $scope.characterForm[fldNm].$dirty == true;
				if( nmG2g && dirtyG2g ){ //ignore multi-value fields
					var isMulti = isInArray(fldNm,multiValueFields);
					if( isMulti ){
						//handle multi-value fields by converting to simple string array
						var tmpAr = [];
						for( var i=0; i<$scope.myCharacter.values[fld].length; i++ ){
							var ob = $scope.myCharacter.values[fld][i];
							tmpAr.push(ob.text);
						}
						//set string array back to tmp object
						tmpOb[fldNm] = tmpAr;
					}else{
						//set new value back to the tmp object
						tmpOb[fldNm] = $scope.myCharacter.values[fldNm];
					}
				}
			});
			
			$http( {
				method : 'PUT',
				url : 'characters/'+$scope.myCharacter.unid,
				data: JSON.stringify(tmpOb)
			})
				.success( function(data, status, headers, config){
					console.log("successfully updated character with unid: "+$scope.myCharacter.unid);
				})
				.error( function(data, status, headers, config){
					//might as well say something
					console.log("poop");
				})
				.then( function(){
					$state.go('characters',{},{reload: true});
				});
		};

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

	// filters array of objects by property, src: http://stackoverflow.com/a/18186947/1720082
	.filter('orderObjectBy', function(){
		return function(input, attribute) {
			if (!angular.isObject(input)){
				return input;
			}

			var array = [];
			for(var objectKey in input) {
			    array.push(input[objectKey]);
			}

			array.sort(function(a, b){
			    a = parseInt(a[attribute]);
			    b = parseInt(b[attribute]);
			    return a - b;
			});
			return array;
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

	.directive('tooltip', function(){
	    return {
	        restrict: 'A',
	        link: function(scope, element, attrs){
	            angular.element(element).hover(function(){
	                // on mouseenter
	                angular.element(element).tooltip('show');
	            }, function(){
	                // on mouseleave
	                angular.element(element).tooltip('hide');
	            });
	        }
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
	            	//fails over should editForm not be in scope
	            	//confirmed: http://jsfiddle.net/edm00se/5t4rhmcf/show/
	            	if( !!scope.editForm && scope.editForm ){
		                var message = attrs.ngReallyMessage;
		                if (message && confirm(message)) {
		                    scope.$apply(attrs.ngReallyClick);
		                }
	            	}else{
	            		scope.$apply(attrs.ngReallyClick);
	            	}
	            });
	        }
	    }
	}]);

})();