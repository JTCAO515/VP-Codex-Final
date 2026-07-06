// i18n.js — 多语种翻译表 + 运行时替换（零依赖 Vanilla）
// 默认英语，支持 9 语言：en/zh/ru/es/ar/ko/ja/fr/de

const I18N = {
  "app.title": {
    en: "China Travel Agent", zh:"中国旅行助手", ru:"Китайский Туристический Агент",
    es:"Agente de Viajes de China", ar:"وكيل السفر الصيني", ko:"중국 여행 도우미",
    ja:"中国旅行エージェント", fr:"Agent de voyage en Chine", de:"China Reise Agent"
  },
  "app.tagline": {
    en: "Plan your China trip",
    zh:"规划你的中国之旅", ru:"Спланируйте поездку в Китай",
    es:"Planifica tu viaje a China", ar:"خطط لرحلتك إلى الصين",
    ko:"중국 여행을 계획하세요", ja:"中国旅行を計画しよう",
    fr:"Planifiez votre voyage en Chine", de:"Planen Sie Ihre China-Reise"
  },
  "app.subtitle": {
    en: "Ask less, chat more. Your itinerary and orders are saved automatically (after login).",
    zh:"少问问题，多交流。先开聊，行程与订单会自动归档到你的历史里（登录后）。",
    ru:"Меньше вопросов, больше общения. Маршруты и заказы сохраняются автоматически.",
    es:"Menos preguntas, más conversación. Tu itinerario se guarda automáticamente.",
    ar:"اسأل أقل، تحدث أكثر. يتم حفظ مسار رحلتك وطلباتك تلقائيًا.",
    ko:"질문은 적게, 대화는 많이. 여행 일정과 주문이 자동 저장됩니다.",
    ja:"質問は少なく、会話を多く。旅程と注文は自動的に保存されます。",
    fr:"Moins de questions, plus de dialogue. Votre itinéraire est sauvegardé automatiquement.",
    de:"Weniger fragen, mehr reden. Ihre Reiseroute wird automatisch gespeichert."
  },
  "app.start": {en:"Start", zh:"开始", ru:"Начать", es:"Empezar", ar:"ابدأ", ko:"시작", ja:"開始", fr:"Démarrer", de:"Start"},
  "app.placeholder": {
    en: "Start chatting... (e.g. Beijing 5 days, food+history, relaxed pace)",
    zh:"开始对话…（例如：北京 5 天游玩，美食+历史，节奏轻松）",
    ru:"Начните чат... (например: Пекин 5 дней, еда+история, спокойный темп)",
    es:"Empieza a chatear... (ej: Pekín 5 días, comida+historia, ritmo relajado)",
    ar:"ابدأ الدردشة... (مثال: بكين 5 أيام، طعام+تاريخ، وتيرة مريحة)",
    ko:"채팅 시작... (예: 베이징 5일, 음식+역사, 여유로운 일정)",
    ja:"チャットを始める… (例：北京5日間、食+歴史、ゆったりペース)",
    fr:"Commencez à discuter... (ex: Pékin 5 jours, gastronomie+histoire, rythme tranquille)",
    de:"Chat starten... (z.B. Peking 5 Tage, Essen+Geschichte, entspannt)"
  },
  "app.open_chat": {en:"Open chat", zh:"打开对话", ru:"Открыть чат", es:"Abrir chat", ar:"فتح الدردشة", ko:"채팅 열기", ja:"チャットを開く", fr:"Ouvrir le chat", de:"Chat öffnen"},
  "app.sign_in": {en:"Sign in with Google", zh:"Google 登录", ru:"Войти через Google", es:"Iniciar sesión con Google", ar:"تسجيل الدخول عبر جوجل", ko:"Google 로그인", ja:"Googleでログイン", fr:"Se connecter avec Google", de:"Mit Google anmelden"},
  "app.guest": {en:"Continue as guest", zh:"游客模式", ru:"Продолжить как гость", es:"Continuar como invitado", ar:"المتابعة كزائر", ko:"게스트로 계속", ja:"ゲストとして続ける", fr:"Continuer en tant qu'invité", de:"Als Gast fortfahren"},
  "app.footer": {
    en: "Tip: You can try without login (last 3 sessions saved locally). Login to sync history across devices.",
    zh:"提示：未登录也能试用（本地保留最近 3 个会话）。登录后将同步保存对话/行程/订单历史。",
    ru:"Совет: Можно试用 без входа (3 сессии сохраняются локально). Войдите для синхронизации.",
    es:"Consejo: Puedes probar sin iniciar sesión (3 sesiones guardadas localmente).",
    ar:"تلميح: يمكنك التجربة بدون تسجيل الدخول (آخر 3 جلسات محفوظة محليًا).",
    ko:"팁: 로그인 없이도 사용 가능 (최근 3개 세션 로컬 저장). 로그인하면 기기간 동기화.",
    ja:"ヒント：ログインなしでもお試しいただけます（直近3セッションをローカル保存）。",
    fr:"Astuce : Essayez sans vous connecter (3 sessions sauvegardées localement).",
    de:"Tipp: Ohne Login nutzbar (3 Sitzungen lokal). Login für geräteübergreifende Synchronisation."
  },
  "auth.signing_in": {en:"Signing in…", zh:"正在登录…", ru:"Вход…", es:"Iniciando sesión…", ar:"جاري تسجيل الدخول…", ko:"로그인 중…", ja:"ログイン中…", fr:"Connexion en cours…", de:"Anmeldung…"},
  "auth.redirect": {en:"Redirecting in 3s…", zh:"3秒后跳转…", ru:"Перенаправление через 3с…", es:"Redirigiendo en 3s…", ar:"إعادة التوجيه خلال 3 ثوان…", ko:"3초 후 이동…", ja:"3秒後にリダイレクト…", fr:"Redirection dans 3s…", de:"Weiterleitung in 3s…"},

  // Chat page
  "chat.title": {en:"Chat", zh:"对话", ru:"Чат", es:"Chat", ar:"دردشة", ko:"채팅", ja:"チャット", fr:"Chat", de:"Chat"},
  "chat.new_trip": {en:"+ New Trip", zh:"+ 新行程", ru:"+ Новый", es:"+ Nuevo", ar:"+ رحلة جديدة", ko:"+ 새 여행", ja:"+ 新規旅行", fr:"+ Nouveau", de:"+ Neue Reise"},
  "chat.type_message": {en:"Type a message…", zh:"输入消息…", ru:"Введите сообщение…", es:"Escribe un mensaje…", ar:"اكتب رسالة…", ko:"메시지 입력…", ja:"メッセージを入力…", fr:"Écrivez un message…", de:"Nachricht eingeben…"},
  "chat.send": {en:"Send", zh:"发送", ru:"Отправить", es:"Enviar", ar:"إرسال", ko:"보내기", ja:"送信", fr:"Envoyer", de:"Senden"},
  "chat.loading": {en:"Thinking…", zh:"思考中…", ru:"Думаю…", es:"Pensando…", ar:"يفكر…", ko:"생각 중…", ja:"考え中…", fr:"Réflexion…", de:"Denke nach…"},
  "chat.sign_out": {en:"Sign out", zh:"退出登录", ru:"Выйти", es:"Cerrar sesión", ar:"تسجيل الخروج", ko:"로그아웃", ja:"ログアウト", fr:"Déconnexion", de:"Abmelden"},
  "chat.sessions": {en:"Sessions", zh:"会话", ru:"Сессии", es:"Sesiones", ar:"الجلسات", ko:"세션", ja:"セッション", fr:"Sessions", de:"Sitzungen"},
  "chat.itinerary": {en:"Itinerary", zh:"行程", ru:"Маршрут", es:"Itinerario", ar:"مسار الرحلة", ko:"여정", ja:"旅程", fr:"Itinéraire", de:"Reiseplan"},
  "chat.hotel": {en:"Hotel", zh:"酒店", ru:"Отель", es:"Hotel", ar:"فندق", ko:"호텔", ja:"ホテル", fr:"Hôtel", de:"Hotel"},
  "chat.orders": {en:"Orders", zh:"订单", ru:"Заказы", es:"Pedidos", ar:"الطلبات", ko:"주문", ja:"注文", fr:"Commandes", de:"Bestellungen"},
  "chat.close": {en:"Close", zh:"关闭", ru:"Закрыть", es:"Cerrar", ar:"إغلاق", ko:"닫기", ja:"閉じる", fr:"Fermer", de:"Schließen"},

  // Supplier pages
  "supplier.login": {en:"Supplier Login", zh:"供应商登录", ru:"Вход поставщика", es:"Acceso proveedor", ar:"تسجيل دخول المورد", ko:"공급자 로그인", ja:"サプライヤーログイン", fr:"Connexion fournisseur", de:"Lieferanten-Login"},
  "supplier.api_key": {en:"API Key", zh:"API 密钥", ru:"API ключ", es:"Clave API", ar:"مفتاح API", ko:"API 키", ja:"APIキー", fr:"Clé API", de:"API-Schlüssel"},
  "supplier.login_btn": {en:"Login", zh:"登录", ru:"Войти", es:"Iniciar", ar:"دخول", ko:"로그인", ja:"ログイン", fr:"Connexion", de:"Anmelden"},
  "supplier.dashboard": {en:"Dashboard", zh:"仪表盘", ru:"Панель", es:"Panel", ar:"لوحة التحكم", ko:"대시보드", ja:"ダッシュボード", fr:"Tableau de bord", de:"Dashboard"},
  "supplier.rfps": {en:"RFPs", zh:"询价单", ru:"Запросы", es:"Solicitudes", ar:"طلبات العروض", ko:"견적요청", ja:"見積依頼", fr:"Appels d'offres", de:"Angebotsanfragen"},
  "supplier.bid": {en:"Submit Bid", zh:"提交报价", ru:"Подать", es:"Enviar oferta", ar:"تقديم عرض", ko:"견적 제출", ja:"見積提出", fr:"Soumettre", de:"Angebot abgeben"},
  "supplier.amount": {en:"Amount (¥)", zh:"金额 (¥)", ru:"Сумма (¥)", es:"Importe (¥)", ar:"المبلغ (¥)", ko:"금액 (¥)", ja:"金額 (¥)", fr:"Montant (¥)", de:"Betrag (¥)"},
  "supplier.notes": {en:"Notes", zh:"备注", ru:"Примечания", es:"Notas", ar:"ملاحظات", ko:"비고", ja:"備考", fr:"Notes", de:"Notizen"},
  "supplier.price": {en:"Price", zh:"价格", ru:"Цена", es:"Precio", ar:"السعر", ko:"가격", ja:"価格", fr:"Prix", de:"Preis"},
  "supplier.status": {en:"Status", zh:"状态", ru:"Статус", es:"Estado", ar:"الحالة", ko:"상태", ja:"状態", fr:"Statut", de:"Status"},
  "supplier.no_data": {en:"No data yet", zh:"暂无数据", ru:"Нет данных", es:"Sin datos", ar:"لا توجد بيانات", ko:"데이터 없음", ja:"データなし", fr:"Aucune donnée", de:"Keine Daten"},
};

// Lang switcher config
const LANGS = [
  {code:"en", name:"English", flag:"🇬🇧", rtl:false},
  {code:"zh", name:"中文", flag:"🇨🇳", rtl:false},
  {code:"ru", name:"Русский", flag:"🇷🇺", rtl:false},
  {code:"es", name:"Español", flag:"🇪🇸", rtl:false},
  {code:"ar", name:"العربية", flag:"🇸🇦", rtl:true},
  {code:"ko", name:"한국어", flag:"🇰🇷", rtl:false},
  {code:"ja", name:"日本語", flag:"🇯🇵", rtl:false},
  {code:"fr", name:"Français", flag:"🇫🇷", rtl:false},
  {code:"de", name:"Deutsch", flag:"🇩🇪", rtl:false},
];

// ── Runtime ──

let _lang = "en";

export function getLang() { return _lang; }

export function setLang(code) {
  _lang = code;
  localStorage.setItem("cta_lang", code);
  applyI18n();
}

export function t(key) {
  const entry = I18N[key];
  if (!entry) return key;
  return entry[_lang] || entry.en || key;
}

export function applyI18n() {
  // data-i18n attributes
  document.querySelectorAll("[data-i18n]").forEach(el => {
    const key = el.getAttribute("data-i18n");
    const text = t(key);
    if (el.tagName === "INPUT" || el.tagName === "TEXTAREA") {
      el.placeholder = text;
    } else {
      el.textContent = text;
    }
  });
  // HTML lang attr
  document.documentElement.lang = _lang;
  // RTL support
  document.documentElement.dir = LANGS.find(l => l.code === _lang)?.rtl ? "rtl" : "ltr";
}

export function initI18n() {
  // Priority: URL ?lang=xx > localStorage > browser > default en
  const param = new URLSearchParams(window.location.search).get("lang");
  const stored = localStorage.getItem("cta_lang");
  const browser = (navigator.language || "en").split("-")[0];
  const valid = LANGS.map(l => l.code);
  _lang = param || stored || (valid.includes(browser) ? browser : "en");
  applyI18n();
}

export function renderLangSwitcher(containerId) {
  const container = document.getElementById(containerId);
  if (!container) return;
  container.innerHTML = `
    <select id="langSwitcher" style="background:transparent;border:1px solid rgba(255,255,255,.15);color:rgba(255,255,255,.78);padding:4px 8px;border-radius:6px;font-size:12px;cursor:pointer">
      ${LANGS.map(l => `<option value="${l.code}" ${l.code===_lang?'selected':''}>${l.flag} ${l.name}</option>`).join("")}
    </select>
  `;
  document.getElementById("langSwitcher").onchange = e => setLang(e.target.value);
}
