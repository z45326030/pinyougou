app.controller("indexController",function ($scope,loginService) {//构建前端控制器

   $scope.findLoginName=function(){
       loginService.loginName().success(function (response) {
           $scope.loginName=response.loginName;
       })
   }


})
