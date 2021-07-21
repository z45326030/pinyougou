app.controller("baseController",function ($scope) {//父控制器

    $scope.searchEntity={};

    //分页配置
    $scope.paginationConf={
        currentPage: 1,//当前页
        totalItems: 10,//总记录数
        itemsPerPage: 10,//每页记录数
        perPageOptions: [10, 20, 30, 40, 50],//页码选项
        onChange: function(){	//当页码发生变化的时候自动触发的方法
            $scope.reloadList();
        }
    }

    //重新加载记录
    $scope.reloadList=function(){
        $scope.search( $scope.paginationConf.currentPage,$scope.paginationConf.itemsPerPage );
    }


    $scope.selectIds=[];//选中的ID数组
    $scope.updateSelection=function($event,id){
        if($event.target.checked){//判断是否选中
            $scope.selectIds.push( id );
        }else{
            //splice删除
            var idx= $scope.selectIds.indexOf(id);//ID在数组中的位置
            $scope.selectIds.splice( idx ,1);
        }
    }

    //提取json中的数据
    $scope.jsonToString=function (jsonString,key) {

        var json= JSON.parse( jsonString );
        var value="";
        for(var i=0;i<json.length;i++ ){
            if(i>0){
                value+=",";
            }
            value+= json[i][key];
        }
        return value;

    }

})
