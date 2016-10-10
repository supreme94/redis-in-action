package com.redis.in.action.chapter01;

public class ArticleUtil {
	/**
     * incr,获取文章的id
     */
    public static final String KEY_ARTICLE_ID_SEQ = "articleIdSeq";

    /**
     * 数据结构:SET
     * key -- voted:articleId
     * value -- user
     * 记录每篇文章的投票用户
     */
    public static final String KEY_VOTE_ARTICLE_PREFIX = "votedSet:";

    /**
     * 数据结构:map
     * key -- articleMap:id
     * value --  article map
     */
    public static final String KEY_ARTICLE_PREFIX = "articleMap:";

    /**
     * 数据结构:zset
     * key -- scoreZSet
     * value -- 得分 articleMap:id
     */
    public static final String KEY_SCORE = "scoreZSet";

    /**
     * 数据结构:zset
     * key -- timeZSet
     * value -- 得分 articleMap:id
     */
    public static final String KEY_TIME = "timeZSet";

    /**
     * 文章标签前缀
     * 数据结构 set
     * key -- tagSet:tag
     * value -- articleMap:id
     */
    public static final String KEY_TAG_PREFIX = "tagSet:";

    /**
     * 文章用户投票记录的过期时间
     */
    public static final int ARTICLE_USER_VOTE_EXPIRE_DAY = 7;

    /**
     * 文章用户投票记录的过期时间
     * 秒为单位
     */
    public static final int ONE_WEEK_IN_SECONDS = 7 * 86400;

    /**
     * 投票得分
     */
    public static final int VOTE_SCORE = 432;

    /**
     * 每页多少条记录
     */
    public static final int ARTICLES_PER_PAGE = 25;
    
    /**
     * 获取文章id
     * @param id
     * @return
     */
    public static String formArticleKey(Long id){
        return KEY_ARTICLE_PREFIX + id;
    }
    
    /**
     * 获取文章已投用户名单key
     * @param id
     * @return
     */
    public static String formVoteKey(Long id){
        return KEY_VOTE_ARTICLE_PREFIX + id;
    }

    public static String formTagKey(String tag){
        return KEY_TAG_PREFIX + tag;
    }

    public static String formScoreTagKey(String scorePrefix,String tag){
        return scorePrefix + ":" + tag;
    }
}
