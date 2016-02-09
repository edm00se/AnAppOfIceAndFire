(function(){
	
	//defines the AngularJS app as a moduloadChildrenTagsle
	angular.module('westerosiApp')

	/*
	 *	Character Controllers
	 */

	 //provies the controller to the app, which handles the interaction of data (model) with the view (a la MVC)
	.controller('CharacterListCtrl', ['$scope','$state','$http','$filter','characterCollectionFactory',
		function($scope, $state, $http, $filter, characterCollectionFactory) {
		
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

	}])

	.controller('OneCharacterCtrl', ['$scope','$state','$stateParams','$http','$filter','characterFactory',
		function($scope, $state, $stateParams, $http, $filter, characterFactory){
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
						values[prop] = data[prop];
					}
					var tmpResp = {
						"editMode": true,
						"unid": data.unid,
						"values": values
					};
					$scope.myCharacter = tmpResp;
				}else{
					var values = {};
					for( var prop in data.values ){
						if( prop.indexOf("_FL") > -1 ){
							//do nothing
						}else{
							var tmpAr = prop.split("");
							if( tmpAr[0] == tmpAr[0].toUpperCase() ){
								tmpAr[0] = tmpAr[0].toLowerCase();
								var nwProp = tmpAr.join("");
								values[nwProp] = data.values[prop];
							}else{
								values[prop] = data.values[prop];
							}
						}
					}
					var tmpResp = {
						"editMode": true,
						"unid": data.unid,
						"values": values
					};
					$scope.myCharacter = tmpResp;
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

		$scope.loadChildrenTags = function(query){
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

		$scope.$on("$locationChangeStart", function(event) {
        	if ($scope.form.$dirty && !confirm('Abandon unsaved changes?')){
            	event.preventDefault();
        	}
        });

	}])

	.controller('NewCharacterCtrl', ['$scope','$state','$stateParams','$http','$filter','characterFactory','characterCollectionFactory',
		function($scope, $state, $stateParams, $http, $filter, characterFactory, characterCollectionFactory){

		$scope.editForm = true;
		$scope.canEditForm = true;
		$scope.myCharacter = {};
		var fieldNames = [];

		$scope.loadAbilityTags = function(query){
			//ideally return a searched set from $http w/ query val, etc.
	        //function returns array of object results
	        return $http.get('/tags/abilities.json');
		};

		// reusable char list with first + last names only
		$scope.peopleAr = [];
		var tmpCharAr = [];
		characterCollectionFactory
			.success( function(data, status, headers, config) {
				if( !data.hasOwnProperty("dataAr") ){
					//loading by non-Domino method, probably json-server; just use the response
					tmpCharAr = data;
				}else{
					tmpCharAr = data.dataAr;
				}
				for( var i=0; i<tmpCharAr.length; i++ ){
					var myChar = tmpCharAr[i];
					$scope.peopleAr.push(myChar.charFirstName+" "+myChar.charLastName);
				}
			}).error( function(data, status, headers, config){
				console.log("status: "+status);
				console.log("data: "+data);
				console.log("headers: "+headers);
				console.log("config: "+JSON.parse(config));
			});

		$scope.loadSiblingTags = function(query){
			return $scope.peopleAr;
		};

		$scope.loadParentTags = function(query){
			return $scope.peopleAr;
		};

		$scope.loadChildrenTags = function(query){
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
				method : 'POST',
				url : 'characters',
				data: JSON.stringify(tmpOb)
			})
				.success( function(data, status, headers, config){
					console.log("successfully updated character with unid: "+data.unid);
				})
				.error( function(data, status, headers, config){
					//might as well say something
					console.log("poop");
				})
				.then( function(){
					$state.go('characters',{},{reload: true});
				});
		};

		$scope.$on("$locationChangeStart", function(event) {
        	if ($scope.form!=undefined && $scope.form.$dirty && !confirm('Abandon unsaved changes?')){
            	event.preventDefault();
        	}
        });

	}]);

})();