//$.widget("curator.suggest", $.curator.subscriptions, {
$.widget("curator.suggest", {

    options: {
        articles: null
    },

    _create: function () {

    },

    _init: function () {
        var $this = this;

        $this.element.empty();

        $this.clusters = $('<div/>');
        var pagination = $('<div/>');

        $this.element.append($this.clusters);
        $this.element.append(pagination);

        pagination.paginate({
            start: 1,
            count: 500,
            display: 5,
            rotate: false,
            border_color: '#fff',
            text_color: '#fff',
            background_color: 'black',
            border_hover_color: '#ccc',
            text_hover_color: '#000',
            background_hover_color: '#fff',
            images: false,
            mouse: 'press',
            onChange: function (element) {
                alert(element)
            }
        });

        $this._retrieve('/curator/rest/article/list/published');
    },

    _retrieve: function (url) {

        var $this = this;

        curator.util.jsonCall('GET', url, null, null, function (response) {

            var firstDate = curator.util.strToDate(response.firstDate);

            var interval = 1000 * 60 * 60 * 24;
            var daysToArticlesMap = $this._clusterArticlesPerDay(interval, firstDate, response.list);

            for (var intervalIndex in daysToArticlesMap) {

                var cluster = $this._newCluster(intervalIndex, interval);

                var visible = $('<div/>');
                var visibleLimit = 10;
                var hidden = $('<div style="display:none"></div>');

                //noinspection JSUnfilteredForInLoop
                var articles = daysToArticlesMap[intervalIndex];

                // todo: sort articles by quality
                curator.util.sortJSONArrayDESC(articles, 'quality');

                var index = 0;
                for (var articleIndex in articles) {

                    index++;
                    //noinspection JSUnfilteredForInLoop
                    var article = articles[articleIndex];

                    // -- Construct Article - --------------------------------------------------------------------------
                    var container = $('<div/>', {class: 'article'});
                    var _title = $('<a/>', {
                        href: '/curator/rest/link/' + article.id,
                        text: article.customTitle
                    });
                    var _rating = $this._getRating(article);
                    var _abstract = $('<div/>', {
                        class: 'abstract',
                        html: article.customTextRendered
                    });
                    var _misc = $('<div/>', {class: 'misc'})
                        .append('on ' + $this._shortUrl(article.url))
                        .append('<br>')
                        .append($('<span/>', {text: parseInt(article.quality * 100)}));

                    container
                        .append($('<div/>', {class: 'title'}).append(_title))
                        .append(_rating)
                        .append(_abstract)
                        .append(_misc)
                        .append('<div style="clear: both"></div>');

                    if (index <= visibleLimit) {
                        visible.append(container);
                    } else {
                        hidden.append(container);
                    }
                }

                cluster.append(visible);
                if (index > visibleLimit) {
                    cluster.append($this._getToggleHiddenArticlesButton(index - visibleLimit, hidden));
                    cluster.append(hidden);
                }

                $this.clusters.append(cluster);
            }

        });
    },

    _getToggleHiddenArticlesButton: function (articlesCount, hiddenContainer) {

        var link = $('<a/>', {
            href: '#',
            text: 'Show ' + articlesCount + ' links'
        }).click(function () {
                hiddenContainer.show();
                $(this).hide();
            });

        return $('<div/>', {class: 'more'})
            .append(link);
    },

    _shortUrl: function (url) {
        return url
            .replace(/http:\/\//g, '')
            .replace(/www./g, '')
            .replace(/\/.*/g, '')
    },

    _getRating: function (article) {

        // doku see http://wbotelhos.com/raty/
        return $('<div class="rating" style="float:right"></div>')
            .raty({
                score: article.ratingsCount == 0 ? 0 : parseInt(article.ratingsSum / article.ratingsCount),
                noRatedMsg: 'anyone rated this product yet!',

                click: function (score, evt) {
                    var params = {'{articleId}': article.id, '{rating}': score};
                    curator.util.jsonCall('POST', '/curator/rest/article/vote/{articleId}?rating={rating}', params, null, function (response) {
                        noty({text: 'Thanks for voting!', timeout: 2000});
                    });
                }
            });
    },

    _newCluster: function (index, interval) {
        var cluster = $('<div class="cluster"></div>');
        var t;
        switch (parseInt(index)) {
            case 0:
                t = 'Today';
                break;
            case 1:
                t = 'Yesterday';
                break;
            case 2:
                t = 'Two days ago';
                break;
            default:
                t = $.timeago(new Date() - index * interval);
                break;
        }
        cluster.append($('<div/>').append($('<h3/>').text(t)));
        return cluster;
    },

    _clusterArticlesPerDay: function (interval, firstTime, articleList) {
        var daysToArticlesMap = {};

        // cluster data by days
        for (var id in articleList) {
            //noinspection JSUnfilteredForInLoop
            var article = articleList[id];

            var publishedTime = curator.util.strToDate(article.date);

            var dayIndex = parseInt((firstTime - publishedTime) / interval);

            if (daysToArticlesMap[dayIndex] == null) {
                daysToArticlesMap[dayIndex] = [];
            }

            daysToArticlesMap[dayIndex].push(article);
        }

        return daysToArticlesMap;
    }

});
