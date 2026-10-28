package com.example.repository;

import com.example.entity.es.TopicDocument;
import jakarta.annotation.Resource;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TopicRepository extends ElasticsearchRepository<TopicDocument, Integer> {


}
