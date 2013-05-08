/* global angular, console */
angular.module('irida.users', [
  'ajaxService'])

/**
 * Configure the route parameters
 * $routeProvider
 */
.config(['$routeProvider', function ($routeProvider) {
  $routeProvider.when(
    '/users', {
    templateUrl: './partials/users.html',
    controller: function ($scope, initData) {
      $scope.users = initData.resources.resources;
      $scope.links = initData.resources.links;
    },
    resolve: {
      initData: function ($q, ajaxService) {
        var defer = $q.defer();
        ajaxService.get('/users').then(function (data) {
          defer.resolve(data);
        });
        return defer.promise;
      }
    }
  });
}])
  .controller('UsersListCtrl', ['$scope', '$route', '$location', 'ajaxService', function ($scope, $route, $location, ajaxService) {
  'use strict';

  $scope.loadUsers = function (url) {
    if (url) {
      ajaxService.get(url).then(

      function (data) {
        ajaxSuccessCallback(data);
      },

      function (errorMessage) {
        // TODO: handle error message
        console.log(errorMessage);
      });
    }
  };

  $scope.gotoUser = function (url) {
    $location.path(url.match(/\/users\/.*$/)[0]);
  };

  $scope.clearForm = function () {
    $scope.newUser = {};
    $scope.errors = {};

    // Need to reset all the fields in the form.
    $('form[name=newUserForm] .ng-dirty').removeClass('ng-dirty').addClass('ng-pristine');
    var form = $scope.newUserForm;
    for (var field in form) {
      if (form[field].$pristine === false) {
        form[field].$pristine = true;
      }
      if (form[field].$dirty === true) {
        form[field].$dirty = false;
      }
      if (form[field].$invalid) {
        form[field].$invalid = false;
      }
    }
    $scope.newUserForm.$pristine = true;
  };

  $scope.submitNewUser = function () {
    if ($scope.newUserForm.$valid) {
      ajaxService.create('/users', $scope.newUser).then(

      function () {
        $scope.loadUsers('/users');
        $scope.clearForm();
        $('#newUserModal').foundation('reveal', 'close');
      },

      function (data) {
        $scope.errors = {};
        angular.forEach(data, function (error, key) {
          $scope.errors[key] = data[key].join('</br>');
        });
      });
    } else {
      console.log('NOT VALID');
    }
  };

  function ajaxSuccessCallback(data) {
    $scope.links = data.resources.links;
    $scope.users = data.resources.resources;
  }

  $scope.$on('$routeChangeSuccess', function () {
    $(document).foundation('reveal', {});
  });
}]);