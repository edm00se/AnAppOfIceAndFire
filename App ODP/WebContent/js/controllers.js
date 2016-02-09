(function(){
	
	//defines the AngularJS app as a module
	angular.module('westerosiApp')

	/*
	 *	General Controllers
	 */

	//navigation controller
	.controller('NavCtrl', ['$scope', '$location', function($scope, $location){
		$scope.isActive = function(route) {
	    	return route === $location.path();
	    }
	}]);

})();