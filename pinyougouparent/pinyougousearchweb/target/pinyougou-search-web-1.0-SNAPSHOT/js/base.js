var app=angular.module("pinyougou",["infinite-scroll"]);

app.filter("trustAsHtml",["$sce",function ($sce) {
    return function (data) {
        return $sce.trustAsHtml(data);
    }
}])