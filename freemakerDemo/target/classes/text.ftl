<html>
<head>
    <meta charset="UTF-8">
    <title>我是一个模板</title>
</head>
<body>
<#include "head.ftl" />
<#--ass hole-->
${name},你好${message}.


<#assign linkman="周先生" />
${linkman}


<#assign info={"mobile":"13900012345","address":"吉大"} />
电话:${info.mobile},address:${info.address}

<#if success=true>
    你已通过实名认证
<#else>
    你未通过实名认证
</#if>

<br>
    商品价格表
<hr>
<#list goodsList as goods>
${goods_index+1}  名称：${goods.name}     价格：${goods.price}<br>
</#list>
<hr>
共${goodsList?size}条记录

<hr>

<#assign text="{'bank':'工商银行','account':'5498711346465789'}"/>
<#assign data=text?eval />


开户行：${data.bank}    账号:${data.account}<br>


<hr>
当前日期：${today?date}<br>
当前时间:${today?time}<br>
当前日期和时间：${today?datetime}<br>
日期格式化输出：${today?string("yyyy年MM月dd日")}

<hr>
当前积分${point?c}<br>

<#if aaa??>

aaa被定义：${aaa}
<#else>
aaa没有被定义
</#if>
<br>

bbb的值:${bbb!"0"}

</body>
</html>