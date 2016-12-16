(function(){
	
	//defines the AngularJS app as a module
	angular.module('westerosiApp')

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
	}]);

})();