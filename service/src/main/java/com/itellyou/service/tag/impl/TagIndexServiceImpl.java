package com.itellyou.service.tag.impl;

import com.itellyou.model.tag.TagDetailModel;
import com.itellyou.model.tag.TagIndexModel;
import com.itellyou.service.common.impl.IndexServiceImpl;
import com.itellyou.service.tag.TagSearchService;
import com.itellyou.util.DateUtils;
import com.itellyou.util.StringUtils;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class TagIndexServiceImpl extends IndexServiceImpl<TagDetailModel> {

    private final TagSearchService searchService;

    public TagIndexServiceImpl( TagSearchService searchService){
        super("./.indexer/tag");
        this.searchService = searchService;
    }

    public List<Document> getDocument(List<Long> ids) throws IOException {
        IndexReader indexReader = getIndexReader();
        IndexSearcher searcher = new IndexSearcher(indexReader);
        List<Document> list = new ArrayList<>();
        TopDocs docs = searcher.search(LongPoint.newSetQuery("id",ids),ids.size());
        if(docs != null && docs.scoreDocs.length > 0) {
            for (ScoreDoc doc : docs.scoreDocs){
                list.add(searcher.doc(doc.doc));
            }
        }
        return list;
    }

    @Override
    public TagIndexModel getModel(Document document) {
        return new TagIndexModel(document);
    }

    @Override
    public Document getDocument(TagDetailModel detailModel) {
        Document doc = super.getDocument(detailModel);
        if(doc == null) return doc;

        String html = detailModel.getHtml();
        doc.add(new StoredField("type","tag"));
        doc.add(new LongPoint("group_id", detailModel.getGroupId()));
        doc.add(new StoredField("group_id",detailModel.getGroupId()));
        doc.add(new TextField("name", detailModel.getName(), Field.Store.YES));
        doc.add(new TextField("content", StringUtils.removeHtmlTags(html), Field.Store.YES));
        doc.add(new IntPoint("star_count",detailModel.getStarCount()));
        doc.add(new IntPoint("article_count",detailModel.getArticleCount()));
        doc.add(new IntPoint("question_count",detailModel.getQuestionCount()));
        doc.add(new LongPoint("created_time", DateUtils.getTimestamp(detailModel.getCreatedTime(),0l)));
        doc.add(new LongPoint("updated_time",DateUtils.getTimestamp(detailModel.getUpdatedTime(),0l)));
        doc.add(new LongPoint("created_user_id",detailModel.getAuthor().getId()));
        doc.add(new StoredField("created_user_id",detailModel.getAuthor().getId()));
        double score = 1.5;
        score += detailModel.getStarCount() / 100.0;
        score += detailModel.getArticleCount() / 100.0;
        score += detailModel.getQuestionCount() / 100.0;
        doc.add(new DoubleDocValuesField("score",score));
        return doc;
    }

    @Override
    @Async
    public void createIndex(Long id) {
        TagDetailModel detailModel = searchService.getDetail(id);
        if(detailModel.isDisabled() || !detailModel.isPublished()) {
            delete(id);
            return;
        }
        create(detailModel);
    }

    @Override
    @Async
    public void updateIndex(Long id) {
        TagDetailModel detailModel = searchService.getDetail(id);
        if(detailModel.isDisabled() || !detailModel.isPublished()) {
            delete(id);
            return;
        }
        update(detailModel);
    }

    @Override
    @Async
    public void updateIndex(Collection<Long> ids) {
        List<TagDetailModel> list = searchService.search(ids,null,null,null,null,null,true,null,true,null,null,null,null,null,null,null,null,null,null,null,null);
        for (TagDetailModel detailModel : list){
            if(detailModel.isDisabled()) delete(detailModel.getId());
            else update(detailModel);
        }
    }
}
