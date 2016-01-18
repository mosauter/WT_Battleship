package controllers;

import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.context.WebContext;
import org.pac4j.play.http.DefaultHttpActionAdapter;
import views.html.home;

import static play.mvc.Results.*;

public class DemoHttpActionAdapter extends DefaultHttpActionAdapter {

    @Override
    public Object adapt(int code, WebContext context) {
        if (code == HttpConstants.UNAUTHORIZED) {
            return unauthorized(home.render("")).as((HttpConstants.HTML_CONTENT_TYPE));
        } else if (code == HttpConstants.FORBIDDEN) {
            return forbidden(home.render("")).as((HttpConstants.HTML_CONTENT_TYPE));
        } else {
            return super.adapt(code, context);
        }
    }
}
