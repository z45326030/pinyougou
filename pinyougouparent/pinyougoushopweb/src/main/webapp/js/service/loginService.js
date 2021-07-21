app.service("loginService",function ($http) {//构建前端服务层

    this.loginName=function () {
        return $http.get('../login/name.do');
    }

})