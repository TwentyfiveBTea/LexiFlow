import type { InterfaceLanguage } from '@/stores/preferences'

type Translation = Partial<Record<InterfaceLanguage, string>>

const translations: Record<string, Translation> = {}

function add(source: string, en: string, ja: string) {
  translations[source] = { 'zh-CN': source, en, ja }
}

// System copy is kept here so pages can remain focused on data and interaction.
[
  ['首页', 'Home', 'ホーム'], ['我的图书馆', 'Library', 'ライブラリ'], ['词汇库', 'Vocabulary', '単語帳'], ['单词复习', 'Review', '復習'], ['钱包', 'Wallet', 'ウォレット'], ['设置', 'Settings', '設定'],
  ['文章库', 'Article library', '記事ライブラリ'], ['词汇明细', 'Vocabulary details', '単語の詳細'], ['控制台', 'Dashboard', 'ダッシュボード'], ['精读', 'Reader', '精読'], ['开始复习', 'Start review', '復習を開始'],
  ['分类管理词汇资源，建立深度阅读与学术研究的知识枢纽', 'Organize vocabulary into focused collections for deep reading and academic work', '単語を整理し、精読と学術研究のための知識ハブをつくります'],
  ['整理所有阅读材料，回到任何一段仍值得推敲的文本', 'Organize every reading and return to the passages worth revisiting', 'すべての読書素材を整理し、読み返す価値のある文章へ戻れます'],
  ['按掌握度安排下一次记忆练习，持续巩固阅读中真正遇到的词', 'Schedule the next review by mastery and reinforce words you actually meet while reading', '習得度に合わせて復習し、読書で出会った語彙を着実に定着させます'],
  ['我的钱包', 'My wallet', 'ウォレット'], ['管理账户额度与充值记录', 'Manage credits and recharge history', 'クレジットとチャージ履歴を管理'],
  ['搜索词库', 'Search collections', '単語帳を検索'], ['搜索标题', 'Search titles', 'タイトルを検索'], ['搜索单词', 'Search words', '単語を検索'], ['全部语言', 'All languages', 'すべての言語'], ['英语', 'English', '英語'], ['日语', 'Japanese', '日本語'],
  ['按语言筛选词汇库', 'Filter collections by language', '言語で単語帳を絞り込む'], ['按语言筛选文章', 'Filter articles by language', '言語で記事を絞り込む'], ['视图切换', 'Switch view', '表示を切り替え'], ['网格视图', 'Grid view', 'グリッド表示'], ['列表视图', 'List view', 'リスト表示'],
  ['新建词库', 'New collection', '新しい単語帳'], ['创建新词库', 'Create a new collection', '新しい単語帳を作成'], ['建立英语或日语词汇库，整理专属学习资料', 'Build an English or Japanese collection for your study materials', '英語または日本語の単語帳を作り、学習素材を整理'], ['创建词汇库', 'Create collection', '単語帳を作成'], ['词汇库名称', 'Collection name', '単語帳名'], ['语言标识', 'Language', '言語'], ['词汇库描述', 'Description', '単語帳の説明'],
  ['词库详细信息', 'Collection details', '単語帳の詳細'], ['学习数据占比', 'Learning distribution', '学習データの内訳'], ['正常词条', 'Words', '単語数'], ['创建时间', 'Created', '作成日時'], ['更新时间', 'Updated', '更新日時'], ['暂无词汇库描述', 'No collection description', '単語帳の説明はありません'], ['完成', 'Done', '完了'], ['取消', 'Cancel', 'キャンセル'], ['创建', 'Create', '作成'],
  ['单词总数', 'Total words', '単語総数'], ['已掌握', 'Mastered', '習得済み'], ['未学习', 'New', '未学習'], ['学习中', 'Learning', '学習中'], ['待复习', 'Due for review', '復習待ち'], ['全部等级', 'All levels', 'すべてのレベル'], ['选择词汇等级', 'Choose vocabulary level', '語彙レベルを選択'], ['单词', 'Word', '単語'], ['释义与短语', 'Meaning & phrases', '意味とフレーズ'], ['发音', 'Pronunciation', '発音'], ['等级', 'Level', 'レベル'], ['加入时间', 'Added', '追加日時'], ['暂无释义', 'No meaning available', '意味はありません'], ['没有符合条件的单词', 'No matching words', '条件に一致する単語はありません'], ['返回词汇库', 'Back to vocabulary', '単語帳に戻る'], ['正在加载词条...', 'Loading words...', '単語を読み込み中…'], ['重新加载', 'Reload', '再読み込み'],
  ['文章主语言', 'Primary language', '記事の主言語'], ['词数统计', 'Word count', '単語数'], ['解析状态', 'Parsing status', '解析ステータス'], ['翻译状态', 'Translation status', '翻訳ステータス'], ['词汇分析状态', 'Vocabulary analysis status', '語彙分析ステータス'], ['解析完成时间', 'Parsed at', '解析完了日時'], ['翻译完成时间', 'Translated at', '翻訳完了日時'], ['最近分析完成时间', 'Analyzed at', '分析完了日時'], ['尚未完成', 'Not completed', '未完了'], ['待处理', 'Pending', '保留中'], ['处理中', 'Processing', '処理中'], ['已完成', 'Completed', '完了'], ['处理失败', 'Failed', '失敗'], ['未知状态', 'Unknown', '不明'], ['选择解析等级', 'Choose analysis level', '解析レベルを選択'], ['将按所选等级匹配文章中的目标词汇', 'Match target words in this article using the selected level', '選択したレベルで記事中の対象語を照合します'], ['词汇解析等级', 'Vocabulary analysis level', '語彙解析レベル'], ['解析', 'Analyze', '解析'], ['解析文章词汇', 'Analyze article vocabulary', '記事の語彙を解析'], ['解析完成', 'Analysis complete', '解析完了'], ['已经解析过该等级', 'This level has already been analyzed', 'このレベルは解析済みです'], ['确认解析', 'Analyze', '解析を確定'], ['解析中…', 'Analyzing…', '解析中…'], ['完成解析', 'analyzed', '解析済み'], ['复用', 'reused', '再利用'], ['没有找到文章', 'No articles found', '記事が見つかりません'], ['换一个关键词或语言筛选试试', 'Try another keyword or language filter', '別のキーワードや言語で試してください'],
  ['最近文章', 'Recent articles', '最近の記事'], ['查看图书馆', 'View library', 'ライブラリを見る'], ['上传 PDF、DOCX、TXT、Markdown 或 HTML 文件', 'Upload a PDF, DOCX, TXT, Markdown, or HTML file', 'PDF、DOCX、TXT、Markdown、HTMLをアップロード'], ['开始复习', 'Start review', '復習を開始'], ['今日待复习', 'Due today', '今日の復習'], ['示例数据', 'Demo data', 'サンプルデータ'], ['今天没有待复习单词', 'No words due today', '今日の復習はありません'], ['新的到期词条会在这里出现', 'New due words will appear here', '新しく復習期限になった単語がここに表示されます'], ['词条', 'Word', '単語'], ['释义', 'Meaning', '意味'], ['语言', 'Language', '言語'], ['暂无记录', 'No records', '記録はありません'], ['正在加载文章库...', 'Loading article library...', '記事ライブラリを読み込み中…'], ['正在加载词汇库...', 'Loading vocabulary...', '単語帳を読み込み中…'], ['正在加载待复习单词...', 'Loading words due for review...', '復習対象の単語を読み込み中…'], ['把今天需要复习的单词逐个复习一遍，保证记忆的连续性', 'Review today’s words one by one to keep your memory continuous', '今日の復習語彙を一つずつ復習し、記憶をつなげます'], ['复习单词加载失败，请稍后重试', 'Failed to load review words. Please try again.', '復習単語の読み込みに失敗しました。もう一度お試しください。'], ['提交复习结果失败，请重试', 'Failed to submit the review. Please try again.', '復習結果の送信に失敗しました。もう一度お試しください。'],
  ['交易记录', 'Transactions', '取引履歴'], ['Credits 使用记录', 'Credits usage', 'Credits 使用履歴'], ['可用额度', 'Available credits', '利用可能クレジット'], ['冻结额度', 'Frozen credits', '凍結クレジット'], ['账户正常', 'Account active', 'アカウントは有効'], ['账户冻结', 'Account frozen', 'アカウント凍結'], ['最后更新', 'Last updated', '最終更新'], ['自定义金额（1–100 元）', 'Custom amount (¥1–100)', '金額を指定（¥1〜100）'], ['输入金额', 'Enter amount', '金額を入力'], ['待支付金额', 'Amount due', '支払額'], ['付款方式', 'Payment method', '支払方法'], ['确认付款', 'Confirm payment', '支払いを確認'], ['查看您的付款详情', 'Review your payment details', '支払い内容を確認'], ['创建订单…', 'Creating order…', '注文を作成中…'], ['查看已完成支付并到账的充值订单', 'View completed recharge orders', '完了したチャージ注文を表示'], ['查看文章处理产生的 Credits 消耗', 'View Credits spent on article processing', '記事処理で使用したCreditsを表示'], ['完成充值后，交易会显示在这里', 'Transactions will appear here after a recharge', 'チャージ後に取引がここに表示されます'], ['完成文章处理后，Credits 消耗会显示在这里', 'Article processing Credits will appear here', '記事処理のCreditsがここに表示されます'], ['完成时间', 'Completed', '完了日時'], ['文章 ID', 'Article ID', '記事ID'], ['总消耗', 'Total', '合計'], ['上一页', 'Previous page', '前のページ'], ['下一页', 'Next page', '次のページ'],
  ['记录加载失败，请确认服务状态后重试', 'Failed to load records. Check the service and try again.', '記録の読み込みに失敗しました。サービスを確認して再試行してください'], ['请输入 1 至 100 元之间的有效金额', 'Enter a valid amount between ¥1 and ¥100', '¥1〜100の有効な金額を入力してください'], ['充值订单创建失败，请稍后重试', 'Failed to create the recharge order. Please try again.', 'チャージ注文の作成に失敗しました。もう一度お試しください。'], ['暂无记录', 'No records', '記録はありません'], ['添加资金', 'Add funds', '資金を追加'], ['选择金额和付款方式', 'Choose an amount and payment method', '金額と支払方法を選択'],
  ['登录', 'Sign in', 'ログイン'], ['注册', 'Create account', 'アカウント作成'], ['登录失败，请稍后重试', 'Sign-in failed. Please try again.', 'ログインに失敗しました。もう一度お試しください。'], ['注册失败，请稍后重试', 'Registration failed. Please try again.', '登録に失敗しました。もう一度お試しください。'], ['登录到工作区', 'Sign in to workspace', 'ワークスペースにログイン'], ['创建账号', 'Create an account', 'アカウントを作成'], ['还没有账号？', 'No account yet?', 'アカウントをお持ちでないですか？'], ['已有账号？', 'Already have an account?', 'アカウントをお持ちですか？'], ['直接登录', 'Sign in', 'ログイン'], ['电子邮件', 'Email', 'メールアドレス'], ['用户名', 'Username', 'ユーザー名'], ['确认密码', 'Confirm password', 'パスワードを確認'], ['再次输入密码', 'Enter password again', 'パスワードを再入力'], ['您所展示的昵称', 'The name shown to others', '表示名'], ['显示或隐藏密码', 'Show or hide password', 'パスワードを表示/非表示'], ['隐藏密码', 'Hide password', 'パスワードを隠す'], ['显示密码', 'Show password', 'パスワードを表示'], ['正在进入…', 'Signing in…', 'ログイン中…'], ['请完整填写信息并同意服务条款', 'Complete the form and agree to the terms', '入力を完了し、利用規約に同意してください'], ['两次输入的密码不一致', 'Passwords do not match', 'パスワードが一致しません'], ['密码长度不少于 6 位，且必须包含大小写字母', 'Password must be at least 6 characters with upper- and lowercase letters', 'パスワードは6文字以上で、大文字と小文字を含めてください'],
  ['账户信息', 'Account information', 'アカウント情報'], ['退出登录', 'Log out', 'ログアウト'], ['打开导航', 'Open navigation', 'ナビゲーションを開く'], ['关闭导航', 'Close navigation', 'ナビゲーションを閉じる'], ['打开边栏', 'Open sidebar', 'サイドバーを開く'], ['收起边栏', 'Collapse sidebar', 'サイドバーを閉じる'], ['主导航', 'Main navigation', 'メインナビゲーション'], ['关闭', 'Close', '閉じる'], ['关闭确认弹窗', 'Close confirmation dialog', '確認ダイアログを閉じる'], ['确认删除', 'Confirm deletion', '削除を確認'], ['删除文章', 'Delete article', '記事を削除'], ['删除词库', 'Delete collection', '単語帳を削除'], ['删除词条', 'Delete word', '単語を削除'], ['删除之后该文章不能恢复，确定要继续吗？', 'This article cannot be recovered after deletion. Continue?', '削除した記事は復元できません。続行しますか？'], ['删除之后该词库不能恢复，确定要继续吗？', 'This collection cannot be recovered after deletion. Continue?', '削除した単語帳は復元できません。続行しますか？'], ['完成', 'Done', '完了'],
  ['阅读偏好', 'Reading preferences', '読書設定'], ['阅读偏好已保存', 'Reading preferences saved', '読書設定を保存しました'], ['界面语言', 'Interface language', '表示言語'], ['界面显示语言', 'Choose the language used by the interface', 'インターフェースの表示言語'], ['保存更改', 'Save changes', '変更を保存'], ['已保存', 'Saved', '保存しました'], ['更换头像', 'Change avatar', 'アバターを変更'], ['选择图片', 'Choose an image', '画像を選択'], ['头像预览', 'Avatar preview', 'アバターのプレビュー'], ['确认更换', 'Confirm change', '変更を確認'], ['上传中...', 'Uploading…', 'アップロード中…'], ['修改密码', 'Change password', 'パスワードを変更'], ['确认修改', 'Confirm change', '変更を確認'], ['提交中...', 'Submitting…', '送信中…'], ['旧密码', 'Current password', '現在のパスワード'], ['新密码', 'New password', '新しいパスワード'], ['确认新密码', 'Confirm new password', '新しいパスワードを確認'], ['请输入旧密码', 'Enter your current password', '現在のパスワードを入力してください'], ['密码已更新', 'Password updated', 'パスワードを更新しました'],
  ['阅读进度', 'Reading progress', '読書の進捗'], ['已解析词汇', 'Analyzed vocabulary', '解析済みの語彙'], ['打开词汇侧栏', 'Open vocabulary panel', '語彙パネルを開く'], ['收起词汇侧栏', 'Collapse vocabulary panel', '語彙パネルを閉じる'], ['返回文章库', 'Back to article library', '記事ライブラリに戻る'], ['隐藏释义', 'Hide meaning', '意味を隠す'], ['显示释义', 'Show meaning', '意味を表示'], ['不认识', 'Unknown', 'わからない'], ['有点模糊', 'Vague', '少し曖昧'], ['完全知道', 'Known', '完全にわかる'], ['今天的复习已经完成', 'Today’s review is complete', '今日の復習は完了しました'], ['回到列表查看新的待复习词条', 'Return to the list to see new words due for review', '一覧に戻って新しい復習対象を確認'],
  ['开始阅读', 'Start reading', '読書を開始'], ['上传外语文章，开启深度精读', 'Upload an article to begin deep reading', '外国語の記事をアップロードして精読を開始'], ['正在上传文章…', 'Uploading article…', '記事をアップロード中…'], ['点击下方开始今天的文章精读和外语学习', 'Start today’s deep reading and language study below', '下から今日の精読と語学学習を始めましょう'], ['Continue reading', 'Continue reading', '続きを読む'], ['Memory deck', 'Memory deck', 'メモリーデッキ'], ['MEMORY DECK', 'MEMORY DECK', 'メモリーデッキ'], ['FRIDAY · DEEP WORK', 'FRIDAY · DEEP WORK', 'FRIDAY · DEEP WORK'], ['词数', 'Words', '単語数'], ['字符数', 'Characters', '文字数'], ['大致阅读时间', 'Estimated reading time', '推定読書時間'], ['正在加载文章...', 'Loading article...', '記事を読み込み中…'], ['正在加载词汇...', 'Loading vocabulary...', '語彙を読み込み中…'], ['这篇文章还没有已解析的词汇等级', 'This article has no analyzed levels yet', 'この記事には解析済みのレベルがありません'], ['当前等级没有命中词汇', 'No words match the current level', '現在のレベルに一致する語彙はありません'], ['收起侧栏', 'Collapse panel', 'パネルを閉じる'], ['筛选词汇等级', 'Filter vocabulary by level', '語彙レベルで絞り込む'], ['生词解析', 'Word analysis', '語彙解析'], ['加入词库', 'Add to collection', '単語帳に追加'], ['暂无同语言词汇库', 'No collections for this language', 'この言語の単語帳はありません'], ['原文出现位置', 'Occurrences in the article', '記事内の出現箇所'], ['正在加载原文出现位置...', 'Loading occurrences...', '出現箇所を読み込み中…'], ['英语 · EN', 'English · EN', '英語 · EN'], ['日语 · JA', 'Japanese · JA', '日本語 · JA'], ['ADD CREDITS', 'ADD CREDITS', 'クレジットを追加'], ['添加资金', 'Add funds', '資金を追加'], ['选择金额和付款方式', 'Choose an amount and payment method', '金額と支払方法を選択'], ['支付宝 Alipay', 'Alipay', 'Alipay'], ['Payment preview', 'Payment preview', '支払いプレビュー'], ['Credits & billing', 'Credits & billing', 'Creditsと請求'], ['Repository · Collections', 'Repository · Collections', '単語帳 · コレクション'], ['REPOSITORY · COLLECTIONS', 'REPOSITORY · COLLECTIONS', '単語帳 · コレクション'], ['Preferences', 'Preferences', '設定'], ['Profile image', 'Profile image', 'プロフィール画像'], ['Account security', 'Account security', 'アカウントセキュリティ'], ['New collection', 'New collection', '新しい単語帳'], ['Learning statistics', 'Learning statistics', '学習統計'], ['Processing detail', 'Processing detail', '処理の詳細'], ['Vocabulary analysis', 'Vocabulary analysis', '語彙分析'], ['Confirm action', 'Confirm action', '操作を確認'],
].forEach(([source, en, ja]) => add(source, en, ja));

[
  ['Add credits', 'Add credits', 'クレジットを追加'], ['CREDITS & BILLING', 'CREDITS & BILLING', 'CREDITSと請求'], ['查看学习统计', 'View learning statistics', '学習統計を表示'], ['充值金额', 'Recharge amount', 'チャージ金額'], ['您支付', 'You pay', 'お支払い額'], ['到账时间', 'Received at', '入金日時'], ['订单号', 'Order number', '注文番号'], ['到账 Credits', 'Credits received', '受取Credits'], ['解析', 'Analyze', '解析'], ['返回单词复习', 'Back to review', '復習に戻る'], ['正在准备复习...', 'Preparing review...', '復習を準備中…'], ['逐个复习', 'Review one by one', '一つずつ復習'], ['Focused recall', 'Focused recall', '集中想起'], ['文章详情加载失败，请稍后重试', 'Failed to load article details. Please try again.', '記事詳細の読み込みに失敗しました。もう一度お試しください。'], ['文章上传失败，请稍后重试', 'Failed to upload the article. Please try again.', '記事のアップロードに失敗しました。もう一度お試しください。'], ['文章列表加载失败，请稍后重试', 'Failed to load articles. Please try again.', '記事一覧の読み込みに失敗しました。もう一度お試しください。'], ['词汇库加载失败，请稍后重试', 'Failed to load vocabulary. Please try again.', '単語帳の読み込みに失敗しました。もう一度お試しください。'], ['词汇解析失败，请稍后重试', 'Vocabulary analysis failed. Please try again.', '語彙解析に失敗しました。もう一度お試しください。'], ['删除文章失败', 'Failed to delete article', '記事の削除に失敗しました'], ['词汇库详情加载失败', 'Failed to load collection details', '単語帳の詳細を読み込めませんでした'], ['删除词条失败', 'Failed to delete word', '単語の削除に失敗しました'], ['查看文章详细信息', 'View article details', '記事の詳細を表示'], ['关闭文章详细信息弹窗', 'Close article details dialog', '記事詳細ダイアログを閉じる'], ['关闭词汇解析弹窗', 'Close vocabulary analysis dialog', '語彙解析ダイアログを閉じる'], ['关闭创建词库弹窗', 'Close create collection dialog', '単語帳作成ダイアログを閉じる'], ['关闭头像弹窗', 'Close avatar dialog', 'アバターダイアログを閉じる'], ['关闭付款确认弹窗', 'Close payment dialog', '支払いダイアログを閉じる'], ['关闭记录弹窗', 'Close records dialog', '記録ダイアログを閉じる'], ['正在加载记录...', 'Loading records...', '記録を読み込み中…'], ['我已阅读并同意服务条款与隐私政策', 'I have read and agree to the terms and privacy policy', '利用規約とプライバシーポリシーに同意します'], ['加入 LexiFlow，开启精读学习之旅', 'Join LexiFlow and begin your deep-reading journey', 'LexiFlowに参加して精読学習を始めましょう'], ['密码', 'Password', 'パスワード'], ['更改用户名', 'Change username', 'ユーザー名を変更'], ['用户名已更新', 'Username updated', 'ユーザー名を更新しました'], ['用户名更新失败', 'Failed to update username', 'ユーザー名の更新に失敗しました'], ['头像更新失败', 'Failed to update avatar', 'アバターの更新に失敗しました'], ['新密码长度不少于 6 位，且必须包含大小写字母', 'New password must be at least 6 characters with upper- and lowercase letters', '新しいパスワードは6文字以上で、大文字と小文字を含めてください'], ['两次输入的新密码不一致', 'New passwords do not match', '新しいパスワードが一致しません'], ['密码更新失败', 'Failed to update password', 'パスワードの更新に失敗しました'],
  ['当前头像', 'Current avatar', '現在のアバター'], ['头像预览', 'Avatar preview', 'アバターのプレビュー'], ['显示旧密码', 'Show current password', '現在のパスワードを表示'], ['隐藏旧密码', 'Hide current password', '現在のパスワードを隠す'], ['显示新密码', 'Show new password', '新しいパスワードを表示'], ['隐藏新密码', 'Hide new password', '新しいパスワードを隠す'], ['隐藏确认密码', 'Hide confirmation password', '確認用パスワードを隠す'], ['显示确认密码', 'Show confirmation password', '確認用パスワードを表示'], ['不少于 6 位，且必须包含大小写字母', 'At least 6 characters with upper- and lowercase letters', '6文字以上で、大文字と小文字を含めてください'],
].forEach(([source, en, ja]) => add(source, en, ja));

[
  ['返回单词复习', 'Back to review', '復習に戻る'], ['正在准备复习...', 'Preparing review...', '復習を準備中…'], ['逐个复习', 'Review one by one', '一つずつ復習'], ['Focused recall', 'Focused recall', '集中想起'], ['文章详情加载失败，请稍后重试', 'Failed to load article details. Please try again.', '記事詳細の読み込みに失敗しました。もう一度お試しください。'], ['文章上传失败，请稍后重试', 'Failed to upload the article. Please try again.', '記事のアップロードに失敗しました。もう一度お試しください。'], ['文章列表加载失败，请稍后重试', 'Failed to load articles. Please try again.', '記事一覧の読み込みに失敗しました。もう一度お試しください。'], ['词汇库加载失败，请稍后重试', 'Failed to load vocabulary. Please try again.', '単語帳の読み込みに失敗しました。もう一度お試しください。'], ['词汇解析失败，请稍后重试', 'Vocabulary analysis failed. Please try again.', '語彙解析に失敗しました。もう一度お試しください。'], ['删除文章失败', 'Failed to delete article', '記事の削除に失敗しました'], ['词汇库详情加载失败', 'Failed to load collection details', '単語帳の詳細を読み込めませんでした'], ['删除词条失败', 'Failed to delete word', '単語の削除に失敗しました'], ['查看学习统计', 'View learning statistics', '学習統計を表示'], ['查看文章详细信息', 'View article details', '記事の詳細を表示'], ['关闭文章详细信息弹窗', 'Close article details dialog', '記事詳細ダイアログを閉じる'], ['关闭词汇解析弹窗', 'Close vocabulary analysis dialog', '語彙解析ダイアログを閉じる'], ['关闭创建词库弹窗', 'Close create collection dialog', '単語帳作成ダイアログを閉じる'], ['关闭头像弹窗', 'Close avatar dialog', 'アバターダイアログを閉じる'], ['关闭付款确认弹窗', 'Close payment dialog', '支払いダイアログを閉じる'], ['关闭记录弹窗', 'Close records dialog', '記録ダイアログを閉じる'], ['充值金额', 'Recharge amount', 'チャージ金額'], ['您支付', 'You pay', 'お支払い額'], ['到账时间', 'Received at', '入金日時'], ['订单号', 'Order number', '注文番号'], ['到账 Credits', 'Credits received', '受取Credits'], ['正在加载记录...', 'Loading records...', '記録を読み込み中…'], ['我已阅读并同意服务条款与隐私政策', 'I have read and agree to the terms and privacy policy', '利用規約とプライバシーポリシーに同意します'], ['加入 LexiFlow，开启精读学习之旅', 'Join LexiFlow and begin your deep-reading journey', 'LexiFlowに参加して精読学習を始めましょう'], ['密码', 'Password', 'パスワード'], ['更改用户名', 'Change username', 'ユーザー名を変更'], ['用户名已更新', 'Username updated', 'ユーザー名を更新しました'], ['用户名更新失败', 'Failed to update username', 'ユーザー名の更新に失敗しました'], ['头像更新失败', 'Failed to update avatar', 'アバターの更新に失敗しました'], ['新密码长度不少于 6 位，且必须包含大小写字母', 'New password must be at least 6 characters with upper- and lowercase letters', '新しいパスワードは6文字以上で、大文字と小文字を含めてください'], ['两次输入的新密码不一致', 'New passwords do not match', '新しいパスワードが一致しません'], ['密码更新失败', 'Failed to update password', 'パスワードの更新に失敗しました'], ['正在加载记录...', 'Loading records...', '記録を読み込み中…'], ['解析', 'Analyze', '解析'], ['共', 'Total', '合計'],
].forEach(([source, en, ja]) => add(source, en, ja));

[
  ['请选择', 'Please choose', '選択してください'], ['请求失败', 'Request failed', 'リクエストに失敗しました'], ['词汇库不存在', 'Collection not found', '単語帳が見つかりません'], ['创建词汇库失败', 'Failed to create collection', '単語帳の作成に失敗しました'], ['删除词库失败', 'Failed to delete collection', '単語帳の削除に失敗しました'], ['加入失败，请稍后重试', 'Failed to add the word. Please try again.', '追加に失敗しました。もう一度お試しください。'], ['一个基于新闻、文献及学术研究的智能语言学习平台', 'An intelligent language-learning platform built around news, literature, and academic research', 'ニュース、文献、学術研究を通じて学ぶインテリジェントな語学学習プラットフォーム'], ['读有所得，记有所长', 'Read deeply. Remember more.', '深く読み、確かに覚える'], ['继续您的外刊精读和外语学习', 'Continue your deep reading and language study', '海外記事の精読と語学学習を続けましょう'], ['登录 LexiFlow', 'Sign in to LexiFlow', 'LexiFlowにログイン'], ['密码长度不少于6位，且必须包含大小写字母', 'Password must be at least 6 characters with upper- and lowercase letters', 'パスワードは6文字以上で、大文字と小文字を含めてください'], ['选择英语词汇库', 'Choose an English collection', '英語の単語帳を選択'], ['选择日语词汇库', 'Choose a Japanese collection', '日本語の単語帳を選択'],
].forEach(([source, en, ja]) => add(source, en, ja));

[
  ['商务英语', 'Business English', 'ビジネス英語'], ['大学英语四级', 'College English Test Band 4', '大学英語四級'], ['大学英语六级', 'College English Test Band 6', '大学英語六級'], ['商学院入学考试', 'Business school entrance exam', 'ビジネススクール入試'], ['研究生入学考试', 'Graduate school entrance exam', '大学院入試'], ['英语四级词表', 'CET-4 vocabulary list', '英語四級語彙'], ['英语八级词表', 'CET-8 vocabulary list', '英語八級語彙'], ['美国大学入学考试', 'US college entrance exam', '米国大学入試'], ['初中英语词表', 'Junior high English vocabulary', '中学英語語彙'], ['高中英语词表', 'Senior high English vocabulary', '高校英語語彙'], ['考研英语词表', 'Postgraduate entrance exam vocabulary', '大学院入試英語語彙'], ['日语入门', 'Japanese for beginners', '日本語入門'], ['基础日语', 'Basic Japanese', '基礎日本語'], ['日常阅读', 'Everyday reading', '日常読解'], ['中高级阅读', 'Upper-intermediate reading', '中上級読解'], ['高级阅读', 'Advanced reading', '上級読解'],
].forEach(([source, en, ja]) => add(source, en, ja));

const dynamicPatterns: Array<{ pattern: RegExp; render: (match: RegExpExecArray, locale: InterfaceLanguage) => string }> = [
  { pattern: /^Repository · (\d[\d,]*) texts$/, render: (m, l) => l === 'en' ? `Repository · ${m[1]} texts` : l === 'ja' ? `ライブラリ · ${m[1]}件` : `文章库 · ${m[1]} 篇` },
  { pattern: /^Collection · (\d[\d,]*) words$/, render: (m, l) => l === 'en' ? `Collection · ${m[1]} words` : l === 'ja' ? `単語帳 · ${m[1]}語` : `词汇库 · ${m[1]} 词` },
  { pattern: /^· Demo$/, render: (m, l) => l === 'ja' ? '· デモ' : l === 'en' ? '· Demo' : '· 示例数据' },
  { pattern: /^(\d[\d,]*) 个词条$/, render: (m, l) => l === 'en' ? `${m[1]} words` : l === 'ja' ? `${m[1]} 語` : `${m[1]} 个词条` },
  { pattern: /^(\d[\d,]*) 个词汇已(复用|完成解析)$/, render: (m, l) => l === 'en' ? `${m[1]} words ${m[2] === '复用' ? 'reused' : 'analyzed'}` : l === 'ja' ? `${m[1]}語を${m[2] === '复用' ? '再利用' : '解析'}` : `${m[1]} 个词汇已${m[2]}` },
  { pattern: /^这是你注册的第 (\d+) 天$/, render: (m, l) => l === 'en' ? `Day ${m[1]} since joining` : l === 'ja' ? `登録から${m[1]}日目` : `这是你注册的第 ${m[1]} 天` },
  { pattern: /^解析 (.+) 的词汇$/, render: (m, l) => l === 'en' ? `Analyze vocabulary in ${m[1]}` : l === 'ja' ? `${m[1]} の語彙を解析` : `解析 ${m[1]} 的词汇` },
  { pattern: /^查看 (.+) 详细信息$/, render: (m, l) => l === 'en' ? `View details for ${m[1]}` : l === 'ja' ? `${m[1]} の詳細を表示` : `查看 ${m[1]} 详细信息` },
  { pattern: /^查看 (.+) 统计$/, render: (m, l) => l === 'en' ? `View statistics for ${m[1]}` : l === 'ja' ? `${m[1]} の統計を表示` : `查看 ${m[1]} 统计` },
  { pattern: /^从词汇库删除 (.+)$/, render: (m, l) => l === 'en' ? `Remove ${m[1]} from collection` : l === 'ja' ? `${m[1]} を単語帳から削除` : `从词汇库删除 ${m[1]}` },
  { pattern: /^已加入“(.+)”$/, render: (m, l) => l === 'en' ? `Added to “${m[1]}”` : l === 'ja' ? `「${m[1]}」に追加しました` : `已加入“${m[1]}”` },
  { pattern: /^欢迎回来，(.+)$/, render: (m, l) => l === 'en' ? `Welcome back, ${m[1]}` : l === 'ja' ? `おかえりなさい、${m[1]}` : `欢迎回来，${m[1]}` },
  { pattern: /^(\d+) 个词待复习$/, render: (m, l) => l === 'en' ? `${m[1]} words due for review` : l === 'ja' ? `復習待ちの単語 ${m[1]} 語` : `${m[1]} 个词待复习` },
  { pattern: /^创建时间 · (.+)$/, render: (m, l) => l === 'en' ? `Created · ${m[1]}` : l === 'ja' ? `作成日時 · ${m[1]}` : `创建时间 · ${m[1]}` },
  { pattern: /^(\d+) 分钟$/, render: (m, l) => l === 'en' ? `${m[1]} min` : l === 'ja' ? `${m[1]}分` : `${m[1]} 分钟` },
  { pattern: /^已加入 · (.+)$/, render: (m, l) => l === 'en' ? `Added · ${m[1]}` : l === 'ja' ? `追加済み · ${m[1]}` : `已加入 · ${m[1]}` },
  { pattern: /^(.+) 已上传，文章正在后台处理$/, render: (m, l) => l === 'en' ? `${m[1]} uploaded. The article is processing in the background.` : l === 'ja' ? `${m[1]} をアップロードしました。記事をバックグラウンドで処理中です` : `${m[1]} 已上传，文章正在后台处理` },
  { pattern: /^订单 (.+) 已创建，请继续完成支付$/, render: (m, l) => l === 'en' ? `Order ${m[1]} created. Continue to payment.` : l === 'ja' ? `注文 ${m[1]} を作成しました。支払いを続けてください` : `订单 ${m[1]} 已创建，请继续完成支付` },
  { pattern: /^共 (\d[\d,]*) 条$/, render: (m, l) => l === 'en' ? `${m[1]} total` : l === 'ja' ? `全${m[1]}件` : `共 ${m[1]} 条` },
  { pattern: /^(\d[\d,]*) 词$/, render: (m, l) => l === 'en' ? `${m[1]} words` : l === 'ja' ? `${m[1]} 語` : `${m[1]} 词` },
]

export function translateText(value: string, locale: InterfaceLanguage) {
  const leading = value.match(/^\s*/)?.[0] ?? ''
  const trailing = value.match(/\s*$/)?.[0] ?? ''
  const trimmed = value.trim()
  if (!trimmed) return value
  const direct = translations[trimmed]?.[locale]
  if (direct) return `${leading}${direct}${trailing}`
  for (const item of dynamicPatterns) {
    const match = item.pattern.exec(trimmed)
    if (match) return `${leading}${item.render(match, locale)}${trailing}`
  }
  return value
}

const textSources = new WeakMap<Text, string>()
const textRenders = new WeakMap<Text, string>()
const attributeSources = new WeakMap<Element, Map<string, string>>()
const attributeRenders = new WeakMap<Element, Map<string, string>>()

function translateElement(element: Element, locale: InterfaceLanguage) {
  const attributes = ['placeholder', 'title', 'aria-label', 'alt']
  const sources = attributeSources.get(element) ?? new Map<string, string>()
  const renders = attributeRenders.get(element) ?? new Map<string, string>()
  for (const name of attributes) {
    const value = element.getAttribute(name)
    if (value === null) continue
    const source = sources.get(name)
    const previous = renders.get(name)
    const original = source && value === previous ? source : value
    const next = translateText(original, locale)
    sources.set(name, original)
    renders.set(name, next)
    if (next !== value) element.setAttribute(name, next)
  }
  attributeSources.set(element, sources)
  attributeRenders.set(element, renders)
}

export function translateDocument(locale: InterfaceLanguage) {
  const root = document.getElementById('app')
  if (!root) return
  const walker = document.createTreeWalker(root, NodeFilter.SHOW_TEXT)
  let node: Node | null
  while ((node = walker.nextNode())) {
    const text = node as Text
    if (text.parentElement?.closest('script,style')) continue
    const source = textSources.get(text)
    const previous = textRenders.get(text)
    const original = source && text.nodeValue === previous ? source : text.nodeValue ?? ''
    const next = translateText(original, locale)
    textSources.set(text, original)
    textRenders.set(text, next)
    if (next !== text.nodeValue) text.nodeValue = next
  }
  root.querySelectorAll('*').forEach((element) => translateElement(element, locale))
}

export function installDocumentLocalization(getLocale: () => InterfaceLanguage) {
  let queued = false
  const refresh = () => {
    if (queued) return
    queued = true
    queueMicrotask(() => {
      queued = false
      translateDocument(getLocale())
    })
  }
  const observer = new MutationObserver(refresh)
  observer.observe(document.getElementById('app') ?? document.body, { childList: true, subtree: true, characterData: true, attributes: true, attributeFilter: ['placeholder', 'title', 'aria-label', 'alt'] })
  refresh()
  return () => observer.disconnect()
}
