package com.btea.lexiflow.article.nlp;

import com.btea.lexiflow.vocab.dao.entity.BizVocabJpDO;
import com.btea.lexiflow.vocab.dao.mapper.BizVocabEnMapper;
import com.btea.lexiflow.vocab.dao.mapper.BizVocabJpMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/8/2
 * @Description: 日语文章词汇形态分析测试
 */
class ArticleVocabAnalyzerJapaneseTest {

    @Test
    void shouldMatchInflectedVerbsWithoutMatchingKanjiSubstringAsNoun() throws Exception {
        BizVocabJpMapper japaneseMapper = mock(BizVocabJpMapper.class);
        when(japaneseMapper.selectList(any())).thenReturn(List.of(
                vocab(2038L, "通"),
                vocab(3001L, "飛び込む"),
                vocab(3002L, "学ぶ")
        ));
        ArticleVocabAnalyzer analyzer = new ArticleVocabAnalyzer(mock(BizVocabEnMapper.class), japaneseMapper);
        String text = "今回の引っ越しを通して、新しい環境に飛び込むことの大切さを学びました。";

        List<ArticleVocabMatch> matches = analyzer.analyzeText(text, "ja", "N1");
        Map<String, ArticleVocabMatch> matchesByWord = matches.stream()
                .collect(Collectors.toMap(ArticleVocabMatch::getBaseWord, Function.identity()));

        assertFalse(matchesByWord.containsKey("通"));
        assertTrue(matchesByWord.containsKey("飛び込む"));
        assertTrue(matchesByWord.containsKey("学ぶ"));
        assertVerbOccurrence(text, matchesByWord.get("飛び込む").getOccurrences().getFirst(), "飛び込む");
        assertVerbOccurrence(text, matchesByWord.get("学ぶ").getOccurrences().getFirst(), "学び");
    }

    private void assertVerbOccurrence(String text, ArticleVocabOccurrence occurrence, String expectedText) {
        assertEquals("verb", occurrence.getPosType());
        assertEquals(expectedText, occurrence.getMatchedText());
        assertEquals(expectedText, text.substring(occurrence.getStartOffset(), occurrence.getEndOffset()));
        assertEquals("kuromoji-ipadic", occurrence.getAnalysisProvider());
    }

    private BizVocabJpDO vocab(long id, String word) {
        return BizVocabJpDO.builder()
                .id(id)
                .word(word)
                .level("N1")
                .build();
    }
}
