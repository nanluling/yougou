app.controller('indexController',function ($scope,$controller,loginService) {


    $scope.showLoginName=function () {
        loginService.loginName().success(

            function (response) {
                //这是取属性值不是 调用方法注意   response.loginName()带点脑子
                $scope.loginName=response.loginName;
            }
        )
    }
});