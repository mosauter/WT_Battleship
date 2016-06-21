package controllers;

import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.authorization.authorizer.ProfileAuthorizer;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.profile.CommonProfile;

import java.util.List;

public class CustomAuthorizer extends ProfileAuthorizer<CommonProfile> {

    /*public boolean isAuthorized(WebContext context, UserProfile profile) {
        if (profile == null) {
            return false;
        }
        if (!(profile instanceof HttpProfile)) {
            return false;
        }
        final HttpProfile httpProfile = (HttpProfile) profile;
        final String username = httpProfile.getUsername();
        return StringUtils.startsWith(username, "jle");
    }*/

    @Override
    protected boolean isProfileAuthorized(WebContext context, CommonProfile profile) throws HttpAction {
        return profile != null && StringUtils.startsWith(profile.getUsername(), "jle");
    }

    @Override
    public boolean isAuthorized(WebContext context, List<CommonProfile> profiles) throws HttpAction {
        return isAnyAuthorized(context, profiles);
    }
}
