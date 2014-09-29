package me.saghm.itstwittertime;

import twitter4j.Status;
import twitter4j.User;

class Tweet {
    private String user;
    private String text;
    //private String sourceURL;
    private long statusId;
    private long userId;

    public Tweet(Status status) {
    	user      = "@" + status.getUser().getScreenName();
	    text      = status.getText();
	    statusId  = status.getId();
        userId    = status.getUser().getId();
    }

    public String getUsername() { return user; }
    public String getText()     { return text; }
    public long getStatusId()   { return statusId; }
    public long getUserId()     { return userId; }

    @Override
    public String toString() {
	    return String.format("Tweet(user=\"%s\", text=[%s], id=%d)",
                user, text, getStatusId());
    }

//    private String getImageURLFromStatus(Status status) {
//        String[] words = status.toString().split("\\s+");
//        String found;
//
//        for (String word: words) {
//            if (!word.startsWith("mediaURL=")) continue;
//            found = word.replaceFirst("mediaURL=", "");
//            found = found.substring(0, found.length() - 1);
//
//            return found;
//        }
//
//        return "";
//    }
}
