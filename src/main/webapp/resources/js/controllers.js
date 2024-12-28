var cartApp = angular.module('cartApp', []);

cartApp.controller('cartCtrl',  function ($scope, $http) {

    $scope.refreshCart = function(cartId) {
        $http.get('/webstore_war_exploded/rest/cart/'+$scope.cartId)
            .success(function(data) {
                $scope.cart = data;
            });
    };

    $scope.clearCart = function() {
        $http.delete('/webstore_war_exploded/rest/cart/'+$scope.cartId)
            .success($scope.refreshCart($scope.cartId));

    };

    $scope.initCartId = function(cartId) {
        $scope.cartId=cartId;
        $scope.refreshCart($scope.cartId);
    };

    $scope.addToCart = function(productId) {
        $http.put('/webstore_war_exploded/rest/cart/add/'+productId)
            .success(function(data) {
                $scope.refreshCart($http.get('/webstore_war_exploded/rest/cart/cartId'));
                alert("Produkt pomyślnie dodany do koszyka!");
            });
    };
    $scope.removeFromCart = function(productId) {
        $http.put('/webstore_war_exploded/rest/cart/remove/'+productId)
            .success(function(data) {
                $scope.refreshCart($http.get('/webstore_war_exploded/rest/cart/cartId'));
            });
    };
});
