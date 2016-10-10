package com.redis.in.action.chapter01;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundZSetOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import com.redis.in.action.bean.Article;

/**
 * 0,文章 : map
 * 1,对文章投票 : map中的votes字段incr
 * 2,每个用户只能投一次 : 文章用户投票set
 * 3,文章按投票和时间排行 : 投票,时间 与 文章 set
 * 4,文章打标签
 */
@Component
@SuppressWarnings({"unchecked","rawtypes"})
public class VoteService {

	@Autowired
	RedisTemplate redisTemplate;

	/**
	 * 
	 * 添加文章
	 * @param user
	 * @param title
	 * @param link
	 * @return 文章的自增id
	 */
	public Long postArticle(String userId,String title, String link) {
		ValueOperations<String, Long> vo = redisTemplate.opsForValue();
		Long id = vo.increment(ArticleUtil.KEY_ARTICLE_ID_SEQ, 1L);
		//本人发布的文章，自动添加到已投票set中
		addHadVoteArticle(id, userId);

		Map<String,Object> articleData = new HashMap<String,Object>();
		articleData.put("title", title);
		articleData.put("link", link);
		articleData.put("user", userId);
		long nowInSecs = System.currentTimeMillis() / 1000;
		articleData.put("now", String.valueOf(nowInSecs));
		articleData.put("votes", "1"); //主要投票数的计数器
		
		String articleKey = ArticleUtil.formArticleKey(id);
		redisTemplate.opsForHash().putAll(articleKey, articleData);
		
		redisTemplate.boundZSetOps(ArticleUtil.KEY_SCORE).add(articleKey, nowInSecs+ArticleUtil.VOTE_SCORE);
		redisTemplate.boundZSetOps(ArticleUtil.KEY_TIME).add(articleKey, nowInSecs);
		
		return id;
	}
	
	public void voteArticle(String userId,Long id ) {
		String articleKey = ArticleUtil.formArticleKey(id);
		Double createTime = redisTemplate.boundZSetOps(ArticleUtil.KEY_TIME).score(articleKey);
		Long now = System.currentTimeMillis() / 1000;
		
		if((createTime + ArticleUtil.ONE_WEEK_IN_SECONDS) < now) 
			return;
		
		Long rs = addHadVoteArticle(id, userId);
		
		if(rs == 0) 
			return;
		
		redisTemplate.boundZSetOps(ArticleUtil.KEY_SCORE).incrementScore(articleKey, 432);
		redisTemplate.opsForHash().increment(articleKey, "votes", 1L);
	}
	
	public void tagArticle(Long id,String[] tags){
        String articleKey = ArticleUtil.formArticleKey(id);
        for(String tag:tags){
            String tagKey = ArticleUtil.formTagKey(tag);
            redisTemplate.opsForSet().add(tagKey,articleKey);
        }
    }
	
    public void delete(Long id){
        String articleKey = ArticleUtil.formArticleKey(id);
        redisTemplate.delete(articleKey);
        redisTemplate.boundZSetOps(ArticleUtil.KEY_SCORE).remove(articleKey);
        redisTemplate.boundZSetOps(ArticleUtil.KEY_TIME).remove(articleKey);
        String votedKey = ArticleUtil.formVoteKey(id);
        redisTemplate.delete(votedKey);
        //delete tag interset
    }
	
	public List<Article> getArticleRank(String zrangeKey, int page) {
		int start = (page - 1 ) * ArticleUtil.ARTICLES_PER_PAGE;
		int end = start +  ArticleUtil.ARTICLES_PER_PAGE -1;
		
		BoundZSetOperations<String, String> zsetop = redisTemplate.boundZSetOps(zrangeKey);
		Set<String> ids = zsetop.reverseRange(start, end);
		List<Article> list = ids.stream().map((id) -> {
			Map<String, Object> data = redisTemplate.opsForHash().entries(id);
			Article bean = new Article();
			bean.setId(Long.valueOf(id.substring(id.lastIndexOf(":") + 1)));
			
			try {
				BeanUtils.populate(bean, data);
			} catch (IllegalAccessException | InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return bean;
		}).collect(Collectors.toList());
		
		return list;
	}
	
	public List<Article> getArticleRankByTag(int page, String zrangkey, String tag) {
		String newZsetKey = ArticleUtil.formScoreTagKey(zrangkey, tag);
		Boolean had = redisTemplate.hasKey(newZsetKey);
		if(!had) {
			Long rs = redisTemplate.opsForZSet().intersectAndStore(ArticleUtil.formTagKey(tag), zrangkey, newZsetKey);
				if(rs == 1 ) {
					redisTemplate.expire(newZsetKey, 60, TimeUnit.SECONDS);
				}
		}
			
		return getArticleRank(newZsetKey, page);
	}
	
	public List<Article> getArticleRankByScore(int page) {
		return getArticleRank(ArticleUtil.KEY_SCORE, page);
	}
	
	public List<Article> getArticleRankByTime(int page) {
		return getArticleRank(ArticleUtil.KEY_TIME, page);
	}
	
	public Article getArticleById(Long id) {
		Map<String, Object> data = redisTemplate.opsForHash().entries(ArticleUtil.formArticleKey(id));
		Article article = new Article();
		article.setId(id);
		
		try {
			BeanUtils.populate(article, data);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		
		return article;
	}
	
	public Long addHadVoteArticle(long id, String userId) {
		String voteKey = ArticleUtil.formVoteKey(id);
		Long rs = redisTemplate.opsForSet().add(voteKey, userId);
		if(rs == 1 ) {
			redisTemplate.expire(voteKey, ArticleUtil.ARTICLE_USER_VOTE_EXPIRE_DAY, TimeUnit.DAYS);
		}
		return rs;
	}
}
