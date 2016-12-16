(function(){
	
	//defines the AngularJS app as a module
	angular.module('westerosiApp',
		[
		'ui.router',
		'ngTagsInput',
		'ngMessages' //'ngAnimate'
		])

	//ui-router config
	.config(
		['$stateProvider', '$urlRouterProvider', function($stateProvider, $urlRouterProvider){
			
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
					url: '/newCharacter',
					templateUrl: 'partials/character.html',
					controller: 'NewCharacterCtrl'
				})
				.state('characters.item', {
					url: '/:item',
					templateUrl: 'partials/character.html',
					controller: 'OneCharacterCtrl'
				});
	}]);

})();