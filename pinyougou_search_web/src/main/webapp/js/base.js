// 定义模块:
var app = angular.module("pinyougou",[]);

//配置过滤器

app.filter('trustHtml',['$sce',function ($sce) {
    return function (data) {//传入被过滤的参数
        return $sce.trustAsHtml(data);//返回已经过滤的参数
    }
}]);