<%@ page contentType="text/html; charset=UTF-8" language="java" import="java.sql.*" errorPage="" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>实时监控报警系统</title>
</head>

<body bgcolor="#6F86FF">
<table>
<tr height="150"><td width="2000" align="center" bgcolor="#0B0000" ><font color="#FFFF00"><font size="+3">实</font><font size="+">时</font><font size="+3">监</font><font size="+2">控</font><font size="+3">报</font><font size="+2">警</font><font size="+3"></font><font size="+2"></font><font size="+3">系</font><font size="+2">统</font></font>
</td>
</tr>
</table>



<table width="800" border="1" align="center" >
<tr bgcolor="#FFFFFF">
    <td >机组ID</td>
    <td>报警时间</td>
    <td>报警信息</td>
</tr>
<%
try
{
//驱动程序名
String driverName="com.mysql.jdbc.Driver";
//连接字符串

//加载驱动程序
Class.forName(driverName).newInstance();
Connection conn=DriverManager.getConnection("jdbc:mysql://172.17.11.174:3306/hive","root","root");
//DriverManager.getConnection(ConnStr)
//创建执行语句
String sql="select * from hive.info order by t desc";
Statement stmt=conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_READ_ONLY);
ResultSet rs=stmt.executeQuery(sql);
int intPageSize;      //一页显示的记录数
int intRowCount;      //记录的总数
int intPageCount;     //总页数
int intPage;         //待显示的页码
String strPage;
int i;
//设置一页显示的记录数
intPageSize=18;
//取得待显示的页码
strPage=request.getParameter("page");
//判断strPage是否等于null,如果是，显示第一页数据
if(strPage==null)
{
intPage=1;
}else{
//将字符串转换为整型
intPage=java.lang.Integer.parseInt(strPage);
}
if(intPage<1)
{
intPage=1;
}
//获取记录总数
rs.last();
intRowCount=rs.getRow();
//计算机总页数
intPageCount=(intRowCount+intPageSize-1)/intPageSize;
//调整待显示的页码
if(intPage>intPageCount) intPage=intPageCount;
if(intPageCount>0)
{
//将记录指针定位到待显示页的第一条记录上
rs.absolute((intPage-1)*intPageSize+1);
}
//下面用于显示数据
i=0;
while(i<intPageSize && !rs.isAfterLast())
{
%>
<tr>
    <td bgcolor="#FFFFFF"><%=rs.getString("id")%></td>
    <td bgcolor="#FFFFFF"><%=rs.getString("t")%></td>
    <td bgcolor="#FFFFFF"><%=rs.getString("infor")%></td>

</tr>
<%
rs.next();
i++;
}
//关闭连接、释放资源
rs.close();
stmt.close();
conn.close();
%>
</br></ br>
<p align="center">

<%

out.print("&nbsp;&nbsp;<a href='new.jsp?page="+(intPage-1)+"'>"+"["+"前一页"+"]"+"</a>"+"&nbsp;&nbsp;");
for(int j=1;j<=intPageCount;j++)
{

out.print("&nbsp;&nbsp;<a href='new.jsp?page="+j+"'>"+"["+j+"]"+"</a>");
}
out.print("&nbsp;&nbsp;<a href='new.jsp?page="+(intPage+1)+"'>"+"["+"后一页"+"]"+"</a>"+"&nbsp;&nbsp;");
%>

</p>
<%
}
catch(Exception e)
{
e.printStackTrace();
}
%>
</table>




<br /><br />

<table>
<tr  height="40"><td width="2000" align="center" bgcolor="#333333"><font color="#FFFFFF" size="-1" >COPYRIGHT &copy; 2016-2017&nbsp;&nbsp;&nbsp;&nbsp;实时报警监控系统 .燕江弟  版权所有&nbsp;&nbsp;&nbsp;&nbsp;禁止拷贝、复制，违者必究。</font>
</td>
</tr>
</table>
</body>
</html>