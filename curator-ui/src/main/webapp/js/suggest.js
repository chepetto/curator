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

        $this.element.append($this.clusters);

        util.jsonCall('GET', '/curator/rest/article/list/published', null, null, function(response) {

            var lastDate = util.strToDate(response.lastDate);

            var interval = 1000*60*60;
            var daysToArticlesMap =  $this._clusterList(interval, lastDate, response.list);

            for(var intervalIndex in daysToArticlesMap) {

                var cluster = $this._newCluster(intervalIndex, interval);
                //noinspection JSUnfilteredForInLoop
                var articles = daysToArticlesMap[intervalIndex];

                for(var articleIndex in articles) {
                    //noinspection JSUnfilteredForInLoop
                    var article = articles[articleIndex];
                    var title = $('<a></a>').attr('href', '/curator/rest/link/'+article.id).text(article.title);

                    var line = $('<div></div>').append(title);

                    cluster.append(line);

//                var _quality = Math.max(0, parseInt(article.quality * 100));
//                var _published = $this._newDateField(article.publishedTime).html();
                }

                $this.clusters.append(cluster);
            }

        });

    },

    _newCluster:function(index, interval) {
        var cluster = $('<div></div>');
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

    _clusterList:function(interval, lastDate, articleList) {
        var daysToArticlesMap = {};

        // cluster data by days
        for(var id in articleList) {
            //noinspection JSUnfilteredForInLoop
            var article = articleList[id];

            var publishedTime = util.strToDate(article.publishedTime);

            var dayIndex = parseInt((publishedTime-lastDate)/interval);

            if(daysToArticlesMap[dayIndex] == null) {
                daysToArticlesMap[dayIndex] = [];
            }

            daysToArticlesMap[dayIndex].push(article);
        }

        return daysToArticlesMap;
    }

});
