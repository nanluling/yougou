app.service('loginService',function ($http) {

    this.loginName=function () {
        return $http('../login/name.do');
    }
});