package org.curator.common.cache;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.curator.common.exceptions.CuratorException;
import org.curator.common.exceptions.CuratorStatus;
import org.infinispan.manager.EmbeddedCacheManager;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class CuratorCacheBean<T> {

    private static final Logger _log = Logger.getLogger(CuratorCacheBean.class);

    @Inject
    private EmbeddedCacheManager container;

    @SuppressWarnings("unchecked")
    public T get(String key, CacheName name) throws CuratorException {
        if (name == null) {
            String message = "Cache name is empty";
            _log.error(message);
            throw new CuratorException(CuratorStatus.CACHE_ERROR, message);
        }
        if (StringUtils.isEmpty(key)) {
            String message = "Cache key is empty";
            _log.error(message);
            throw new CuratorException(CuratorStatus.CACHE_ERROR, message);
        }
        return (T) container.getCache(name.toString()).get(key);
    }

    @SuppressWarnings("unchecked")
    public List<T> getAll(CacheName name) throws CuratorException {

        if (StringUtils.isEmpty(name.toString())) {
            String message = "Cache name is empty";
            _log.error(message);
            throw new CuratorException(CuratorStatus.CACHE_ERROR, message);
        }
        List<T> userSet = new ArrayList<T>();
        for (Object obj : container.getCache(name.toString()).values()) {
            userSet.add((T) obj);
        }
        return userSet;
    }


    public void put(String key, T obj, CacheName name) throws CuratorException {
        if (StringUtils.isEmpty(name.toString())) {
            String message = "Cache name is empty";
            _log.error(message);
            throw new CuratorException(CuratorStatus.CACHE_ERROR, message);
        }
        if (StringUtils.isEmpty(key)) {
            String message = "Cache key is empty";
            _log.error(message);
            throw new CuratorException(CuratorStatus.CACHE_ERROR, message);
        }
        container.getCache(name.toString()).put(key, obj);
    }

    public boolean contains(String key, CacheName name) throws CuratorException {
        if (StringUtils.isEmpty(name.toString())) {
            String message = "Cache name is empty";
            _log.error(message);
            throw new CuratorException(CuratorStatus.CACHE_ERROR, message);
        }
        if (StringUtils.isEmpty(key)) {
            String message = "Cache key is empty";
            _log.error(message);
            throw new CuratorException(CuratorStatus.CACHE_ERROR, message);
        }
        return container.getCache(name.toString()).containsKey(key);
    }
}
