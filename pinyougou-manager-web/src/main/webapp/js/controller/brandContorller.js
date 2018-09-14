//品牌控制层
app.controller('brandController',function ($scope,$controller,brandService) {

    $controller('baseController',{$scope:$scope});

        //查询品牌列表
        $scope.findAll=function(){
            brandService.findAll().success(
                function(response){
                    $scope.list=response;
                }
            );
        }


        //分页
        $scope.findPage=function(page,size){
            brandService.findPage(page,size).success(
                function(response){
                    $scope.list=response.rows;//显示当前页数据
                    $scope.paginationConf.totalItems=response.total;//更新总记录数
                }
            );
        }

        //新增
        $scope.save=function(){
            var serviceObjrct;//方法名
            if($scope.entity.id!=null){
                serviceObjrct=brandService.update($scope.entity);
            }else {
                serviceObjrct=brandService.add($scope.entity);
            }
            brandService.save().success(
                function(response){
                    if(response.success){
                        $scope.reloadList();//刷新
                    }else{
                        alert(response.message);
                    }
                }
            );
        }

        //查询实体
        $scope.findOne=function(id){
            brandService.findOne(id).success(
                function(response){
                    $scope.entity=response;
                }
            );
        }
        //删除
        $scope.delete=function(){
            //获取选中的复选框
            brandService.delete( $scope.selectIds).success(
                function(response){
                    if(response.success){
                        $scope.reloadList();//刷新列表
                        $scope.selectIds=[];
                    }
                }
            );

        }

        //查询符合条件的所有
        $scope.searchEntity={};
        //条件查询
        $scope.search=function(page,rows){

            brandService.search(page,rows,$scope.searchEntity).success(
                function(response){
                    $scope.list=response.rows;//显示当前页数据
                    $scope.paginationConf.totalItems=response.total;//更新总记录数
                }
            );

        }

});