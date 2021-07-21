<%--
  Created by IntelliJ IDEA.
  User: Zhonghb
  Date: 2021/7/19
  Time: 19:53
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>一品优购</title>
</head>
<body>
    欢迎访问一品优购！
<%=request.getRemoteUser()%>

    <a href="http://localhost:9100/cas/logout?service=http://www.baidu.com">退出</a>
</body>
</html>
