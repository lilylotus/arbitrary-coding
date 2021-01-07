package cn.nihility.redis.service;

import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.List;
import java.util.Set;

public interface ZSetCacheService extends CommonCacheService {
    boolean add(String key, Object value, double score);

    Set<Object> range(String key, long start, long end);

    Set<Object> rangeByLex(String key, RedisZSetCommands.Range range);

    long zCard(String key);

    long count(String key, double min, double max);

    double incrementScore(String key, Object value, double delta);

    double score(String key, Object o);

    Set<Object> rangeByLex(String key, RedisZSetCommands.Range range, RedisZSetCommands.Limit limit);

    void add(String key, Set<ZSetOperations.TypedTuple<Object>> tuples);

    Set<Object> rangeByScore(String key, double min, double max);

    Set<Object> rangeByScore(String key, double min, double max, long offset, long count);

    Set<ZSetOperations.TypedTuple<Object>> rangeWithScores(String key, long start, long end);

    Set<ZSetOperations.TypedTuple<Object>> rangeByScoreWithScores(String key, double min, double max);

    Set<ZSetOperations.TypedTuple<Object>> rangeByScoreWithScores(String key, double min, double max, long offset, long count);

    long rank(String key, Object o);

    Cursor<ZSetOperations.TypedTuple<Object>> scan(String key, ScanOptions options);

    Set<Object> reverseRange(String key, long start, long end);

    Set<Object> reverseRangeByScore(String key, double min, double max);

    Set<Object> reverseRangeByScore(String key, double min, double max, long offset, long count);

    Set<ZSetOperations.TypedTuple<Object>> reverseRangeByScoreWithScores(String key, double min, double max);

    Set<ZSetOperations.TypedTuple<Object>> reverseRangeByScoreWithScores(String key, double min, double max, long offset, long count);

    Set<ZSetOperations.TypedTuple<Object>> reverseRangeWithScores(String key, long start, long end);

    long reverseRank(String key, Object o);

    long intersectAndStore(String key, String otherKey, String destKey);

    long intersectAndStore(String key, List<String> list, String destKey);

    long unionAndStore(String key, String otherKey, String destKey);

    long unionAndStore(String key, List<String> list, String destKey);

    long remove(String key, Object... values);

    long removeRangeByScore(String key, double min, double max);

    long removeRange(String key, long start, long end);
}