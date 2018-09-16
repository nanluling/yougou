app.controller('indexController',function ($scope,$controller,loginService) {

    //读取当前的登录人

    $scope.showLoginName=function () {
        loginService.loginName().success(
            function (response) {
                $scope.longName=response.getName;
            }
        )
    }

});