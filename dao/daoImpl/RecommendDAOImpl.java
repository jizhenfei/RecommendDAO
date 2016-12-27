package cn.edu.bjtu.weibo.daoimpl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.edu.bjtu.weibo.dao.RecommendDAO;
import redis.clients.jedis.Jedis;

public class RecommendDAOImpl implements RecommendDAO {
	int XxCommentmeMessageNumber = 0;
	Jedis client;

	public RecommendDAOImpl(){
		final Set<String> sentinels = new HashSet<String>();
		final GenericObjectPoolConfig config = new GenericObjectPoolConfig();
		config.setTestOnReturn(true);
		config.setTestOnBorrow(true);

		sentinels.add("121.42.193.80:26379");
		sentinels.add("121.42.193.80:26378");
		SentinelBasedJedisPoolWrapper pool = new SentinelBasedJedisPoolWrapper(config, 90000, null, 0, "mymaster", sentinels);

		client = pool.getResource();
		pool.returnResource(client);
		pool.destroy();
	}

	@Override
	public List<String> getWeiboLabels(String weiboId) {
		if (client.exists("Weibo:" + weiboId + ":id")) {
			String key = "Weibo:" + weiboId + ":labels-list";
			List<String> list = client.lrange(key, 0, -1);
			return list;
		}

		return null;
	}

	@Override
	public boolean setWeiboLabels(String weiboId, String[] labels) {
		String key = "Weibo:" + weiboId + ":labels-list";
		String[] l = labels;
		for (int i = 0; i < l.length; i++) {
			client.lpush(key, l[i]);
		}
		return true;
	}

	@Override
	public Map<String, Double> getUserLabel(String userId) {
		String key = "User:" + userId + ":labels";
		Map<String, Double> map = new HashMap<String, Double>();
		Set<String> keylist = client.hkeys(key);
		for (String str : keylist) {
			String val = client.hget(key, str);
			map.put(str, Double.parseDouble(val));
		}
		return map;
	}

	@Override
	public boolean setUserLabels(String userId, Map<String, Double> labels) {
		Map<String, String> map = new HashMap<String, String>();
		String key = "User:" + userId + ":labels";
		for (Map.Entry<String, Double> entry : labels.entrySet()) {
			map.put(entry.getKey(), entry.getValue().toString());
		}
		client.hmset(key, map);
		return true;
	}

	@Override
	public List<String> getRecommendWeiboList(String userId, int pageIndex, int numberPerPage) {
		String key = "User:" + userId + ":recommendweibo-list";
		List<String> list = client.lrange(key, 0, -1);
		List<String> subList = list.subList((pageIndex - 1) * numberPerPage, pageIndex * numberPerPage);
		return subList;
	}

	@Override
	public boolean setRecommendWeiboList(String userId, List<String> weiboIdList) {
		String key = "User:" + userId + ":recommendweibo-list";
		for (int i = 0; i < weiboIdList.size(); i++) {
			client.lpush(key, weiboIdList.get(i));
		}
		return true;
	}

	@Override
	public boolean deleteRecommendWeibo(String userId) {
		String key = "User:" + userId + ":recommendweibo-list";
		client.del(key);
		return true;
	}

}
