package west2project.context;

import java.util.concurrent.TimeUnit;

public class Contexts {

    public static final TimeUnit REDIS_TIME_UNIT = TimeUnit.MINUTES;
    public static final String DEFAULT_AVATAR_URL = "D:\\dev\\west2File\\avatar\\默认头像.png";
    public static final String DEFAULT_AVATAR_BOX = "D:\\dev\\west2File\\avatar";
    public static final String VIDEO_BOX = "D:\\dev\\west2File\\video\\video";
    public static final String COVER_BOX = "D:\\dev\\west2File\\video\\cover";
    public static final Long REDIS_EMAIL_CODE_TTL = 15L;
    public static final Long CACHE_PUBLISH_VIDEO_LIST_TTL = 5L;
    public static final Long CACHE_FOLLOW_FAN_TTL = 5L;
    public static final Long CACHE_VIDEO_COMMENT_LIKE_TTL = 10L;
    public static final Long CACHE_USERNAME_TO_USERID_TTL = 5L;
    public static final Long CACHE_VIDEODO_TTL = 20L;
    public static final Long CACHE_COMMENTDO_TTL = 20L;
    public static final Long CACHE_COMMENT_LIST_TTL = 20L;
    public static final Long CACHE_POPULAR_TTL = 60L;
    public static final Long CACHE_USERDO_TTL = 20L;
    public static final String CACHE = "cache:";
    public static final String CACHE_COMMENT_LIST_VIDEO = "cache:comment:list:video:";
    public static final String CACHE_COMMENT_LIST_COMMENT = "cache:comment:list:comment:";
    public static final String CACHE_PUBLISH_VIDEO_LIST = "cache:video:publish_list:";
    public static final String CACHE_FOLLOW = "cache:follow:";
    public static final String CACHE_FRIEND = "cache:friend:";
    public static final String CACHE_FAN = "cache:fan:";
    public static final String CACHE_VIDEODO = "cache:video_do:";
    public static final String CACHE_COMMENTDO = "cache:comment_do:";
    public static final String CACHE_VIDEO_LIKE = "cache:video:like:";
    public static final String CACHE_COMMENT_LIKE = "cache:comment:like:";
    public static final String CACHE_USERDO = "cache:user_do:";
    public static final String CACHE_USERNAME_TO_USERID = "cache:user_name_id:";
    public static final String EMAIL_CODE = "email:code:";
    public static final String TASK = "task:";
}
