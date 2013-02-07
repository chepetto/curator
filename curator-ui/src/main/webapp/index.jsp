<%@ page import="org.curator.common.bundle.ResourceBundle" %>
<%@ page import="java.util.Map" %>
<!DOCTYPE html>
<!--[if lt IE 7]>
<html class="no-js lt-ie9 lt-ie8 lt-ie7"> <![endif]-->
<!--[if IE 7]>
<html class="no-js lt-ie9 lt-ie8"> <![endif]-->
<!--[if IE 8]>
<html class="no-js lt-ie9"> <![endif]-->
<!--[if gt IE 8]><!-->
<html class="no-js"> <!--<![endif]-->
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <title></title>
    <meta name="description" content="">
    <meta name="viewport" content="width=device-width">

    <!-- Place favicon.ico and apple-touch-icon.png in the root directory -->

    <link rel="stylesheet" href="css/normalize.css">
    <link rel="stylesheet" href="css/main.css">
    <link rel="stylesheet" href="css/vendor/layout-default-latest.css">
    <link rel="stylesheet" href="css/vendor/jquery-ui-1.9.2.custom.css">
    <link rel="stylesheet" href="css/vendor/jquery.dataTables.css">

    <script src="js/vendor/modernizr-2.6.2.min.js"></script>

    <script src="js/vendor/jquery-1.8.2.min.js"></script>
    <script src="js/vendor/jquery.raty.js"></script>
    <script src="js/vendor/jquery-ui-1.9.2.custom.js"></script>
    <script src="js/vendor/jquery.dataTables.js"></script>
    <script src="js/vendor/jquery.timeago.js"></script>
    <script src="js/vendor/jquery.paginate.js"></script>

    <!-- notifications -->
    <script src="js/vendor/jquery.noty.js"></script>
    <script src="js/vendor/noty/layouts/top.js"></script>
    <script src="js/vendor/noty/themes/default.js"></script>

    <script src="js/plugins.js"></script>
    <script src="js/util.js"></script>
    <script src="js/review.js"></script>

    <script type="text/javascript">

        (function(curator) {
            curator.I18n = {
                <% for (Map.Entry<String, String> entry : ResourceBundle.getEntries(request.getLocale(), "ui").entrySet()) { %>
                "<%=entry.getKey()%>":"<%=entry.getValue()%>",<% } %>
            };
        })('curator');
    </script>

</head>
<body>

<!--[if lt IE 7]>
<p class="chromeframe">You are using an <strong>outdated</strong> browser. Please <a href="http://browsehappy.com/">upgrade
    your browser</a></p>
<![endif]-->

<div id="nav">
    <ul>
        <li><a href="#home">Hot</a></li>
        <li><a href="#news">Live</a></li>
        <li><a href="#contact">Contact</a></li>
        <li><a href="#about">About</a></li>
    </ul>
</div>
<div id="content">
    <!-- content generated -->
</div>
</body>
</html>