<!--
/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/31
 * @Description: 服务条款与隐私政策页面
 */
-->
<script setup lang="ts">
import { ArrowLeft } from 'lucide-vue-next'
import { computed } from 'vue'
import BrandMark from '@/components/BrandMark.vue'
import { usePreferencesStore, type InterfaceLanguage } from '@/stores/preferences'

type LegalKind = 'terms' | 'privacy'

interface LegalSection {
  title: string
  paragraphs?: string[]
  bullets?: string[]
}

interface LegalDocument {
  title: string
  summary: string
  effectiveDate: string
  sections: LegalSection[]
}

interface LegalLocaleCopy {
  back: string
  version: string
  effective: string
  terms: string
  privacy: string
  documents: Record<LegalKind, LegalDocument>
}

const props = defineProps<{ kind: LegalKind }>()
const preferences = usePreferencesStore()

const legalCopy: Record<InterfaceLanguage, LegalLocaleCopy> = {
  'zh-CN': {
    back: '返回注册',
    version: '版本 1.0',
    effective: '生效日期',
    terms: '服务条款',
    privacy: '隐私政策',
    documents: {
      terms: {
        title: 'LexiFlow 服务条款',
        summary: '本条款说明您使用译流 LexiFlow 账号、文章精读、AI 处理、词汇学习、Credits 与支付服务时，双方各自的权利与责任。',
        effectiveDate: '2026年7月31日',
        sections: [
          {
            title: '一、条款的接受与适用',
            paragraphs: [
              '欢迎使用译流 LexiFlow。您注册账号、勾选同意或继续使用本服务，即表示您已经阅读、理解并同意受本条款及隐私政策约束。',
              '如您代表组织使用本服务，您应确认已经取得代表该组织接受本条款的有效授权。若您不同意本条款，请勿注册或继续使用服务。',
            ],
          },
          {
            title: '二、服务内容与账号管理',
            paragraphs: ['LexiFlow 提供外语文章上传与解析、段落翻译、词汇分析、词汇库管理、阅读与复习记录、Credits 计费及相关学习功能。具体功能可能随版本调整。'],
            bullets: [
              '您应提供真实、准确、有效的注册信息，并及时维护账号资料。',
              '您应妥善保管密码和登录凭证，不得出租、出借、转让或共享账号。',
              '发现账号被未经授权使用时，应立即修改密码并通过可用的项目联系方式通知我们。',
              '账号下发生的操作原则上视为账号持有人的行为，但法律另有规定或能够证明账号被盗用的除外。',
            ],
          },
          {
            title: '三、上传内容与知识产权',
            paragraphs: [
              '您保留对上传文章、文档及其他内容依法享有的权利。您仅授予 LexiFlow 为提供解析、翻译、OCR、存储、检索、词汇分析和展示功能所必需的、非独占且可撤销的处理许可。',
              '您应确保有权上传和处理相关内容，不得上传侵犯著作权、商业秘密、个人信息权益或其他合法权益的材料。我们不会因提供技术处理能力而取得您上传内容的所有权。',
            ],
          },
          {
            title: '四、AI 处理与学习结果',
            paragraphs: [
              '文章翻译、OCR、词汇释义、等级识别和其他分析结果可能由人工智能模型自动生成。受原文质量、语言差异和模型能力限制，结果可能存在遗漏、错误或不一致。',
              'AI 结果仅用于语言学习和阅读辅助，不构成法律、医疗、财务、学术诚信或其他专业意见。您应在发表、提交作业、作出专业判断或依赖相关结果前自行核验。',
            ],
          },
          {
            title: '五、Credits、费用与支付',
            bullets: [
              '部分文章处理功能会按照页面显示的规则消耗 Credits；实际扣减以服务端完成的计量记录为准。',
              '充值通过第三方支付机构完成。支付账号、银行卡等敏感支付凭据由支付机构处理，LexiFlow 仅接收订单号、金额和支付状态等必要信息。',
              '因重复支付、系统异常或法律规定需要退款的，请提供订单信息联系我们核查。已实际消耗的 Credits 原则上不予退还，法律另有规定的除外。',
              '我们调整计费项目或价格时，将在生效前通过页面提示等合理方式告知，调整不追溯影响已经完成的交易。',
            ],
          },
          {
            title: '六、使用规范',
            paragraphs: ['您不得利用本服务从事违法活动或干扰平台正常运行，包括但不限于：'],
            bullets: [
              '上传违法、有害、侵权、含恶意代码或未经授权收集的个人信息；',
              '绕过权限、计费、速率限制或安全措施，批量抓取、逆向攻击或探测系统漏洞；',
              '冒充他人、欺诈支付、滥用接口，或以明显超出正常个人学习范围的自动化方式占用资源；',
              '利用生成结果侵害他人权利、实施学术不端，或对外声称未经核验的 AI 内容必然准确。',
            ],
          },
          {
            title: '七、第三方服务',
            paragraphs: ['为实现对象存储、AI 处理和支付等功能，LexiFlow 可能接入第三方服务。第三方服务受其自身条款约束；我们会选择合理可靠的服务商并仅提供完成相应功能所必需的信息，但无法控制第三方独立运营的全部行为。'],
          },
          {
            title: '八、服务变更、中断与终止',
            paragraphs: [
              '我们会尽合理努力维持服务稳定，但维护升级、网络故障、供应商异常、不可抗力或安全事件可能造成暂时中断。对可预见的重大停机，我们将尽可能提前提示。',
              '如您严重违反本条款、危害系统安全或侵害他人权益，我们可以限制相关功能、暂停或终止账号，并在适用法律要求的范围内说明理由。您可以停止使用服务并请求注销账号。',
            ],
          },
          {
            title: '九、免责声明',
            paragraphs: ['在法律允许的范围内，本服务按“现状”和“可用”状态提供。我们不承诺服务永不中断，也不保证每项 AI 结果、词典信息或第三方内容完全准确。因用户上传内容的合法性、未核验 AI 结果或违反使用规范造成的后果，由相关责任方依法承担。'],
          },
          {
            title: '十、责任限制',
            paragraphs: ['对于因故意或重大过失、侵犯人身权益或法律不得限制的责任，我们依法承担责任。除此之外，在法律允许范围内，LexiFlow 对与某项付费服务直接相关的累计责任，以争议发生前十二个月内您为该项服务实际支付的金额为上限；免费服务的责任依适用法律确定。'],
          },
          {
            title: '十一、条款更新、法律适用与联系',
            paragraphs: [
              '我们可能因功能、法律或运营变化更新本条款。重大变更会通过页面提示等合理方式告知；变更生效后继续使用服务即表示接受更新内容，但依法需要另行同意的除外。',
              '本条款适用中华人民共和国法律。争议应先友好协商；协商不成的，任何一方可向服务运营者所在地有管辖权的人民法院提起诉讼。有关账号、计费、内容权利或条款的问题，可通过应用内可用的反馈渠道或项目维护者公开联系方式提出。',
            ],
          },
        ],
      },
      privacy: {
        title: 'LexiFlow 隐私政策',
        summary: '本政策说明译流 LexiFlow 在提供文章精读与词汇学习服务时收集哪些信息、如何使用与保护信息，以及您可以如何行使个人信息权利。',
        effectiveDate: '2026年7月31日',
        sections: [
          {
            title: '一、适用范围与处理原则',
            paragraphs: [
              '本政策适用于 LexiFlow 网页端及其提供的账号、文章处理、学习、Credits 和支付相关服务。我们遵循合法、正当、必要和诚信原则，仅处理实现明确目的所需的信息。',
              '第三方网站或支付页面由相应第三方独立运营，其个人信息处理活动适用其各自的隐私规则。',
            ],
          },
          {
            title: '二、我们收集的信息',
            bullets: [
              '账号信息：电子邮箱、用户名、头像及账号标识。密码仅以安全的摘要形式验证，我们不以明文保存密码。',
              '文章与学习数据：您上传的文件、解析文本、翻译结果、词汇命中、词汇库、掌握等级、复习记录和阅读进度。',
              'Credits 与订单信息：余额、冻结与消费记录、订单号、充值金额、支付渠道和支付状态；我们不直接保存银行卡号或支付密码。',
              '设备与日志信息：请求时间、IP 地址、浏览器类型、接口状态、错误日志及必要的安全审计信息。',
              '偏好与本地数据：界面语言、阅读字体、字号、高亮设置以及维持登录状态所需的本地存储数据。',
            ],
          },
          {
            title: '三、信息的使用目的',
            bullets: [
              '创建和管理账号，完成身份验证、登录和安全保护；',
              '解析、翻译、OCR 和分析文章，生成词汇及复习计划；',
              '保存学习进度、词汇库、界面偏好，并在不同页面提供连续体验；',
              '计算 Credits 消耗、创建支付订单、核验支付通知和展示交易记录；',
              '诊断故障、防止欺诈与滥用、保障系统和数据安全；',
              '履行法定义务、处理投诉并改进服务质量。',
            ],
          },
          {
            title: '四、Cookie 与本地存储',
            paragraphs: ['LexiFlow 使用浏览器本地存储保存登录令牌、界面语言和阅读偏好，以维持会话并提供一致体验。您可以通过浏览器清除这些数据，但清除后可能需要重新登录，部分偏好也会恢复默认值。'],
          },
          {
            title: '五、委托处理、共享与披露',
            paragraphs: ['我们不会出售您的个人信息。为完成具体功能，我们可能向下列服务商提供必要范围内的信息，并要求其依据约定处理和保护数据：'],
            bullets: [
              '云服务器、数据库、缓存、日志和对象存储服务商；',
              '提供翻译、文本分析或 OCR 能力的 AI 模型服务商，可能接收完成任务所需的文章片段或页面图像；',
              '处理充值和支付通知的第三方支付机构；',
              '依法有权要求披露信息的司法、行政或监管机关。',
            ],
          },
          {
            title: '六、跨境处理',
            paragraphs: ['我们优先按照适用法律选择和配置服务商。如某项 AI、对象存储或基础设施服务涉及向境外提供个人信息，我们将依法进行必要评估、告知接收方和处理目的，并在法律要求时取得您的单独同意或采取其他合规措施。'],
          },
          {
            title: '七、保存期限与删除',
            paragraphs: [
              '账号和学习数据通常在账号存续及提供服务所必需的期间保存。日志、安全记录和交易记录按照故障排查、反欺诈、财税及法律义务所需期限保存。超过必要期限后，我们会删除、匿名化或按照法律要求隔离保存。',
              '您删除文章或注销账号后，相关数据会从活动系统中删除；受备份周期、争议处理或法定义务影响，部分数据可能在有限期间内继续保留且不再用于日常业务。',
            ],
          },
          {
            title: '八、信息安全',
            paragraphs: ['我们采取访问控制、密码摘要、传输保护、权限隔离、日志审计、备份及缓存失效等合理措施保护信息。互联网服务不存在绝对安全；发生可能影响您权益的安全事件时，我们会依法采取补救措施并进行通知。'],
          },
          {
            title: '九、您的权利',
            paragraphs: ['在适用法律规定的范围内，您可以查询、更正或补充账号资料，删除文章和词汇数据，撤回基于同意的处理，获取个人信息副本，申请注销账号，或对自动化处理结果提出说明和异议。部分请求可能需要验证身份；法律要求保留的信息在法定期限届满前无法立即删除。'],
          },
          {
            title: '十、未成年人保护',
            paragraphs: ['未满十四周岁的未成年人应在监护人阅读并同意本政策后使用服务。若我们发现未经监护人同意收集了儿童个人信息，将依法尽快删除或采取其他保护措施。监护人可以通过项目公开联系方式提出查询或删除请求。'],
          },
          {
            title: '十一、政策更新与联系',
            paragraphs: [
              '我们可能因功能、法律或处理活动变化更新本政策。重大变更会通过页面提示等合理方式告知，并在依法需要时重新取得您的同意。',
              '如需行使个人信息权利，或对本政策、数据安全和隐私处理存在疑问，可通过应用内可用的反馈渠道或项目维护者公开联系方式联系我们。我们将在核验身份后于法律规定期限内处理。',
            ],
          },
        ],
      },
    },
  },
  en: {
    back: 'Back to registration',
    version: 'Version 1.0',
    effective: 'Effective',
    terms: 'Terms of Service',
    privacy: 'Privacy Policy',
    documents: {
      terms: {
        title: 'LexiFlow Terms of Service',
        summary: 'These Terms explain the rights and responsibilities that apply when you use LexiFlow accounts, deep-reading tools, AI processing, vocabulary learning, Credits, and payment services.',
        effectiveDate: 'July 31, 2026',
        sections: [
          { title: '1. Acceptance and scope', paragraphs: ['By registering, selecting the acceptance checkbox, or continuing to use LexiFlow, you confirm that you have read and agree to these Terms and the Privacy Policy. If you act for an organization, you confirm that you are authorized to bind it.'] },
          { title: '2. Services and accounts', paragraphs: ['LexiFlow provides document upload and parsing, translation, OCR, vocabulary analysis, collections, reading and review records, Credits, and related learning features. Features may change as the service develops.'], bullets: ['Keep registration details accurate and current.', 'Protect your password and access token; do not rent, transfer, sell, or share your account.', 'Notify us through an available project contact if you suspect unauthorized access.', 'Actions under an account are generally treated as the account holder’s actions unless applicable law or evidence shows unauthorized use.'] },
          { title: '3. Uploaded content and intellectual property', paragraphs: ['You retain the rights you lawfully hold in uploaded documents. You grant LexiFlow a non-exclusive, revocable license limited to the processing needed to parse, translate, store, retrieve, analyze, and display that content.', 'You must have the right to upload and process the content. Do not submit material that infringes copyright, trade secrets, privacy, personal information rights, or other lawful interests. LexiFlow does not acquire ownership merely by processing your content.'] },
          { title: '4. AI-generated results', paragraphs: ['Translations, OCR, definitions, vocabulary levels, and other analyses may be generated automatically and can contain omissions, errors, or inconsistencies.', 'AI output is for language learning and reading assistance. It is not legal, medical, financial, academic-integrity, or other professional advice. Verify results before publication, assessment, or any important decision.'] },
          { title: '5. Credits, fees, and payments', bullets: ['Some processing features consume Credits under the rules shown in the product; completed server-side metering records control.', 'Payments are handled by third-party payment providers. LexiFlow receives necessary order, amount, channel, and status data but does not directly store bank card numbers or payment passwords.', 'Contact us with the order details for duplicate payments, system errors, or legally required refunds. Credits already consumed are generally non-refundable unless the law requires otherwise.', 'Material price or billing changes will be announced reasonably in advance and will not retroactively change completed transactions.'] },
          { title: '6. Acceptable use', paragraphs: ['You may not use the service unlawfully or interfere with its operation.'], bullets: ['Do not upload illegal, harmful, infringing, malicious, or unlawfully collected personal data.', 'Do not bypass permissions, billing, rate limits, or security controls, scrape at scale, reverse attack, or probe vulnerabilities.', 'Do not impersonate others, commit payment fraud, abuse APIs, or use automation that unreasonably consumes shared resources.', 'Do not use generated output to infringe rights or commit academic misconduct, or present unverified AI output as guaranteed fact.'] },
          { title: '7. Third-party services', paragraphs: ['LexiFlow may rely on providers for storage, AI processing, and payments. Their own terms apply to their independent services. We select providers reasonably and disclose only what is necessary, but cannot control every aspect of their independent operations.'] },
          { title: '8. Changes, interruption, and termination', paragraphs: ['We use reasonable efforts to keep the service available, but maintenance, networks, suppliers, force majeure, or security events may interrupt it. We will provide advance notice of foreseeable material downtime where practical.', 'We may restrict, suspend, or terminate accounts that materially breach these Terms, threaten security, or infringe others. You may stop using the service and request account deletion.'] },
          { title: '9. Disclaimers', paragraphs: ['To the extent permitted by law, the service is provided “as is” and “as available.” We do not promise uninterrupted service or perfectly accurate AI, dictionary, or third-party content. Responsibility for unlawful uploads, unverified reliance, or misuse remains with the responsible party.'] },
          { title: '10. Limitation of liability', paragraphs: ['We remain responsible where liability cannot legally be limited, including intentional or grossly negligent conduct and unlawful harm to personal rights. Otherwise, liability directly related to a paid feature is limited, where permitted, to the amount you paid for that feature during the twelve months before the dispute.'] },
          { title: '11. Updates, governing law, and contact', paragraphs: ['We may update these Terms for product, legal, or operational changes. Material updates will be reasonably announced. Continued use after the effective date means acceptance unless separate consent is legally required.', 'These Terms are governed by the laws of the People’s Republic of China. Disputes should first be discussed in good faith and may then be brought before a competent court where the service operator is located. Contact us through an available in-product channel or the project maintainer’s published contact details.'] },
        ],
      },
      privacy: {
        title: 'LexiFlow Privacy Policy',
        summary: 'This Policy explains what information LexiFlow processes for deep reading and vocabulary learning, how it is used and protected, and how you can exercise your privacy rights.',
        effectiveDate: 'July 31, 2026',
        sections: [
          { title: '1. Scope and principles', paragraphs: ['This Policy applies to LexiFlow web services for accounts, article processing, learning, Credits, and payments. We process personal information lawfully, fairly, transparently, and only as necessary for stated purposes.', 'Third-party sites and payment pages operate independently and follow their own privacy rules.'] },
          { title: '2. Information we collect', bullets: ['Account data: email address, username, avatar, and account identifiers. Passwords are verified using secure hashes and are not stored in plain text.', 'Article and learning data: uploaded files, extracted text, translations, vocabulary matches, collections, mastery levels, reviews, and reading progress.', 'Credits and orders: balances, reservations, usage records, order numbers, recharge amounts, payment channels, and payment status. We do not directly store card numbers or payment passwords.', 'Device and logs: request time, IP address, browser type, API status, error logs, and security audit information.', 'Preferences and local data: interface language, reading font, size, highlighting, and browser storage needed to maintain sessions.'] },
          { title: '3. Why we use information', bullets: ['Create accounts, authenticate sessions, and protect account security.', 'Parse, translate, OCR, and analyze articles and generate vocabulary review plans.', 'Maintain learning progress, collections, and interface preferences.', 'Meter Credits, create orders, verify payment notifications, and show transaction records.', 'Diagnose faults, prevent fraud and abuse, secure the service, meet legal obligations, resolve complaints, and improve quality.'] },
          { title: '4. Cookies and local storage', paragraphs: ['LexiFlow uses browser storage for login tokens, interface language, and reading preferences. Clearing it may sign you out and reset some preferences.'] },
          { title: '5. Processors, sharing, and disclosure', paragraphs: ['We do not sell personal information. We may provide necessary data to contracted providers that support cloud hosting, databases, cache, logs, object storage, AI translation or OCR, and payments. AI providers may receive article excerpts or page images needed for the requested task. We may also disclose information when lawfully required by a competent authority.'] },
          { title: '6. Cross-border processing', paragraphs: ['We prioritize configurations that comply with applicable law. If an AI, storage, or infrastructure provider requires a cross-border transfer, we will complete required assessments, explain the recipient and purpose, and obtain separate consent or use another lawful safeguard where required.'] },
          { title: '7. Retention and deletion', paragraphs: ['Account and learning data are retained while the account exists and as needed to provide services. Logs, security records, and transaction records are kept for troubleshooting, anti-fraud, tax, accounting, and legal duties. Data is deleted, anonymized, or isolated after the necessary period.', 'After article deletion or account closure, data is removed from active systems. Limited copies may remain during backup cycles or where disputes and law require retention, without ordinary business use.'] },
          { title: '8. Security', paragraphs: ['We use reasonable measures such as access controls, password hashing, transmission protection, permission separation, audit logs, backups, and cache invalidation. No online system is absolutely secure. We will mitigate and notify affected users of qualifying security incidents as required by law.'] },
          { title: '9. Your rights', paragraphs: ['Subject to applicable law, you may access, correct, supplement, copy, or delete personal information; withdraw consent; delete articles and vocabulary data; close your account; and request explanations or object to automated processing. We may verify identity, and legally required records cannot be deleted before their retention period ends.'] },
          { title: '10. Children', paragraphs: ['Children under 14 should use the service only after a guardian has reviewed and accepted this Policy. If we learn that a child’s data was collected without appropriate guardian consent, we will delete it or take other legally required protective measures.'] },
          { title: '11. Updates and contact', paragraphs: ['We may update this Policy for product, legal, or processing changes. Material changes will be reasonably announced and renewed consent obtained where required.', 'To exercise privacy rights or ask about privacy and security, contact us through an available in-product channel or the project maintainer’s published contact details. We will verify identity and respond within the period required by law.'] },
        ],
      },
    },
  },
  ja: {
    back: '登録画面に戻る',
    version: 'バージョン 1.0',
    effective: '施行日',
    terms: '利用規約',
    privacy: 'プライバシーポリシー',
    documents: {
      terms: {
        title: 'LexiFlow 利用規約',
        summary: '本規約は、LexiFlow のアカウント、精読、AI 処理、語彙学習、Credits および決済サービスを利用する際の権利と責任を定めます。',
        effectiveDate: '2026年7月31日',
        sections: [
          { title: '1. 規約への同意と適用', paragraphs: ['アカウント登録、同意欄の選択、または継続利用により、本規約とプライバシーポリシーを読み、同意したものとみなされます。組織を代表して利用する場合は、その組織を拘束する正当な権限を有することを確認してください。'] },
          { title: '2. サービスとアカウント', paragraphs: ['LexiFlow は文書のアップロードと解析、翻訳、OCR、語彙分析、単語帳、読書・復習記録、Credits などの学習機能を提供します。機能はサービスの発展に伴い変更される場合があります。'], bullets: ['登録情報を正確かつ最新に保ってください。', 'パスワードと認証情報を適切に管理し、アカウントの貸与、譲渡、販売、共有を行わないでください。', '不正利用が疑われる場合は、直ちにパスワードを変更し、利用可能な連絡先から通知してください。', '法令または証拠により不正利用が確認される場合を除き、アカウント上の操作は原則として保有者の行為とみなされます。'] },
          { title: '3. アップロード内容と知的財産権', paragraphs: ['アップロードした文書に対して適法に有する権利は、引き続き利用者に帰属します。利用者は、解析、翻訳、保存、検索、語彙分析および表示に必要な範囲でのみ、LexiFlow に非独占的かつ撤回可能な処理権限を付与します。', '著作権、営業秘密、プライバシー、個人情報その他の権利を侵害する資料をアップロードしてはなりません。技術的処理によって LexiFlow が内容の所有権を取得することはありません。'] },
          { title: '4. AI による処理結果', paragraphs: ['翻訳、OCR、語義、語彙レベルなどは自動生成され、欠落、誤り、不一致を含む可能性があります。', 'AI の出力は語学学習と読解支援を目的とし、法律、医療、金融、学術倫理その他の専門的助言ではありません。公開、提出または重要な判断の前に必ず確認してください。'] },
          { title: '5. Credits、料金、決済', bullets: ['一部の処理機能は画面に表示されたルールに従って Credits を消費し、サーバー側で完了した計測記録を基準とします。', '決済は第三者の決済事業者が処理します。LexiFlow は注文番号、金額、決済手段、状態など必要な情報のみを受け取り、カード番号や決済パスワードを直接保存しません。', '重複決済、システム障害、法令上必要な返金については、注文情報を添えてお問い合わせください。使用済み Credits は、法令に別段の定めがある場合を除き返金されません。', '重要な料金変更は合理的な期間をもって通知し、完了済みの取引に遡及適用しません。'] },
          { title: '6. 禁止事項', paragraphs: ['違法行為またはサービス運営を妨げる目的で利用してはなりません。'], bullets: ['違法、有害、権利侵害、悪意あるコード、または不当に収集した個人情報をアップロードする行為。', '権限、課金、速度制限、セキュリティ対策の回避、大量収集、攻撃、脆弱性調査。', 'なりすまし、決済詐欺、API の濫用、通常の個人学習を著しく超える自動処理。', '生成結果による権利侵害や学術不正、未確認の AI 出力を確実な事実として表示する行為。'] },
          { title: '7. 第三者サービス', paragraphs: ['ストレージ、AI 処理、決済のため第三者サービスを利用する場合があります。第三者の独立サービスには各自の規約が適用されます。当方は合理的に事業者を選定し必要最小限の情報のみを提供しますが、第三者の全行為を管理することはできません。'] },
          { title: '8. 変更、中断、終了', paragraphs: ['安定運用に合理的な努力を払いますが、保守、ネットワーク、事業者障害、不可抗力、セキュリティ事象により中断する場合があります。予測可能な重大停止は可能な限り事前に通知します。', '重大な規約違反、セキュリティへの脅威、他者の権利侵害がある場合、機能制限、停止またはアカウント終了を行うことがあります。利用者は利用を停止し、アカウント削除を申請できます。'] },
          { title: '9. 免責', paragraphs: ['法令で認められる範囲で、本サービスは現状有姿かつ提供可能な状態で提供されます。中断がないこと、AI、辞書、第三者コンテンツが完全に正確であることを保証しません。違法なアップロード、未確認結果への依存、または不正利用の責任は、該当する責任者が負います。'] },
          { title: '10. 責任の制限', paragraphs: ['故意または重大な過失、人身権侵害その他法令上制限できない責任については、法令に従います。それ以外で有料機能に直接関連する責任は、法令で認められる範囲で、紛争前12か月に当該機能へ実際に支払った金額を上限とします。'] },
          { title: '11. 更新、準拠法、連絡先', paragraphs: ['製品、法令、運用上の変更に応じて本規約を更新する場合があります。重要な変更は合理的な方法で通知します。別途同意が必要な場合を除き、施行後の継続利用は更新への同意を意味します。', '本規約には中華人民共和国の法律が適用されます。紛争はまず誠実に協議し、解決しない場合はサービス運営者所在地の管轄裁判所に提起できます。アプリ内の利用可能な窓口または公開されたプロジェクト管理者の連絡先からお問い合わせください。'] },
        ],
      },
      privacy: {
        title: 'LexiFlow プライバシーポリシー',
        summary: '本ポリシーは、LexiFlow が精読と語彙学習のために取り扱う情報、その利用・保護方法、および利用者の権利について説明します。',
        effectiveDate: '2026年7月31日',
        sections: [
          { title: '1. 適用範囲と原則', paragraphs: ['本ポリシーは、アカウント、記事処理、学習、Credits、決済に関する LexiFlow のウェブサービスに適用されます。個人情報を適法、公正、必要最小限かつ明確な目的のために処理します。', '第三者サイトや決済ページは独立して運営され、それぞれのプライバシールールが適用されます。'] },
          { title: '2. 収集する情報', bullets: ['アカウント情報：メールアドレス、ユーザー名、画像、アカウント識別子。パスワードは安全なハッシュで検証し、平文では保存しません。', '記事・学習情報：アップロードファイル、抽出テキスト、翻訳、語彙分析、単語帳、習得度、復習記録、読書進捗。', 'Credits・注文情報：残高、保留、利用記録、注文番号、金額、決済手段、決済状態。カード番号や決済パスワードは直接保存しません。', '端末・ログ情報：リクエスト時刻、IP アドレス、ブラウザ種別、API 状態、エラーログ、セキュリティ監査情報。', '設定・ローカル情報：表示言語、読書フォント、文字サイズ、ハイライト、ログイン維持に必要なブラウザ保存情報。'] },
          { title: '3. 利用目的', bullets: ['アカウント作成、認証、ログイン、安全保護。', '記事の解析、翻訳、OCR、語彙分析と復習計画の作成。', '学習進捗、単語帳、表示設定の保存。', 'Credits の計測、注文作成、決済通知の確認、取引履歴の表示。', '障害診断、不正・濫用防止、セキュリティ確保、法的義務、苦情対応、品質改善。'] },
          { title: '4. Cookie とローカルストレージ', paragraphs: ['ログイントークン、表示言語、読書設定をブラウザのローカルストレージに保存します。削除すると再ログインが必要となり、一部設定が初期化される場合があります。'] },
          { title: '5. 委託、共有、開示', paragraphs: ['個人情報を販売しません。クラウド、データベース、キャッシュ、ログ、オブジェクトストレージ、AI 翻訳・OCR、決済を支援する委託先へ必要な範囲で情報を提供する場合があります。AI 事業者には依頼処理に必要な記事断片やページ画像が送信されることがあります。また、権限ある機関から適法な要求がある場合に開示することがあります。'] },
          { title: '6. 国外への移転', paragraphs: ['適用法に適合する構成を優先します。AI、ストレージ、基盤事業者の利用により国外移転が必要な場合、必要な評価、受領者と目的の説明、個別同意またはその他の法的保護措置を実施します。'] },
          { title: '7. 保存期間と削除', paragraphs: ['アカウントと学習データは、アカウント存続中およびサービス提供に必要な期間保存します。ログ、安全・取引記録は、障害対応、不正防止、会計・税務、法的義務に必要な期間保存し、その後削除、匿名化または隔離します。', '記事削除や退会後、活動中のシステムからデータを削除します。バックアップ周期、紛争、法的義務のため一部が限定期間残る場合がありますが、通常業務には利用しません。'] },
          { title: '8. セキュリティ', paragraphs: ['アクセス制御、パスワードハッシュ、通信保護、権限分離、監査ログ、バックアップ、キャッシュ無効化など合理的な措置を講じます。絶対的な安全は保証できませんが、権利に影響する事故には法令に従い対応・通知します。'] },
          { title: '9. 利用者の権利', paragraphs: ['適用法の範囲で、個人情報の閲覧、訂正、補充、複製、削除、同意撤回、記事・語彙データの削除、退会、自動処理への説明・異議申立てができます。本人確認を行う場合があり、法定保存情報は期間満了前に削除できません。'] },
          { title: '10. 子どもの情報', paragraphs: ['14歳未満の子どもは、保護者が本ポリシーを確認し同意したうえで利用してください。適切な保護者同意なく収集したことが判明した場合、速やかに削除するか法令上必要な保護措置を講じます。'] },
          { title: '11. 更新とお問い合わせ', paragraphs: ['製品、法令、処理内容の変更により本ポリシーを更新する場合があります。重要な変更は合理的に通知し、必要な場合は再同意を取得します。', '権利行使またはプライバシー・セキュリティに関する質問は、アプリ内の利用可能な窓口または公開されたプロジェクト管理者の連絡先からお寄せください。本人確認後、法定期間内に対応します。'] },
        ],
      },
    },
  },
}

const localeCopy = computed(() => legalCopy[preferences.interfaceLanguage])
const document = computed(() => localeCopy.value.documents[props.kind])
</script>

<template>
  <main class="legal-page">
    <nav class="legal-nav" aria-label="Legal document navigation">
      <BrandMark />
      <RouterLink class="back-link" to="/register"><ArrowLeft :size="16" />{{ localeCopy.back }}</RouterLink>
    </nav>

    <article class="legal-document surface fade-in">
      <header class="legal-header">
        <p class="eyebrow">{{ localeCopy.version }}</p>
        <h1>{{ document.title }}</h1>
        <p class="legal-summary">{{ document.summary }}</p>
        <p class="effective-date"><span>{{ localeCopy.effective }}</span><strong>{{ document.effectiveDate }}</strong></p>
      </header>

      <section v-for="section in document.sections" :key="section.title" class="legal-section">
        <h2>{{ section.title }}</h2>
        <p v-for="paragraph in section.paragraphs" :key="paragraph">{{ paragraph }}</p>
        <ul v-if="section.bullets">
          <li v-for="bullet in section.bullets" :key="bullet">{{ bullet }}</li>
        </ul>
      </section>
    </article>

    <footer class="legal-footer">
      <nav>
        <RouterLink to="/terms">{{ localeCopy.terms }}</RouterLink>
        <RouterLink to="/privacy">{{ localeCopy.privacy }}</RouterLink>
      </nav>
      <a href="https://beian.miit.gov.cn/" target="_blank" rel="noopener noreferrer">粤ICP备2025405156号-2</a>
    </footer>
  </main>
</template>

<style scoped>
.legal-page { min-height: 100vh; padding: 28px 24px 20px; background: var(--surface); }
.legal-nav { width: min(100%, 860px); min-height: 44px; display: flex; align-items: center; justify-content: space-between; gap: 24px; margin: 0 auto 24px; }
.back-link { display: inline-flex; align-items: center; gap: 7px; color: var(--ink-muted); font-size: 13px; font-weight: 650; }
.back-link:hover, .back-link:focus-visible { color: var(--primary); }
.legal-document { width: min(100%, 860px); padding: 52px clamp(28px, 7vw, 76px) 64px; margin-inline: auto; }
.legal-header { padding-bottom: 34px; margin-bottom: 34px; border-bottom: 1px solid var(--outline); }
.legal-header h1 { margin: 0; color: var(--primary); font-family: 'Literata', 'Songti SC', STSong, Georgia, serif; font-size: 38px; line-height: 1.24; font-weight: 650; }
.legal-summary { max-width: 700px; margin: 18px 0 0; color: var(--ink-muted); font-size: 15px; line-height: 1.8; }
.effective-date { display: flex; align-items: center; gap: 10px; margin: 22px 0 0; color: var(--ink-muted); font-size: 12px; }
.effective-date strong { color: var(--ink); font-weight: 650; }
.legal-section + .legal-section { margin-top: 34px; }
.legal-section h2 { margin: 0 0 13px; color: var(--primary); font-family: 'Literata', 'Songti SC', STSong, Georgia, serif; font-size: 20px; line-height: 1.45; font-weight: 650; }
.legal-section p, .legal-section li { color: var(--ink-muted); font-size: 14px; line-height: 1.9; }
.legal-section p { margin: 0; }
.legal-section p + p { margin-top: 10px; }
.legal-section ul { display: grid; gap: 7px; padding-left: 22px; margin: 12px 0 0; }
.legal-section li { padding-left: 4px; }
.legal-section li::marker { color: var(--secondary); }
.legal-footer { width: min(100%, 860px); display: flex; align-items: center; justify-content: space-between; gap: 24px; padding: 22px 4px 0; margin-inline: auto; color: var(--ink-muted); font-size: 11px; line-height: 18px; }
.legal-footer nav { display: flex; gap: 18px; }
.legal-footer a { text-underline-offset: 3px; }
.legal-footer a:hover, .legal-footer a:focus-visible { color: var(--primary); text-decoration: underline; }

@media (max-width: 640px) {
  .legal-page { padding: 18px 14px; }
  .legal-nav { margin-bottom: 16px; }
  .legal-document { padding: 34px 22px 44px; }
  .legal-header { padding-bottom: 26px; margin-bottom: 28px; }
  .legal-header h1 { font-size: 30px; }
  .legal-summary { font-size: 14px; }
  .legal-section + .legal-section { margin-top: 28px; }
  .legal-section h2 { font-size: 18px; }
  .legal-footer { align-items: flex-start; flex-direction: column; gap: 10px; }
}
</style>
