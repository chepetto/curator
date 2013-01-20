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
        util.jsonCall('GET', '/curator/rest/article/list/suggest', null, null, function(response) {

            var firstDate = util.strToDate(response.firstDate);

            var interval = 1000*60*60*24;
            var daysToArticlesMap =  $this._clusterList(interval, firstDate, response.list);

            for(var intervalIndex in daysToArticlesMap) {

                var cluster = $this._newCluster(intervalIndex, interval);
                //noinspection JSUnfilteredForInLoop
                var articles = daysToArticlesMap[intervalIndex];


//                <div class="article-group">
//
//                    <h3 class="date">Heute, 19. Januar 2013</h3>
//
//                    <div class="more"><a href="javascript:;">Show 3 more</a></div>
//
//                </div>


                for(var articleIndex in articles) {
                    //noinspection JSUnfilteredForInLoop
                    var article = articles[articleIndex];

//                    <div class="article">
//                        <div class="title"><a href="">Geiselnahme in Algerien zu Ende: Sicherheitskräfte stürmen Erdgasanlage</a></div>
//                        <div class="rating">
//                            <span>&#9733;</span>
//                            <span>&#9733;</span>
//                            <span>&#9733;</span>
//                            <span>&#9733;</span>
//                            <span>&#9734;</span>
//                        </div>
//                        <div class="abstract">Elf Terroristen und sieben Geiseln wurden getötet - Österreicher ausgeflogen und auf dem Weg in die Heimat</div>
//                        <div class="misc">on derstandard.at</div>
//                        <div style="clear: both"></div>
//                    </div>

                    var container = $('<div class="article"></div>');

                    var _title = $('<a></a>').attr('href', '/curator/rest/link/'+article.id).text(article.title).wrap('<div></div>');
                    var _rating = $this._getRating(article.voteCount, article.voteSum);
                    var _abstract = $('<div class="abstract"></div>').text(article.text);
                    var _misc = $('<div class="misc"></div>').text(article.text);

                    container
                        .append(_title)
                        .append(_rating)
                        .append(_abstract)
                        .append(_misc)
                        .append('<div style="clear: both"></div>');

                    cluster.append(container);

                }

                $this.clusters.append(cluster);
            }

        });

    },

    _getRating : function (voteCount, voteSum) {

        var rating = $('<div class="rating"></div>');
        // <div class="rating"></div>
        //   <span>&#9733;</span>
        //   <span>&#9733;</span>
        //   <span>&#9733;</span>
        //   <span>&#9733;</span>
        //   <span>&#9734;</span>
        // </div>

        var score = parseInt(voteSum/voteCount);
        for(var i=0; i<5; i++) {
            var star = '<span>&#9733;</span>';
            if(score<i) {
                star = '<span>&#9734;</span>';
            }
            rating.append(star);
        }

        return rating;
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

            var publishedTime = util.strToDate(article.date);

            var dayIndex = parseInt((firstTime-publishedTime)/interval);

            if(daysToArticlesMap[dayIndex] == null) {
                daysToArticlesMap[dayIndex] = [];
            }

            daysToArticlesMap[dayIndex].push(article);
        }

        return daysToArticlesMap;
    }

});
