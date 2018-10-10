app.controller('searchController',function($scope,$location,searchService){


	//搜索
	$scope.search=function(){
        $scope.searchMap.pageNo= parseInt($scope.searchMap.pageNo);
		searchService.search($scope.searchMap).success(
			function(response){
				$scope.resultMap=response;
				buildPageLabel();
			}


		);		
	};


	$scope.searchMap={'keywords':'','category':'','brand':'','spec':{},'price':'','pageNo':1,'pageSize':40,'sortField':'','sort':''};//传入后台的搜索对象

	$scope.addSearchItem=function (key, value) {
		if(key == 'category' || key == 'brand' || key == 'price'){
			$scope.searchMap[key]=value;
		}else{
			$scope.searchMap.spec[key]=value;
		}


		$scope.search();
    }


    $scope.removeSearchItem=function (key) {
		if(key=="category" || key=="brand" || key == 'price'){

			$scope.searchMap[key]="";
		}else {
			delete $scope.searchMap.spec[key];
		}

        $scope.search();
    }

    buildPageLabel=function () {
		$scope.pageLabel=[];//新增分页栏属性

		var maxPageNo = $scope.resultMap.totalPages;
		var firstPage = 1;
		var lastPage  = maxPageNo;
		$scope.firstDot=true;//前面有
		$scope.lastDot=true;//后面有

		if($scope.resultMap.totalPages > 5){//如果总页数大于5
			if($scope.searchMap.pageNo <= 3){
				lastPage=5;
				$scope.firstDot=false;
			}else if ($scope.searchMap.pageNo >= lastPage-2){
				firstPage = maxPageNo-4;

				$scope.lastDot=false;
			}else {//中间页显示的页数
				firstPage = $scope.searchMap.pageNo-2;
				lastPage=$scope.searchMap.pageNo+2;
			}
		}else {
		    $scope.firstDot=false;
		    $scope.lastDot=false;
        }


		//循环产生页码标签

		for(var i=firstPage;i<lastPage;i++){
			$scope.pageLabel.push(i);
		}
    }

    //根据页码查询
	$scope.queryByPage=function (pageNo) {
		//页码认证

		if(pageNo<1 || pageNo>$scope.resultMap.totalPages){
			return;
		}

		$scope.searchMap.pageNo=pageNo;
		$scope.search();
    }


    //判读当前页为第一页

    $scope.isTopPage=function () {
        if($scope.searchMap.pageNo==1){
            return true;
        }else {
            return false;
        }
    }

    //判读当前页是否未最后一页

    $scope.isEndPage=function () {
        if($scope.searchMap.pageNo==$scope.resultMap.totalPages){
            return true;
        }else {
            return false;
        }
    }

    $scope.sortSearch=function (sortField,sort) {
        $scope.searchMap.sortField=sortField;
        $scope.searchMap.sort=sort;
        $scope.search();
    }

    //判断关键字是不是品牌
	$scope.keywordsIsBrand=function () {
		for(var i=0;i<$scope.resultMap.brandList.length;i++){
			if($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text) >= 0){
				return true;
			}
		}

		return false;
    }


    //加载查询字符串
	$scope.loadwords=function () {
		$scope.searchMap.keywords=$location.search()['keywords'];
		$scope.search();
    }

});