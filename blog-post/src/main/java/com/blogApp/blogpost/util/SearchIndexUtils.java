package com.blogApp.blogpost.util;

import com.blogApp.blogpost.model.Post;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.massindexing.MassIndexer;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.hibernate.Session;

import jakarta.persistence.EntityManager;

/**
 * Tiện ích để quản lý chỉ mục tìm kiếm Hibernate Search
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SearchIndexUtils {

    private final EntityManager entityManager;

    /**
     * Xây dựng lại toàn bộ chỉ mục tìm kiếm
     */
    @Transactional
    public void rebuildIndex() {
        try {
            log.info("Bắt đầu xây dựng lại chỉ mục tìm kiếm");
            Session session = entityManager.unwrap(Session.class);
            SearchSession searchSession = Search.session(session);

            MassIndexer indexer = searchSession.massIndexer(Post.class)
                    .threadsToLoadObjects(4);

            indexer.startAndWait();
            log.info("Hoàn thành xây dựng lại chỉ mục tìm kiếm");
        } catch (InterruptedException e) {
            log.error("Lỗi khi xây dựng lại chỉ mục tìm kiếm", e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Xây dựng lại chỉ mục tìm kiếm cho một entity cụ thể
     * @param entityClass Lớp entity cần xây dựng lại chỉ mục
     */
    @Transactional
    public <T> void rebuildIndexForEntity(Class<T> entityClass) {
        try {
            log.info("Bắt đầu xây dựng lại chỉ mục tìm kiếm cho {}", entityClass.getSimpleName());
            Session session = entityManager.unwrap(Session.class);
            SearchSession searchSession = Search.session(session);

            MassIndexer indexer = searchSession.massIndexer(entityClass)
                    .threadsToLoadObjects(4);

            indexer.startAndWait();
            log.info("Hoàn thành xây dựng lại chỉ mục tìm kiếm cho {}", entityClass.getSimpleName());
        } catch (InterruptedException e) {
            log.error("Lỗi khi xây dựng lại chỉ mục tìm kiếm cho {}", entityClass.getSimpleName(), e);
            Thread.currentThread().interrupt();
        }
    }
}