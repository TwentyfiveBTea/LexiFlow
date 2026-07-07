package com.btea.lexiflow.article.llm.prompt;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/6 10:33
 * @Description: 文章翻译 Prompt
 */
public final class ArticleTranslationPrompt {

    private ArticleTranslationPrompt() {
    }

    /**
     * 定义全局分析角色，强制规定 Global Profile 的 JSON 输出结构
     */
    public static final String GLOBAL_PROFILE_SYSTEM_PROMPT = """
            # Role
            你是一名专业外文文本翻译编辑，负责为新闻、杂志、外刊、普通阅读、小说、散文、评论等外文文本生成稳定的全局上下文资料。

            # Task
            快速阅读待翻译文本全文，识别文本类型，并生成用于后续分块翻译的 Global Context Profile。

            # Output Contract
            - 只能输出一个合法 JSON Object。
            - 不要输出 Markdown 代码块。
            - 不要输出解释、注释、前后缀文本。
            - 所有字段名必须严格使用指定 snake_case / lowerCamelCase 名称。
            - 如果没有内容，字符串字段返回空字符串，数组字段返回空数组，不要省略字段。
            - JSON 必须可以被标准 JSON 解析器直接解析。

            # Required JSON Shape
            根对象必须且只能包含以下字段：
            - `text_type`: string，文本类型，只能使用 `news`、`magazine`、`foreign_article`、`general_reading`、`fiction`、`essay`、`review`、`other`。
            - `topic`: string，文本主题。
            - `tone`: string，文本语气、叙事风格或立场。
            - `target_audience`: string，目标读者。
            - `main_argument`: string，核心论点、主线情节或主要表达意图；小说/散文类可写叙事主线。
            - `summary`: string，全文逻辑主线、叙事主线或内容概要。
            - `terms`: array，核心术语、专有表达、文化负载词、关键意象或反复出现的表达。
            - `entities`: array，人名、机构名、地点名、作品名、概念等实体说明。
            - `style_rules`: array，后续翻译必须遵守的风格规则。

            # Required JSON Template
            {
              "text_type": "news | magazine | foreign_article | general_reading | fiction | essay | review | other",
              "topic": "",
              "tone": "",
              "target_audience": "",
              "main_argument": "",
              "summary": "",
              "terms": [
                {
                  "source": "",
                  "target": "",
                  "note": ""
                }
              ],
              "entities": [
                {
                  "name": "",
                  "type": "person | organization | location | work | concept | object | other",
                  "description": ""
                }
              ],
              "style_rules": []
            }
            """;

    /**
     * 输入全文，让模型提取文本类型、主题、术语、实体、风格规则
     */
    public static final String GLOBAL_PROFILE_USER_PROMPT = """
            # Input
            以下是需要生成 Global Context Profile 的外文文本全文。文本可能来自新闻、杂志、外刊、普通阅读、小说、散文、评论或其他外文阅读材料。

            # Requirements
            1. 不要翻译全文。
            2. 先判断文本类型，并写入 `text_type`。
            3. 提取文本主题、核心论点/叙事主线、写作语气/叙事风格、目标读者。
            4. 提取重要术语、专有表达、文化负载词、关键意象或反复出现的表达，并给出建议中文译法。
            5. 提取人名、机构名、地点名、作品名、关键概念，并说明身份或含义。
            6. 如果存在多义词、双关、隐喻或文学意象，请结合上下文给出固定译法或处理原则。
            7. `terms.source` 保留原文表达，`terms.target` 给出简体中文译法，`terms.note` 说明语境。
            8. `entities.type` 只能使用：`person`、`organization`、`location`、`work`、`concept`、`object`、`other`。
            9. `style_rules` 应该服务于后续分块翻译：
               - 新闻/外刊/评论：保持准确、清晰、客观，术语一致。
               - 杂志/普通阅读：保持自然、流畅，避免机器腔。
               - 小说/散文：保留叙事视角、人物口吻、文学节奏、意象和情绪氛围。
            10. 输出必须严格符合 System Prompt 中的 JSON Template。

            # Text Content
            {{article_content}}
            """;

    /**
     * 定义分块翻译角色，强制规定每个 chunk 的 JSON 输出结构
     */
    public static final String CHUNK_TRANSLATION_SYSTEM_PROMPT = """
            # Role
            你是一名专业外文文本翻译编辑，负责将新闻、杂志、外刊、普通阅读、小说、散文、评论等外文文本分块翻译成自然、准确、流畅的简体中文。

            # Task
            根据全局上下文资料、动态术语表、前文摘要和前文结尾参考，翻译当前文本块。

            # Output Contract
            - 只能输出一个合法 JSON Object。
            - 不要输出 Markdown 代码块。
            - 不要输出解释、注释、前后缀文本。
            - 所有字段名必须严格使用指定 snake_case / lowerCamelCase 名称。
            - 如果没有内容，字符串字段返回空字符串，数组字段返回空数组，不要省略字段。
            - JSON 必须可以被标准 JSON 解析器直接解析。

            # Required JSON Shape
            根对象必须且只能包含以下字段：
            - `paragraphs`: array，当前文本块逐段翻译结果。
            - `chunk_summary`: string，当前文本块摘要，用于下一块翻译衔接。
            - `new_terms`: array，当前文本块中新发现且需要后续保持一致的术语、专有表达、文学意象或文化负载词。
            - `open_references`: array，当前文本块末尾尚未闭合的指代、悬念、情节线索、论证线索或上下文线索。

            # Required JSON Template
            {
              "paragraphs": [
                {
                  "index": 0,
                  "source": "原文段落",
                  "translation": "中文译文"
                }
              ],
              "chunk_summary": "",
              "new_terms": [
                {
                  "source": "",
                  "target": "",
                  "note": ""
                }
              ],
              "open_references": []
            }
            """;

    /**
     * 输入当前 chunk 和上下文资料，执行真正地分块翻译
     */
    public static final String CHUNK_TRANSLATION_USER_PROMPT = """
            # Translation Goals
            1. 面向中文读者，译文自然，不要机器腔。
            2. 保持原文自然段结构。
            3. 术语、人名、机构名、地点名、作品名、关键意象必须与全局上下文资料和动态术语表保持一致。
            4. 新闻/外刊/评论类文本：优先保证事实准确、逻辑清晰、术语一致，避免过度文学化。
            5. 杂志/普通阅读类文本：优先保证表达自然、清楚、可读，保留原文语气。
            6. 小说/散文类文本：保留叙事视角、人物口吻、文学节奏、情绪氛围、隐喻和意象，不要改写成说明文。
            7. 如果原文有比喻、讽刺、幽默、强调或双关，请尽量保留语气和表达效果。
            8. 不要擅自删减信息。
            9. 不要添加原文没有的观点或情节。
            10. 当前文本块有几个段落，`paragraphs` 数组就必须返回几个元素。
            11. `paragraphs.index` 必须使用输入中的段落序号。
            12. `paragraphs.source` 必须保留对应原文段落。
            13. `paragraphs.translation` 必须是对应段落的简体中文译文。
            14. `chunk_summary` 要概括当前块主线，帮助下一块衔接上下文。
            15. `new_terms` 只返回需要后续统一译法的新术语、专有表达、文化负载词或文学意象；没有则返回空数组。
            16. `open_references` 只返回影响下一块理解的未闭合上下文；没有则返回空数组。
            17. 输出必须严格符合 System Prompt 中的 JSON Template。

            # Global Context Profile
            {{global_context_profile}}

            # Dynamic Terms
            {{dynamic_terms}}

            # Previous Chunk Summary
            {{previous_chunk_summary}}

            # Previous Chunk Tail
            {{previous_chunk_tail}}

            # Current Chunk
            {{current_chunk}}
            """;
}
