//$.widget("curator.suggest", $.curator.subscriptions, {
$.widget("curator.suggest", {

    options: {
        articles: null
    },

    _create:function() {

    },

    _init:function() {
        var $this = this;

        $this.element.empty();

        $this.clusters = $('<div></div>');
        var pagination = $('<div></div>');

        $this.element.append($this.clusters);
        $this.element.append(pagination);

        pagination.paginate({
            start 		: 1,
            count 		: 500,
            display  	: 5,
            rotate      : false,
            border_color			: '#fff',
            text_color  			: '#fff',
            background_color    	: 'black',
            border_hover_color		: '#ccc',
            text_hover_color  		: '#000',
            background_hover_color	: '#fff',
            images					: false,
            mouse					: 'press',
            onChange:function(element) {
                alert(element)
            }
        });
        util.jsonCall('GET', '/curator/rest/article/list/published', null, null, function(response) {

            var firstDate = util.strToDate(response.firstDate);

            var interval = 1000*60*60*24;
            var daysToArticlesMap =  $this._clusterList(interval, firstDate, response.list);

            for(var intervalIndex in daysToArticlesMap) {

                var cluster = $this._newCluster(intervalIndex, interval);
                //noinspection JSUnfilteredForInLoop
                var articles = daysToArticlesMap[intervalIndex];

                for(var articleIndex in articles) {
                    //noinspection JSUnfilteredForInLoop
                    var article = articles[articleIndex];
                    var title = $('<a></a>').attr('href', '/curator/rest/link/'+article.id).text(article.title);
                    var desc = $('<div style="overflow: hidden; height: 50px"></div>').text(article.text);

                    var line = $('<div></div>').append(title).append(desc);

                    cluster.append(line);

//                var _quality = Math.max(0, parseInt(article.quality * 100));
//                var _published = $this._newDateField(article.publishedTime).html();
                }

                $this.clusters.append(cluster);
            }

        });

    },

    _newCluster:function(index, interval) {
        var cluster = $('<div class="cluster"></div>');
        var t;
        switch(parseInt(index)) {
            case 0: t = 'Today'; break;
            case 1: t = 'Yesterday'; break;
            case 2: t = 'Two days ago'; break;
            default: t = $.timeago(new Date() - index*interval); break;
        }
        cluster.append($('<div></div>').text(t));
        return cluster;
    },

    _clusterList:function(interval, firstTime, articleList) {
        var daysToArticlesMap = {};

        // cluster data by days
        for(var id in articleList) {
            //noinspection JSUnfilteredForInLoop
            var article = articleList[id];

            var publishedTime = util.strToDate(article.publishedTime);

            var dayIndex = parseInt((firstTime-publishedTime)/interval);

            if(daysToArticlesMap[dayIndex] == null) {
                daysToArticlesMap[dayIndex] = [];
            }

            daysToArticlesMap[dayIndex].push(article);
        }

        return daysToArticlesMap;
    }

});
