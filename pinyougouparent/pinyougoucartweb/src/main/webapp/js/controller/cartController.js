//首页控制器
app.controller('cartController',function($scope,$location,  cartService){

    //初始化
    $scope.init=function () {
        var itemId= $location.search()['itemId'];
        var num=$location.search()['num'];
        if(itemId!=null && num!=null ){
            $scope.addGoodsToCartList(itemId,num);
        }else{
            //$scope.cartList= cartService.getCartList();//显示本地购物车
            $scope.findCartList();//查询购物车
        }
    }

    //查询购物车
    $scope.findCartList=function () {
        cartService.findCartList( cartService.getCartList()  ).success(
            function (response) {
                if(response.success){//如果成功
                    $scope.cartList=response.data;//返回结果
                    if(response.loginname!=''){//如果登录，清除本地购物车
                        cartService.removeCartList();
                    }
                }
            }
        )
    }


    //合计
    $scope.$watch("cartList",function (newValue,oldValue) {
        $scope.totalValue=  cartService.sum(newValue);
    })


    //添加商品到购物车
    $scope.addGoodsToCartList=function (itemId,num) {
        cartService.addGoodsToCartList(  cartService.getCartList(), itemId,num ).success(
            function (response) {
                if(response.success){//如果成功
                    $scope.cartList=response.data;
                    if(response.loginname==""){
                        cartService.saveCartList(response.data);//保存购物车到本地
                    }else{
                        //合并
                        $scope.findCartList();
                    }
                }else{
                    alert(response.data);
                }
            }
        )
    }


    //获取地址列表
    $scope.findAddressList=function(){
        cartService.findAddressList().success(
            function(response){
                $scope.addressList=response;
                //默认地址处理
                for( var i=0;i< $scope.addressList.length;i++){
                    if($scope.addressList[i].isDefault=='1'){
                        $scope.address=$scope.addressList[i];
                        break;
                    }
                }
            }
        );
    }

   //选择地址
    $scope.selectAddress=function(address){
        $scope.address=address;
    }

    //判断当前的地址是不是选中的地址
    $scope.isSelectedAddress=function (address) {
        if($scope.address==address){
            return true;
        }else{
            return false;
        }
    }

    $scope.order={paymentType:"1"};//订单

    //选择支付方式
    $scope.selectPaymentType=function (paymentType) {
        $scope.order.paymentType=paymentType;
    }

    //提交订单
    $scope.submitOrder=function () {
        $scope.order.receiverAreaName=$scope.address.address;
        $scope.order.receiverMobile=$scope.address.mobile;
        $scope.order.receiver=$scope.address.contact;

        cartService.submitOrder($scope.order).success(
            function (response) {
                if(response.success){
                    if($scope.order.paymentType=='1'){
                        location.href="pay.html";
                    }else{
                        location.href="paysuccess.html";//货到付款
                    }
                }else{
                    alert(response.message);
                }
            }
        )

    }

});