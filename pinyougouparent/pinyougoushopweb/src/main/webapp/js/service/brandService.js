app.service("brandService",function ($http) {//构建前端服务层

    this.findAll=function () {
        return $http.get('../brand/findAll.do');
    }
    this.findPage=function(page,rows){
        return $http.get("../brand/findPage.do?page="+page+"&rows="+rows);
    }
    this.search=function (page,rows,searchEntity) {
        return $http.post("../brand/search.do?page="+page+"&rows="+rows,  searchEntity );
    }
    this.add=function(entity){
        return $http.post("../brand/add.do",entity);
    }
    this.update=function(entity){
        return $http.post("../brand/update.do",entity);
    }
    this.findOne=function (id) {
        return  $http.get("../brand/findOne.do?id="+id);
    }
    this.dele=function (ids) {
        return  $http.get("../brand/delete.do?ids="+ids);
    }
    this.selectOptionList=function(){
        return $http.get("../brand/selectOptionList.do");
    }
})