public String getCurrentUsername() {
    try {
        final CommonProfile profile=getUserProfile();
        return(String)profile.getAttribute(NAME_TAG);
    } catch(Exception e) {
        e.printStackTrace();
    }
    return null;
}

@RequiresAuthentication(clientName = "OidcClient")
public Result authenticate(String redirectUrl) {
    return redirect("/" + redirectUrl);
}
