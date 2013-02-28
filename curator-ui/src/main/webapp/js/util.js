(function (curator) {

    curator.util = new function () {

        this.jsonCall = function (_type, url, urlReplacements, jsonObject, onSuccess, onError) {
            var $this = this;
            try {

                //noinspection JSUnusedLocalSymbols
                $.ajax({
                    type:_type,
                    url:$this.replaceInUrl(url, urlReplacements),
                    dataType:"json",
                    contentType:"application/json",
                    data:jsonObject,
                    cache:false,
                    processData:false,
                    success:function (data) {
                        switch (data.statusCode) {
                            case 0:
                                if (typeof(onSuccess) === 'function') {
                                    onSuccess.call(this, data.result);
                                }
                                break;
                            default:
                                alert('Error: ' + data.errorMessage);
                                if (typeof(onError) === 'function') {
                                    onError.call(this, data);
                                }
                                break;
                        }
                    },
                    error:function (data) {
                        if (typeof(onError) === 'function') {
                            onError.call(this);
                        }
                    }
                });
            } catch (err) {
                if (typeof(onError) === 'function') {
                    onError.call(this, err);
                }
            }
        };

        this.replaceInUrl = function (url, urlReplacements) {
            if (urlReplacements == null)
                return url;
            for (var key in urlReplacements) {
                var value = urlReplacements[key];
                if (value == null || value == "") {
                    url = url.replace(key, " ");
                }
                else {
                    //noinspection JSPotentiallyInvalidConstructorUsages
                    url = url.replace(key, encodeURI(value));
                }
            }
            return url;
        };

        this.strToDate = function (dateStr) {
            // 2012-08-23 22:27:15
            var arr = dateStr.replace(/:/g, ' ').replace(/-/g, ' ').split(' ');
            return new Date(arr[0], arr[1] - 1, arr[2], arr[3], arr[4], arr[5]);
        };

        this.sortArray = function (array) {
            var n = array.length;

            for (var i = 0; i < n; i++) {
                for (var j = n - 1; j > i; j--) {
                    if (array[j - 1] > array[j]) {
                        var o = array[j - 1];
                        array[j - 1] = array[j];
                        array[j] = o;
                    }
                }
            }
        };

        this.sortJSONArrayASC = function (array, key) {
            var n = array.length;

            for (var i = 0; i < n; i++) {
                for (var j = n - 1; j > i; j--) {
                    if (array[j - 1][key] > array[j][key]) {
                        var o = array[j - 1];
                        array[j - 1] = array[j];
                        array[j] = o;
                    }
                }
            }
        };

        this.sortJSONArrayDESC = function (array, key) {
            var n = array.length;

            for (var i = 0; i < n; i++) {
                for (var j = n - 1; j > i; j--) {
                    if (array[j - 1][key] < array[j][key]) {
                        var o = array[j - 1];
                        array[j - 1] = array[j];
                        array[j] = o;
                    }
                }
            }
        };

        this.dialogNewArticle = function () {
            var dialog = $('#dialog-add-article-template').clone().removeAttr('id').dialog({
                modal:true,
                resizable:false,
                draggable:false,
                closeOnEscape:true,
                sticky:true,
                onClose:function () {
                    $(this).dialog('destroy').remove();
                },
                width:700,
                buttons:{
                    'Publish':function () {

                        var title = dialog.find('.custom-title').val();
                        var url = dialog.find('.custom-link').val();
                        var text = dialog.find('.custom-text').val();

                        // todo validate

                        var article = {title:title, url:url, text:text};

                        curator.util.jsonCall('POST', '/curator/rest/article', null, JSON.stringify(article), function (response) {
                            // todo
                            noty({text:'Thanks for posting!', timeout:2000});
                        });
                    }
                }
            });
        };

        this.dialogNewFeed = function () {

            var dialog = $('#dialog-add-feed-template').clone().removeAttr('id').dialog({
                modal:true,
                resizable:false,
                draggable:false,
                closeOnEscape:true,
                sticky:true,
                onClose:function () {
                    $(this).dialog('destroy').remove();
                },
                width:700,
                buttons:{
                    'Add to database':function () {

                        var url = dialog.find('.url').val();

                        curator.util.jsonCall('POST', '/curator/rest/feed/create?url={url}', {'{url}':url}, null, function (response) {
                            if (response == true) {
                                noty({text:'New feed added.', timeout:2000});
                            }
                        });
                    }
                }
            });
        }
    }

})(curator);