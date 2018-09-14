//服务层
app.service('brandService',function ($http) {
    this.findAll=function () {
        return $http.get('../brand/findAll.do');
    }

    this.findPage=function (page,size) {
        return $http.get('../brand/findPage.do?page='+page +'&size='+size)
    }

    this.save=function (entity) {
        return $http.post('../brand/add.do',entity);
    }

    this.findOne=function (id) {
        return $http.get('../brand/findOne.do?id='+id);
    }

    this.delete=function (selectIds) {
        return $http.get('../brand/delete.do?ids='+selectIds);
    }

    this.search=function (page, rows, searchEntity) {
        return $http.post('../brand/search.do?page='+page +'&rows='+rows, searchEntity);
    }

});