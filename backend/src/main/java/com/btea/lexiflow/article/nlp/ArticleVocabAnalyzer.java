package com.btea.lexiflow.article.nlp;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.btea.lexiflow.common.convention.errorcode.BaseErrorCode;
import com.btea.lexiflow.common.convention.exception.ClientException;
import com.btea.lexiflow.article.nlp.ArticleSourceRangeUtil.SourceSegment;
import com.btea.lexiflow.vocab.dao.entity.BizVocabEnDO;
import com.btea.lexiflow.vocab.dao.entity.BizVocabJpDO;
import com.btea.lexiflow.vocab.dao.mapper.BizVocabEnMapper;
import com.btea.lexiflow.vocab.dao.mapper.BizVocabJpMapper;
import com.btea.lexiflow.vocab.util.VocabLevelUtil;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import static com.btea.lexiflow.article.constant.ArticleConstant.VOCAB_QUERY_BATCH_SIZE;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/5 01:24
 * @Description: 文章词汇分析器
 */
@Component
@RequiredArgsConstructor
public class ArticleVocabAnalyzer {

    private final BizVocabEnMapper bizVocabEnMapper;
    private final BizVocabJpMapper bizVocabJpMapper;

    private volatile StanfordCoreNLP englishPipeline;

    /**
     * 分析文章词汇
     *
     * @param text 文章纯文本
     * @param languageCode 语言标识
     * @param analysisLevel 词汇分析等级
     * @return 词汇命中结果
     */
    public List<ArticleVocabMatch> analyzeText(String text, String languageCode, String analysisLevel) throws Exception {
        return analyzeText(text, languageCode, analysisLevel, false);
    }

    /**
     * 分析文章原文范围内的词汇
     *
     * @param text 文章正文，可能同时包含原文和中文译文
     * @param languageCode 语言标识
     * @param analysisLevel 词汇分析等级
     * @param translated 是否按原文和译文交替结构存储
     * @return 词汇命中结果
     */
    public List<ArticleVocabMatch> analyzeText(String text, String languageCode, String analysisLevel,
                                               boolean translated) throws Exception {
        List<SourceSegment> sourceSegments = ArticleSourceRangeUtil.extract(text, translated);
        if ("ja".equals(languageCode)) {
            return analyzeJapaneseText(sourceSegments, analysisLevel);
        }
        return analyzeEnglishText(sourceSegments, analysisLevel);
    }

    private List<ArticleVocabMatch> analyzeEnglishText(List<SourceSegment> sourceSegments,
                                                       String analysisLevel) throws Exception {
        String databaseLevel = VocabLevelUtil.toDatabaseLevel("en", analysisLevel);
        Set<String> candidates = new LinkedHashSet<>();
        List<EnglishSegmentAnalysis> analyses = new ArrayList<>();
        for (SourceSegment sourceSegment : sourceSegments) {
            Annotation annotation = new Annotation(sourceSegment.text());
            getEnglishPipeline().annotate(annotation);
            List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
            if (sentences != null && !sentences.isEmpty()) {
                analyses.add(new EnglishSegmentAnalysis(sourceSegment, sentences));
                for (CoreMap sentence : sentences) {
                    for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                        String normalizedText = normalizeEnglish(token.word());
                        String lemma = normalizeEnglish(token.lemma());
                        if (!normalizedText.isEmpty()) {
                            candidates.add(normalizedText);
                        }
                        if (!lemma.isEmpty()) {
                            candidates.add(lemma);
                        }
                    }
                }
            }
        }
        if (candidates.isEmpty()) {
            return List.of();
        }
        Map<String, BizVocabEnDO> vocabMap = getEnglishVocabMap(candidates, databaseLevel);
        if (vocabMap.isEmpty() && !hasEnglishLevel(databaseLevel)) {
            throw new ClientException(BaseErrorCode.VOCAB_NOT_FOUND);
        }
        Map<Long, ArticleVocabMatch> matchMap = new LinkedHashMap<>();

        for (EnglishSegmentAnalysis analysis : analyses) {
            int baseOffset = analysis.sourceSegment().startOffset();
            for (CoreMap sentence : analysis.sentences()) {
                String sentenceText = sentence.get(CoreAnnotations.TextAnnotation.class);
                Integer sentenceStart = sentence.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
                Integer sentenceEnd = sentence.get(CoreAnnotations.CharacterOffsetEndAnnotation.class);
                for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                    String matchedText = token.word();
                    String normalizedText = normalizeEnglish(matchedText);
                    String lemma = normalizeEnglish(token.lemma());
                    BizVocabEnDO vocab = vocabMap.get(lemma);
                    if (vocab == null) {
                        vocab = vocabMap.get(normalizedText);
                    }
                    if (vocab == null) {
                        continue;
                    }
                    ArticleVocabOccurrence occurrence = ArticleVocabOccurrence.builder()
                            .matchedText(matchedText)
                            .normalizedText(lemma == null || lemma.isEmpty() ? normalizedText : lemma)
                            .posTag(token.tag())
                            .posType(convertEnglishPos(token.tag()))
                            .sentence(sentenceText)
                            .sentenceStartOffset(baseOffset + sentenceStart)
                            .sentenceEndOffset(baseOffset + sentenceEnd)
                            .startOffset(baseOffset + token.beginPosition())
                            .endOffset(baseOffset + token.endPosition())
                            .analysisProvider("corenlp")
                            .analysisVersion("4.5.7-source-v1")
                            .build();
                    addOccurrence(matchMap, vocab.getId(), vocab.getWord(), matchedText, occurrence);
                }
            }
        }
        return toSortedMatches(matchMap);
    }

    private List<ArticleVocabMatch> analyzeJapaneseText(List<SourceSegment> sourceSegments,
                                                        String analysisLevel) throws Exception {
        String databaseLevel = VocabLevelUtil.toDatabaseLevel("ja", analysisLevel);
        List<BizVocabJpDO> vocabList = bizVocabJpMapper.selectList(new LambdaQueryWrapper<BizVocabJpDO>()
                .eq(BizVocabJpDO::getLevel, databaseLevel));
        if (vocabList.isEmpty()) {
            throw new ClientException(BaseErrorCode.VOCAB_NOT_FOUND);
        }
        Map<Long, ArticleVocabMatch> matchMap = new LinkedHashMap<>();
        for (BizVocabJpDO vocab : vocabList) {
            for (SourceSegment sourceSegment : sourceSegments) {
                String sourceText = sourceSegment.text();
                int fromIndex = 0;
                while (fromIndex < sourceText.length()) {
                    int localStart = sourceText.indexOf(vocab.getWord(), fromIndex);
                    if (localStart < 0) {
                        break;
                    }
                    int localEnd = localStart + vocab.getWord().length();
                    int start = sourceSegment.startOffset() + localStart;
                    int end = sourceSegment.startOffset() + localEnd;
                    SentenceRange sentenceRange = findSentenceRange(sourceText, localStart, localEnd);
                    ArticleVocabOccurrence occurrence = ArticleVocabOccurrence.builder()
                            .matchedText(vocab.getWord())
                            .normalizedText(vocab.getWord())
                            .posType("other")
                            .sentence(sourceText.substring(sentenceRange.getStartOffset(), sentenceRange.getEndOffset()))
                            .sentenceStartOffset(sourceSegment.startOffset() + sentenceRange.getStartOffset())
                            .sentenceEndOffset(sourceSegment.startOffset() + sentenceRange.getEndOffset())
                            .startOffset(start)
                            .endOffset(end)
                            .analysisProvider("simple")
                            .analysisVersion("2.0-source-only")
                            .build();
                    addOccurrence(matchMap, vocab.getId(), vocab.getWord(), vocab.getWord(), occurrence);
                    fromIndex = localEnd;
                }
            }
        }
        return toSortedMatches(matchMap);
    }

    private void addOccurrence(Map<Long, ArticleVocabMatch> matchMap, Long wordId, String baseWord,
                               String matchedText, ArticleVocabOccurrence occurrence) {
        ArticleVocabMatch match = matchMap.computeIfAbsent(wordId, key -> ArticleVocabMatch.builder()
                .wordId(wordId)
                .baseWord(baseWord)
                .matchedForms(new LinkedHashSet<>())
                .occurrences(new ArrayList<>())
                .build());
        match.getMatchedForms().add(matchedText);
        match.getOccurrences().add(occurrence);
    }

    private List<ArticleVocabMatch> toSortedMatches(Map<Long, ArticleVocabMatch> matchMap) {
        return matchMap.values().stream()
                .filter(each -> !each.getOccurrences().isEmpty())
                .peek(each -> each.getOccurrences().sort(Comparator.comparing(ArticleVocabOccurrence::getStartOffset)))
                .sorted((left, right) -> Integer.compare(right.getOccurrences().size(), left.getOccurrences().size()))
                .collect(Collectors.toList());
    }

    private Map<String, BizVocabEnDO> getEnglishVocabMap(Set<String> words, String databaseLevel) {
        Map<String, BizVocabEnDO> result = new HashMap<>();
        for (List<String> batch : partition(new ArrayList<>(words))) {
            List<BizVocabEnDO> vocabList = bizVocabEnMapper.selectList(new LambdaQueryWrapper<BizVocabEnDO>()
                    .eq(BizVocabEnDO::getLevel, databaseLevel)
                    .in(BizVocabEnDO::getWord, batch));
            for (BizVocabEnDO vocab : vocabList) {
                result.put(normalizeEnglish(vocab.getWord()), vocab);
            }
        }
        return result;
    }

    private boolean hasEnglishLevel(String databaseLevel) {
        return bizVocabEnMapper.selectCount(new LambdaQueryWrapper<BizVocabEnDO>()
                .eq(BizVocabEnDO::getLevel, databaseLevel)) > 0;
    }

    private StanfordCoreNLP getEnglishPipeline() {
        if (englishPipeline == null) {
            synchronized (this) {
                if (englishPipeline == null) {
                    Properties props = new Properties();
                    props.setProperty("annotators", "tokenize,ssplit,pos,lemma");
                    englishPipeline = new StanfordCoreNLP(props);
                }
            }
        }
        return englishPipeline;
    }

    private String convertEnglishPos(String posTag) {
        if (posTag == null) {
            return "other";
        }
        if (posTag.startsWith("NN")) {
            return "noun";
        }
        if (posTag.startsWith("VB")) {
            return "verb";
        }
        if (posTag.startsWith("JJ")) {
            return "adj";
        }
        if (posTag.startsWith("RB")) {
            return "adv";
        }
        if (posTag.startsWith("PRP")) {
            return "pron";
        }
        if (posTag.startsWith("DT")) {
            return "det";
        }
        if (posTag.startsWith("IN")) {
            return "adp";
        }
        if (posTag.startsWith("CD")) {
            return "num";
        }
        if (posTag.startsWith("CC")) {
            return "conj";
        }
        return "other";
    }

    private SentenceRange findSentenceRange(String text, int start, int end) {
        int sentenceStart = start;
        while (sentenceStart > 0 && !isSentenceBoundary(text.charAt(sentenceStart - 1))) {
            sentenceStart--;
        }
        int sentenceEnd = end;
        while (sentenceEnd < text.length() && !isSentenceBoundary(text.charAt(sentenceEnd))) {
            sentenceEnd++;
        }
        if (sentenceEnd < text.length()) {
            sentenceEnd++;
        }
        return new SentenceRange(sentenceStart, sentenceEnd);
    }

    private boolean isSentenceBoundary(char ch) {
        return ch == '。' || ch == '！' || ch == '？' || ch == '.' || ch == '!' || ch == '?' || ch == '\n';
    }

    private String normalizeEnglish(String text) {
        if (text == null) {
            return "";
        }
        return text.toLowerCase(Locale.ROOT).replaceAll("^[^a-z]+|[^a-z]+$", "");
    }

    private <T> List<List<T>> partition(List<T> values) {
        List<List<T>> result = new ArrayList<>();
        for (int i = 0; i < values.size(); i += VOCAB_QUERY_BATCH_SIZE) {
            result.add(values.subList(i, Math.min(i + VOCAB_QUERY_BATCH_SIZE, values.size())));
        }
        return result;
    }

    @Data
    @AllArgsConstructor
    private static class SentenceRange {
        private Integer startOffset;
        private Integer endOffset;
    }

    private record EnglishSegmentAnalysis(SourceSegment sourceSegment, List<CoreMap> sentences) {
    }
}
