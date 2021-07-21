 //控制层 
app.controller('goodsController' ,function($scope,$controller,$location ,goodsService,uploadService,itemCatService,typeTemplateService){
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(){
		var id= $location.search()["id"];//接受页面传递的参数
		goodsService.findOne(id).success(
			function(response){
				$scope.entity= response;
				//富文本值
				editor.html( $scope.entity.goodsDesc.introduction );
				//商品图片
                $scope.entity.goodsDesc.itemImages=  JSON.parse($scope.entity.goodsDesc.itemImages);
                //扩展属性
				$scope.entity.goodsDesc.customAttributeItems= JSON.parse( $scope.entity.goodsDesc.customAttributeItems );
				//规格
				$scope.entity.goodsDesc.specificationItems= JSON.parse( $scope.entity.goodsDesc.specificationItems );

				//SKU列表
				for(var i=0;i< $scope.entity.itemList.length ;i++ ){
                    $scope.entity.itemList[i].spec  = JSON.parse($scope.entity.itemList[i].spec);
				}

			}
		);				
	}
	
	//增加
	/*
	$scope.add=function(){

        $scope.entity.goodsDesc.introduction=editor.html();//商品介绍

        goodsService.add( $scope.entity  ).success(
			function(response){
                alert(response.message);
				if(response.success){
		        	$scope.entity={};
                    editor.html("");//清空富文本编辑器
				}
			}		
		);				
	}*/


    //保存
    $scope.save=function(){

        $scope.entity.goodsDesc.introduction=editor.html();//商品介绍
		var object;
		if($scope.entity.goods.id==null){
            object=goodsService.add( $scope.entity  );
		}else{
            object=goodsService.update( $scope.entity  );
		}
        object.success(
            function(response){
                alert(response.message);
                if(response.success){
                    $scope.entity={};
                    editor.html("");//清空富文本编辑器
					location.href="goods.html";//页面跳转
                }
            }
        );
    }
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		goodsService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
					$scope.selectIds=[];
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}

	$scope.image_entity={};//图片实体

	$scope.uploadImage=function () {
        uploadService.upload().success(function (response) {
			if(response.error==0){//成功
                $scope.image_entity.url =response.url;
			}
        })

    }

    $scope.entity={ goodsDesc:{itemImages:[],specificationItems:[] } };//商品实体

    //添加到图片列表
    $scope.add_image_entity=function(){
		$scope.entity.goodsDesc.itemImages.push( $scope.image_entity )
	}

	//删除图片
	$scope.remove_image_entity=function(index){
        $scope.entity.goodsDesc.itemImages.splice(index,1);
	}

	//查询商品一级分类列表
	$scope.selectItemCat1List=function(){
        itemCatService.findByParentId(0).success( function (response) {
			$scope.itemCat1List=response;
        })
	}

	//查询商品二级分类列表
	$scope.$watch( "entity.goods.category1Id",function (newValue,oldValue) {
        itemCatService.findByParentId(newValue).success( function (response) {
            $scope.itemCat2List=response;
        })
    })

    //查询商品三级分类列表
    $scope.$watch( "entity.goods.category2Id",function (newValue,oldValue) {
        itemCatService.findByParentId(newValue).success( function (response) {
            $scope.itemCat3List=response;
        })
    })

    //查询模板ID
    $scope.$watch( "entity.goods.category3Id",function (newValue,oldValue) {
        itemCatService.findOne( newValue ).success(function (response) {
            $scope.entity.goods.typeTemplateId = response.typeId;//模板ID
        })
    })

    //查询品牌列表
    $scope.$watch( "entity.goods.typeTemplateId",function (newValue,oldValue) {
        typeTemplateService.findOne( newValue ).success( function (response) {
			$scope.typeTemplate=response;
			$scope.typeTemplate.brandIds =  JSON.parse($scope.typeTemplate.brandIds  );//品牌列表由字符串转换为对象

			if($location.search()["id"]==null){ //不是修改
                $scope.entity.goodsDesc.customAttributeItems= JSON.parse($scope.typeTemplate.customAttributeItems);//扩展属性
			}

        })

        typeTemplateService.findSpecList( newValue ).success( function (response) {
			$scope.specList=  response;
        })

    })

	//更新选中的规格
	$scope.updateSpecItems=function ($event, name,value) {

		//思路：在集合中查询规格名称为某值的对象
        var object=  $scope.searchObjectByKey( $scope.entity.goodsDesc.specificationItems ,"name",name );
        if(object!=null ){ //有此规格
			if($event.target.checked){//如果是选中
                object.values.push(value);
			}else{//如果是取消选中
                object.values.splice( object.values.indexOf(value) ,1);
                if( object.values.length==0 ){
                    $scope.entity.goodsDesc.specificationItems.splice(
                    	$scope.entity.goodsDesc.specificationItems.indexOf(object) ,1  );
				}
			}

		}else{//没有此规格,添加规格记录
            $scope.entity.goodsDesc.specificationItems.push({name:name,values:[value] }  );
		}

    }


    $scope.createItemList=function(){

		$scope.entity.itemList=[ { spec:{},price:0,num:99999,status:"1",isDefault:"0" }];

		var items =  $scope.entity.goodsDesc.specificationItems;
		for(var i=0;i<items.length;i++){
            $scope.entity.itemList= addColumn( $scope.entity.itemList, items[i].name ,items[i].values);
		}

	}
	
	addColumn=function (list,name,values) {
		var newList=[];
    	for(var i=0;i<list.length;i++ ){
            var oldRow= list[i];
    		for(var j=0;j<values.length;j++  ){
				var newRow=  JSON.parse(JSON.stringify(oldRow))  ;   //深克隆
                newRow.spec[name]=values[j];
                newList.push(newRow);
			}
		}
		return newList;
    }

    $scope.status=["未审核","已审核","审核未通过","关闭"];//状态

	$scope.itemCatList=[];//商品分类列表  以商品分类ID作为下标  ，以商品分类名称作为值

	//查询商品分类列表
	$scope.findItemCatList=function () {
        itemCatService.findAll().success(function (response) {
            for(var i=0;i<response.length;i++){
                var itemCat= response[i];
                $scope.itemCatList[itemCat.id  ]= itemCat.name;
			}
        })
    }


    $scope.checkAttributeValue=function(specName,optionName){
        var specList=  $scope.entity.goodsDesc.specificationItems;//规格列表
		var spec= $scope.searchObjectByKey(specList,"name",specName );//查询规格
		if(spec==null){
			return false;
		}else{
            if( spec.values.indexOf(optionName  )>=0){
            	return true;
			}else{
            	return false;
			}
		}
		return true;
	}

    /**
	 * 上下架
     * @param marketabel
     */
	$scope.updateMarketabel=function (marketabel) {
		goodsService.updateMarketabel( $scope.selectIds , marketabel).success(function (response) {
			alert(response.message)	;
			if(response.success){
				$scope.reloadList();
				$scope.selectIds=[];
			}
        })
    }

});	
