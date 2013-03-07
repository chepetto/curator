$.widget("curator.listview", {

    options: {
        url: null,
        class: null
    },

    _init: function () {
        var $this = this;

        if ($this.options.url == null) {
            throw 'url is null';
        }

        $this.element.empty()
            .addClass($this.options.class)
            .addClass('listview');
        $this._table();
    },

    _table: function () {

        var $this = this;

        var list = $('<div/>');

        $this.element.append(list);

        curator.util.jsonCall('GET', $this.options.url, null, null, function (response) {

            $.each(response.list, function (id, article) {

                var _title = $('<a/>', {href: '/curator/rest/link/' + article.id, text: article.title}).wrap('<div>').parent().html();
                var _source = article.url.replace('http://', '').replace('https://', '').replace('www.', '').replace(/\/.*/g, '');
                var _quality = Math.max(0, parseInt(article.quality * 100));

                var _dateField;
                //if (_published) {
                //    _dateField = $this._newDateField(article.featuredTime);
                //} else {
                _dateField = $this._newDateField(article.date);
                //}

                var e_title = $('<div class="title" style="float: left;"/>').append(_title);
                var e_source = $('<div class="source" style="float: left;"/>').append('on ' + _source);
                var e_head = $('<div/>')
                        .append(e_title)
                        .append(e_source)
                    ;

                var e_abstract = $('<div/>', {class:'abstract', text:article.text});

                var e_time = $('<div class="time"/>').append(_dateField);
                var e_views = $('<div/>', {class:'views', text:article.views});
                var e_votes = $('<div class="votes"/>').append($this._getRating(article));
                //var e_tags = $('<div/>', {class:'tags', text:'Tag'});
                var e_stats = $('<div class="stats"/>')
                        .append(e_views)
                        .append('<div style="clear: both;"/>')
                        .append(e_time)
                        .append(e_votes)
                //.append(e_tags)
                    ;

//                var e_permalink = $('<a/>', {text: 'Permalink', href: '/id/' + article.id}).click(function () {
//                    alert(id)
//                });
                var e_menu = $('<div class="menu"/>')
//                        .append(e_permalink)
                    ;

                var e_article = $('<div class="article"/>')
                        .append(e_head)
                        .append('<div style="clear: both;"/>')
                        .append(e_stats)
                        .append(e_abstract)
                        .append('<div style="clear: both;"/>')
                        .append(e_menu)
                        .append('<div style="clear: both;"/>')
                    ;


                e_abstract.css('max-height', 18);
                e_abstract.click(function () {
                    // todo animate
                    e_abstract.css('max-height', '');
                });
                list.append(e_article);
            });

        });

    },

    _getRating: function (article) {
        var ratingsCount = article.ratingsCount;
        var ratingsSum = article.ratingsSum;

        var minStars = 1;
        var minScore = 10;
        var maxStars = 5;
        var maxScore = 40;

        var k = (minStars - maxStars) / (minScore - maxScore);
        var d = maxStars - maxScore * k;

        //var calcScore = parseInt(4 / 30 * 100 * article.quality - 1 / 3);
        var calcScore = parseInt(k * 100 * article.quality + d);

        return $('<div/>').raty({

            score: ratingsCount <= 5 ? calcScore : parseInt(ratingsSum / ratingsCount),
            noRatedMsg: 'anyone rated this product yet!',

            click: function (score, evt) {
                var params = {'{articleId}': article.id, '{rating}': score};
                curator.util.jsonCall('POST', '/curator/rest/article/vote/{articleId}?rating={rating}', params, null, function (response) {
                    noty({text: 'Thanks for voting!', timeout: 2000});
                });
            }
        });
    },

    _newDateField: function (dateStr) {
        var date = curator.util.strToDate(dateStr);
        return $('<div/>')
            .append($('<span/>', {text: date.getTime()}).hide())
            .append($('<span/>', {text: $.timeago(date)}));
    }

});