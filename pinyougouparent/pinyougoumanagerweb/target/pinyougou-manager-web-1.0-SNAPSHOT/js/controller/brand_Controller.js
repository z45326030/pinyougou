app.controller("brandController",function ($scope,$controller,brandService) {//构建前端控制器

    $controller("baseController",{$scope:$scope});

    $scope.findAll=function () {
        brandService.findAll().success(function(response){
            $scope.list=response;
        })
    }

    //分页

    $scope.findPage=function(page,rows){
        brandService.findPage(page,rows).success(function(response){
            $scope.list=response.rows;
            $scope.paginationConf.totalItems=response.total;//定义总记录数
        })
    }



    //条件查询+分页
    $scope.search=function(page,rows){
        brandService.search(page,rows,$scope.searchEntity).success( function (response) {
            $scope.list=response.rows;
            $scope.paginationConf.totalItems=response.total;//定义总记录数
        })
    }

    //新增品牌
    $scope.save=function () {
        var object=null;
        if($scope.entity.id!=null){
            object=brandService.update($scope.entity);
        }else{
            object=brandService.add($scope.entity);
        }
        object.success(function (response) {
            if(response.success){
                $scope.reloadList();
            }else{
                alert(response.message);
            }
        })
    }

    $scope.findOne=function(id){
        brandService.findOne(id).success(function (response) {
            $scope.entity=response;
        })
    }

    //删除
    $scope.dele=function(){
        if($scope.selectIds.length==0){
            alert("请选择！");
            return;
        }
        brandService.dele($scope.selectIds).success(function (response) {
            if(response.success){
                $scope.reloadList();//刷新列表
                $scope.selectIds=[];
            }else{
                alert(response.message );
            }
        })
    }


})
