<%@ page import="org.curator.common.bundle.ResourceBundle" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.io.File" %>
<%@ page import="java.io.FilenameFilter" %>
<%@ page import="java.io.FileInputStream" %>
<%@ page import="java.util.Scanner" %>
<%@ page import="java.io.FileFilter" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
    <head>
        <%-- Force IE8 Content Type --%>
        <meta http-equiv="X-UA-Compatible" content="IE=8">

        <%-- ############### CSS ############### --%>
        <link href="css/cupertino/jquery-ui-1.8.21.custom.css" rel="stylesheet" type="text/css">
        <link href="css/plugins/jquery.datepick.css" rel="stylesheet" type="text/css">

        <link href="css/migor.css" rel="stylesheet" type="text/css">
        <link href="css/page.event.css" rel="stylesheet" type="text/css">

        <%--Plugins--%>
        <script type="text/javascript" src="js/plugins/jquery-1.7.2.min.js"></script>
        <script type="text/javascript" src="js/plugins/jquery-ui-1.8.21.custom.min.js"></script>
        <script type="text/javascript" src="js/plugins/jquery.blockui.js"></script>
        <script type="text/javascript" src="js/plugins/jquery.json-2.2.js"></script>
        <script type="text/javascript" src="js/plugins/jquery.tooltip.js"></script>

        <script type="text/javascript" src="js/plugins/jquery.datepick.js"></script>
        <script type="text/javascript" src="js/plugins/jquery.tmpl.min.js"></script>
        <%--<script type="text/javascript" src="js/plugins/jquery.datepick.ext.js"></script>--%>

        <%--Project specific--%>
        <script type="text/javascript" src="js/urlEncode.js"></script>
        <script type="text/javascript" src="js/init.js"></script>
        <script type="text/javascript" src="js/utils.js"></script>


        <script type="text/javascript" src="js/widget-header.js"></script>

        <script type="text/javascript" src="js/turnover.js"></script>
        <script type="text/javascript" src="js/geo.js"></script>
        <script type="text/javascript" src="js/page-create-event.js"></script>
        <script type="text/javascript" src="js/comp-create-event.js"></script>
        <script type="text/javascript" src="js/page-show-event.js"></script>
        <script type="text/javascript" src="js/comp-show-event.js"></script>

        <script type="text/javascript">
            migor.bundle = {
                <% for (Map.Entry<String, String> entry : ResourceBundle.getEntries(request.getLocale(), "ui").entrySet()) { %>
                        "<%=entry.getKey()%>":"<%=entry.getValue()%>",<% } %>
            };
        </script>
<%
    // load templates
    File jsp = new File(request.getSession().getServletContext().getRealPath(request.getServletPath()));
    File dir = new File(jsp.getParentFile()+"/tmpl");
    for(File template: dir.listFiles(new FileFilter() {
        public boolean accept(File f) {
            return f.getName().toLowerCase().endsWith(".html");
        }
    })) {
%><script id="<%=template.getName().replaceAll(".html","")%>-tmpl" type="text/x-jquery-tmpl">
<%
            Scanner in = new Scanner(new FileInputStream(template));
            while(in.hasNextLine()) {
%><%=in.nextLine()%>
<% } %></script>
<% } %>
    </head>
<body>
    <div id="header" class="ui-widget-header ui-state-default ui-corner-all"></div>
    <div id="content">

    </div>
    <div id="footer"></div>
    <div id="dialogs"></div>
</body>
</html>