app.controller('indexController',function ($scope,contentService) {

    $scope.contentList=[];

    $scope.findByContentCategoryId=function(categoryId){
        contentService.findByContentCategoryId(categoryId).success( function (response) {
            $scope.contentList[categoryId]= response;
        })
    }

    //搜索
    $scope.search=function () {
        var keywords=$scope.keywords;//获取关键字
        location.href="http://localhost:9104/search.html#?keywords="+keywords;
        //location.href="http://search.pinyougou.com/search.html#?keywords="+keywords;
    }

})