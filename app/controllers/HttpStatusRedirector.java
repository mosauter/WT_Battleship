package controllers;

import org.pac4j.core.context.HttpConstants;
import org.pac4j.play.PlayWebContext;
import org.pac4j.play.http.DefaultHttpActionAdapter;
import play.mvc.Result;
import views.html.home;

import static play.mvc.Results.forbidden;
import static play.mvc.Results.unauthorized;

public class HttpStatusRedirector extends DefaultHttpActionAdapter {

    @Override
    public Result adapt(int code, PlayWebContext context) {
        if (code == HttpConstants.UNAUTHORIZED) {
            return unauthorized(home.render("", "unauthorized")).as(HttpConstants.HTML_CONTENT_TYPE);
        } else if (code == HttpConstants.FORBIDDEN) {
            return forbidden(home.render("", "forbidden")).as(HttpConstants.HTML_CONTENT_TYPE);
        } else {
            return super.adapt(code, context);
        }
    }
}
