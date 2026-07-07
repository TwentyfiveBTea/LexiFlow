package com.btea.lexiflow.article.nlp;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.btea.lexiflow.common.convention.errorcode.BaseErrorCode;
import com.btea.lexiflow.common.convention.exception.ClientException;
import com.btea.lexiflow.vocab.dao.entity.BizVocabEnDO;
import com.btea.lexiflow.vocab.dao.entity.BizVocabJpDO;
import com.btea.lexiflow.vocab.dao.mapper.BizVocabEnMapper;
import com.btea.lexiflow.vocab.dao.mapper.BizVocabJpMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
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

    private final ObjectMapper objectMapper = new ObjectMapper();
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
        if ("ja".equals(languageCode)) {
            return analyzeJapaneseText(text, analysisLevel);
        }
        return analyzeEnglishText(text, analysisLevel);
    }

    private List<ArticleVocabMatch> analyzeEnglishText(String text, String analysisLevel) throws Exception {
        Set<String> levelWords = loadLevelWords("en", analysisLevel);
        Map<String, BizVocabEnDO> vocabMap = getEnglishVocabMap(levelWords);
        Map<Long, ArticleVocabMatch> matchMap = new LinkedHashMap<>();

        Annotation annotation = new Annotation(text);
        getEnglishPipeline().annotate(annotation);
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        if (sentences == null) {
            return List.of();
        }

        for (CoreMap sentence : sentences) {
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
                        .sentenceStartOffset(sentenceStart)
                        .sentenceEndOffset(sentenceEnd)
                        .startOffset(token.beginPosition())
                        .endOffset(token.endPosition())
                        .analysisProvider("corenlp")
                        .analysisVersion("4.5.7")
                        .build();
                addOccurrence(matchMap, vocab.getId(), vocab.getWord(), matchedText, occurrence);
            }
        }
        return toSortedMatches(matchMap);
    }

    private List<ArticleVocabMatch> analyzeJapaneseText(String text, String analysisLevel) throws Exception {
        Set<String> levelWords = loadLevelWords("ja", analysisLevel);
        Map<String, BizVocabJpDO> vocabMap = getJapaneseVocabMap(levelWords);
        Map<Long, ArticleVocabMatch> matchMap = new LinkedHashMap<>();
        for (BizVocabJpDO vocab : vocabMap.values()) {
            int fromIndex = 0;
            while (fromIndex < text.length()) {
                int start = text.indexOf(vocab.getWord(), fromIndex);
                if (start < 0) {
                    break;
                }
                int end = start + vocab.getWord().length();
                SentenceRange sentenceRange = findSentenceRange(text, start, end);
                ArticleVocabOccurrence occurrence = ArticleVocabOccurrence.builder()
                        .matchedText(vocab.getWord())
                        .normalizedText(vocab.getWord())
                        .posType("other")
                        .sentence(text.substring(sentenceRange.getStartOffset(), sentenceRange.getEndOffset()))
                        .sentenceStartOffset(sentenceRange.getStartOffset())
                        .sentenceEndOffset(sentenceRange.getEndOffset())
                        .startOffset(start)
                        .endOffset(end)
                        .analysisProvider("simple")
                        .analysisVersion("1.0")
                        .build();
                addOccurrence(matchMap, vocab.getId(), vocab.getWord(), vocab.getWord(), occurrence);
                fromIndex = end;
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

    private Map<String, BizVocabEnDO> getEnglishVocabMap(Set<String> words) {
        Map<String, BizVocabEnDO> result = new HashMap<>();
        for (List<String> batch : partition(new ArrayList<>(words))) {
            List<BizVocabEnDO> vocabList = bizVocabEnMapper.selectList(new LambdaQueryWrapper<BizVocabEnDO>()
                    .in(BizVocabEnDO::getWord, batch));
            for (BizVocabEnDO vocab : vocabList) {
                result.put(normalizeEnglish(vocab.getWord()), vocab);
            }
        }
        return result;
    }

    private Map<String, BizVocabJpDO> getJapaneseVocabMap(Set<String> words) {
        Map<String, BizVocabJpDO> result = new HashMap<>();
        for (List<String> batch : partition(new ArrayList<>(words))) {
            List<BizVocabJpDO> vocabList = bizVocabJpMapper.selectList(new LambdaQueryWrapper<BizVocabJpDO>()
                    .in(BizVocabJpDO::getWord, batch));
            for (BizVocabJpDO vocab : vocabList) {
                result.put(vocab.getWord(), vocab);
            }
        }
        return result;
    }

    private Set<String> loadLevelWords(String languageCode, String analysisLevel) throws Exception {
        Path dataDir = resolveDataDir(languageCode);
        String normalizedLevel = normalizeLevel(analysisLevel);
        if (!Files.exists(dataDir)) {
            throw new ClientException(BaseErrorCode.VOCAB_NOT_FOUND);
        }

        Set<String> words = new HashSet<>();
        try (var stream = Files.list(dataDir)) {
            List<Path> files = stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".json"))
                    .filter(path -> matchesLevelFile(path.getFileName().toString(), normalizedLevel, languageCode))
                    .collect(Collectors.toList());
            for (Path file : files) {
                JsonNode array = objectMapper.readTree(file.toFile());
                if (!array.isArray()) {
                    continue;
                }
                for (JsonNode node : array) {
                    JsonNode wordNode = node.get("word");
                    if (wordNode == null || wordNode.asText().isBlank()) {
                        continue;
                    }
                    String word = wordNode.asText();
                    words.add("ja".equals(languageCode) ? word : normalizeEnglish(word));
                }
            }
        }
        if (words.isEmpty()) {
            throw new ClientException(BaseErrorCode.VOCAB_NOT_FOUND);
        }
        return words;
    }

    private boolean matchesLevelFile(String filename, String normalizedLevel, String languageCode) {
        String lowerFilename = filename.toLowerCase(Locale.ROOT);
        String lowerLevel = normalizedLevel.toLowerCase(Locale.ROOT);
        if ("ja".equals(languageCode)) {
            return lowerFilename.contains("." + lowerLevel + ".");
        }
        return lowerFilename.startsWith(lowerLevel + "_");
    }

    private String normalizeLevel(String analysisLevel) {
        String level = analysisLevel.trim().toUpperCase(Locale.ROOT);
        return switch (level) {
            case "POSTGRADUATE", "KAOYAN" -> "KaoYan";
            default -> level;
        };
    }

    private Path resolveDataDir(String languageCode) {
        Path projectDirData = Paths.get("data", languageCode);
        if (Files.exists(projectDirData)) {
            return projectDirData;
        }
        Path backendDirData = Paths.get("..", "data", languageCode);
        if (Files.exists(backendDirData)) {
            return backendDirData;
        }
        return projectDirData;
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
}
