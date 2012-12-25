package org.curator.common.model;

@SuppressWarnings("serial")
public class Content extends Corpus {

    private String text;
    private String html;

    public Content() {
        // default
    }

    public Content(String content) {
        setText(content);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        text = text.replaceAll("[\n\t\r ]+", " ");
        super.setText(text);
        this.text = text;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

}
