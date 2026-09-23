package run.hotspotnews.web;

/**
 * 摸鱼页面（单文件 HTML）—— 带动画效果的精致版。
 *
 * <p>页面通过 {@code /apis/api.plugin.halo.run/v1alpha1/hotspot-news} 拉取聚合数据，
 * 支持来源筛选与"摸鱼模式"（一键伪装为工作文档样式）。</p>
 */
public final class NewsPage {

    private NewsPage() {
    }

    public static final String HTML = """
        <!DOCTYPE html>
        <html lang="zh-CN">
        <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>摸鱼一刻 · 笑话与鸡汤</title>
        <style>
          @keyframes gradientShift { 0%{background-position:0% 50%} 50%{background-position:100% 50%} 100%{background-position:0% 50%} }
          @keyframes floatY { 0%,100%{transform:translateY(0)} 50%{transform:translateY(-6px)} }
          @keyframes fadeInUp { from{opacity:0;transform:translateY(20px)} to{opacity:1;transform:translateY(0)} }
          @keyframes pulse { 0%,100%{opacity:.4;transform:scale(1)} 50%{opacity:1;transform:scale(1.15)} }
          @keyframes shimmer { 0%{background-position:-200% 0} 100%{background-position:200% 0} }
          @keyframes bounceIn { 0%{opacity:0;transform:scale(.3)} 50%{transform:scale(1.05)} 70%{transform:scale(.95)} 100%{opacity:1;transform:scale(1)} }
          @keyframes slideDown { from{opacity:0;transform:translateY(-10px)} to{opacity:1;transform:translateY(0)} }
          @keyframes cardIn { 0%{opacity:0;transform:translateY(34px) scale(.94)} 55%{opacity:1;transform:translateY(-6px) scale(1.012)} 75%{transform:translateY(2px) scale(.997)} 100%{opacity:1;transform:translateY(0) scale(1)} }
          @keyframes fadeOutDown { from{opacity:1;transform:translateY(0)} to{opacity:0;transform:translateY(18px)} }
          @keyframes spin { to{transform:rotate(360deg)} }

          :root {
            --joke: #f59e0b;
            --joke-light: rgba(245,158,11,.1);
            --soup: #10b981;
            --soup-light: rgba(16,185,129,.1);
            --bg: #f0f2f5;
            --card: #ffffff;
            --text: #1f2329;
            --muted: #8a9099;
            --line: #e6e8eb;
            --primary: #6366f1;
            --primary-dark: #4f46e5;
          }
          * { box-sizing: border-box; margin: 0; padding: 0; }
          body { background: var(--bg); color: var(--text); font-family: "PingFang SC","Microsoft YaHei",sans-serif; min-height: 100vh; }

          /* ===== 页头 ===== */
          .hero {
            position: relative;
            overflow: hidden;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 25%, #f59e0b 50%, #764ba2 75%, #667eea 100%);
            background-size: 300% 300%;
            animation: gradientShift 12s ease infinite;
            padding: 48px 20px 56px;
            text-align: center;
            color: #fff;
          }
          .hero::after {
            content: '';
            position: absolute; left: 0; right: 0; bottom: -1px; height: 40px;
            background: var(--bg);
            border-radius: 50% 50% 0 0 / 100% 100% 0 0;
          }
          .hero .emoji {
            font-size: 48px;
            display: inline-block;
            animation: floatY 3s ease-in-out infinite;
          }
          .hero h1 {
            font-size: 34px; font-weight: 800; margin-top: 12px; letter-spacing: 2px;
            text-shadow: 0 2px 12px rgba(0,0,0,.2);
            animation: fadeInUp .8s ease;
          }
          .hero .subtitle {
            font-size: 16px; opacity: .85; margin-top: 8px;
            animation: fadeInUp .8s ease .15s backwards;
          }
          .hero .stats {
            display: flex; justify-content: center; gap: 24px; margin-top: 18px;
            animation: fadeInUp .8s ease .3s backwards;
          }
          .hero .stat { text-align: center; }
          .hero .stat .num { font-size: 22px; font-weight: 700; }
          .hero .stat .label { font-size: 12px; opacity: .7; }

          /* ===== 工具栏 ===== */
          .wrap { max-width: 720px; margin: -28px auto 0; padding: 0 16px 40px; position: relative; z-index: 1; }
          .toolbar {
            display: flex; align-items: center; justify-content: space-between;
            margin-bottom: 16px;
            animation: slideDown .5s ease;
          }
          .toolbar .tabs { display: flex; gap: 8px; }
          .tab {
            padding: 8px 16px; border-radius: 999px; font-size: 13px; font-weight: 600;
            cursor: pointer; background: #fff; color: #5f6773; border: 1px solid var(--line);
            transition: all .25s;
          }
          .tab:hover { border-color: var(--primary); color: var(--primary); transform: translateY(-1px); }
          .tab.active {
            background: linear-gradient(135deg, var(--primary), var(--primary-dark));
            color: #fff; border-color: transparent;
            box-shadow: 0 4px 14px rgba(99,102,241,.3);
          }
          .toolbar .actions { display: flex; gap: 8px; }
          .btn {
            border: 0; border-radius: 10px; padding: 8px 14px; font-size: 13px; font-weight: 600;
            cursor: pointer; background: #fff; color: var(--text);
            box-shadow: 0 2px 8px rgba(0,0,0,.06); transition: all .25s;
          }
          .btn:hover { transform: translateY(-2px); box-shadow: 0 6px 16px rgba(0,0,0,.1); }
          .btn:active { transform: translateY(0); }
          .btn.primary {
            background: linear-gradient(135deg, var(--primary), var(--primary-dark));
            color: #fff;
          }

          /* ===== 搜索 ===== */
          .search-wrap { position: relative; margin-bottom: 16px; }
          .search {
            width: 100%; padding: 12px 16px 12px 42px;
            border: 1px solid var(--line); border-radius: 12px; font-size: 14px;
            background: var(--card); outline: none; transition: all .25s;
          }
          .search:focus { border-color: var(--primary); box-shadow: 0 0 0 4px rgba(99,102,241,.1); }
          .search-icon {
            position: absolute; left: 14px; top: 50%; transform: translateY(-50%);
            width: 18px; height: 18px; opacity: .4;
          }

          /* ===== 列表（每批 3 条大卡片） ===== */
          .meta { font-size: 13px; color: var(--muted); margin: 0 6px 14px; }
          .list { display: grid; gap: 20px; }
          .list.swapping .card { animation: fadeOutDown .3s ease forwards; }
          .card {
            position: relative; overflow: hidden;
            background: var(--card); border-radius: 20px; padding: 30px 34px 28px;
            box-shadow: 0 2px 12px rgba(0,0,0,.05);
            transition: transform .3s, box-shadow .3s;
            animation: cardIn .65s cubic-bezier(.22,1,.36,1) backwards;
            border: 1px solid transparent;
          }
          .card .idx {
            position: absolute; right: 20px; top: 10px;
            font-size: 56px; font-weight: 800; line-height: 1;
            font-family: Georgia, "Times New Roman", serif;
            color: rgba(99,102,241,.08); pointer-events: none; user-select: none;
          }
          .card:hover {
            transform: translateY(-3px);
            box-shadow: 0 8px 24px rgba(0,0,0,.08);
            border-color: var(--line);
          }
          .card .head { display: flex; align-items: center; gap: 10px; margin-bottom: 14px; flex-wrap: wrap; }
          .badge {
            font-size: 12px; padding: 4px 12px; border-radius: 999px; font-weight: 700;
            display: inline-flex; align-items: center; gap: 5px;
          }
          .badge::before { content: ''; width: 6px; height: 6px; border-radius: 50%; }
          .badge.joke { background: var(--joke-light); color: var(--joke); }
          .badge.joke::before { background: var(--joke); }
          .badge.soup { background: var(--soup-light); color: var(--soup); }
          .badge.soup::before { background: var(--soup); }
          .card .cat {
            font-size: 13px; color: var(--muted);
            padding: 3px 10px; background: #f4f5f7; border-radius: 6px;
          }
          .card .title { font-size: 19px; line-height: 1.75; font-weight: 700; color: var(--text); }
          .card .content {
            font-size: 18px; line-height: 2.05; color: #3d4350; margin-top: 10px;
          }
          .card .from { margin-top: 16px; font-size: 15px; color: var(--muted); text-align: right; font-style: italic; }
          .empty {
            text-align: center; color: var(--muted); padding: 64px 0; font-size: 14px;
          }
          .empty .icon { font-size: 40px; display: block; margin-bottom: 12px; opacity: .3; }

          /* ===== 页脚 ===== */
          .footer {
            text-align: center; padding: 32px 20px 40px; margin-top: 20px;
          }
          .footer .wave {
            height: 40px; width: 100%; margin-bottom: 20px;
            background: linear-gradient(90deg, transparent, var(--primary), transparent);
            background-size: 200% 100%;
            animation: shimmer 3s linear infinite;
            border-radius: 2px; opacity: .15;
          }
          .footer .text { font-size: 12px; color: var(--muted); line-height: 1.8; }
          .footer .heart { display: inline-block; animation: pulse 1.5s ease-in-out infinite; }

          /* ===== Toast ===== */
          .toast {
            position: fixed; left: 50%; bottom: 28px; transform: translateX(-50%) translateY(20px);
            background: rgba(31,35,41,.95); color: #fff; padding: 12px 22px; border-radius: 12px;
            font-size: 13px; opacity: 0; transition: all .3s; pointer-events: none; z-index: 99;
            box-shadow: 0 8px 24px rgba(0,0,0,.15);
          }
          .toast.show { opacity: 1; transform: translateX(-50%) translateY(0); }

          /* ===== 摸鱼模式 ===== */
          .moyu { background: #fff !important; }
          .moyu .hero, .moyu .footer, .moyu .search-wrap, .moyu .toolbar { display: none !important; }
          .moyu .wrap { max-width: 640px; margin: 40px auto; }
          #moyuView { display: none; }
          .moyu #moyuView { display: block; animation: bounceIn .4s ease; }
          .moyu #newsApp { display: none !important; }
          .moyu .moyu-title {
            font-size: 16px; font-weight: 700; letter-spacing: 1px; text-align: center;
            border-bottom: 2px solid #333; padding-bottom: 10px; margin-bottom: 12px;
          }
          .moyu .moyu-sub { text-align: center; font-size: 12px; color: #666; margin-bottom: 24px; }
          .moyu .moyu-list { font-size: 13px; line-height: 2.2; color: #333; font-family: "SimSun", serif; }
          .moyu .moyu-list li { list-style: decimal inside; padding: 4px 0; }
          .moyu .moyu-tip { margin-top: 28px; text-align: center; font-size: 11px; color: #999; }

          /* ===== 加载骨架 ===== */
          .skeleton {
            background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
            background-size: 200% 100%;
            animation: shimmer 1.5s infinite;
            border-radius: 20px; height: 130px; margin-bottom: 20px;
          }

          /* ===== 换一批按钮加载态 ===== */
          .btn .spin { display: inline-block; }
          .btn.loading .spin { animation: spin .8s linear infinite; }

          @media (max-width: 640px) {
            .hero h1 { font-size: 26px; }
            .hero .subtitle { font-size: 14px; }
            .card { padding: 24px 20px 22px; border-radius: 16px; }
            .card .idx { font-size: 40px; right: 12px; }
            .card .title { font-size: 17px; }
            .card .content { font-size: 17px; }
          }
        </style>
        </head>
        <body>

        <!-- 页头 -->
        <div class="hero">
          <div class="emoji">&#127778;</div>
          <h1>摸鱼一刻</h1>
          <div class="subtitle">上班太累？来点笑话和鸡汤充充电 &#9749;</div>
          <div class="stats">
            <div class="stat"><div class="num" id="statJoke">-</div><div class="label">笑话</div></div>
            <div class="stat"><div class="num" id="statSoup">-</div><div class="label">鸡汤</div></div>
            <div class="stat"><div class="num" id="statTotal">-</div><div class="label">总计</div></div>
          </div>
        </div>

        <div class="wrap">
          <div id="newsApp">
            <div class="toolbar">
              <div class="tabs" id="tabs">
                <button class="tab active" data-source="all">&#127775; 全部</button>
                <button class="tab" data-source="joke">&#128514; 笑话</button>
                <button class="tab" data-source="soup">&#129504; 心灵鸡汤</button>
              </div>
              <div class="actions">
                <button class="btn" id="refreshBtn"><span class="spin">&#128260;</span> 换一批</button>
                <button class="btn primary" id="moyuBtn">&#128373; 摸鱼</button>
              </div>
            </div>

            <div class="search-wrap">
              <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="11" cy="11" r="8"/><path d="m21 21-4.3-4.3"/>
              </svg>
              <input class="search" id="searchInput" placeholder="搜索内容…">
            </div>

            <div class="meta" id="meta"></div>
            <div class="list" id="list">
              <div class="skeleton"></div><div class="skeleton"></div><div class="skeleton"></div>
            </div>
          </div>

          <div id="moyuView">
            <div class="moyu-title">【内部资料】员工关怀与企业文化学习材料</div>
            <div class="moyu-sub">XX科技有限公司 &middot; 人力资源部 &middot; 内部文件</div>
            <ul class="moyu-list" id="moyuList"></ul>
            <div class="moyu-tip">&mdash; 文档内容加密，请勿截图传播 &mdash;</div>
          </div>
        </div>

        <!-- 页脚 -->
        <div class="footer">
          <div class="wave"></div>
          <div class="text">
            数据来源：内置笑话集 &middot; 一言（hitokoto）句子库<br>
            Made with <span class="heart">&#10084;</span> for摸鱼人 &middot; 仅作放松使用，请勿用于商业用途
          </div>
        </div>

        <div class="toast" id="toast"></div>

        <script>
        (function () {
          const BASE = '/apis/api.plugin.halo.run/v1alpha1';
          const BATCH = 3;              /* 每批展示条数 */
          let allItems = [];
          let currentBatch = [];         /* 当前随机展示的条目 */
          let activeSource = 'all';
          let keyword = '';
          const listEl = document.getElementById('list');
          const metaEl = document.getElementById('meta');
          const moyuListEl = document.getElementById('moyuList');
          const toastEl = document.getElementById('toast');

          function esc(s) {
            return String(s == null ? '' : s).replace(/[&<>"']/g, function (c) {
              return { '&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;' }[c];
            });
          }
          function sourceName(s) { return s === 'JOKE' ? '笑话' : '心灵鸡汤'; }
          function sourceCls(s) { return s === 'JOKE' ? 'joke' : 'soup'; }

          function filtered() {
            return allItems.filter(function (it) {
              const hitSource = activeSource === 'all' || (it.source || '').toLowerCase() === activeSource;
              const kw = keyword.trim().toLowerCase();
              const hitKw = !kw || (it.title || '').toLowerCase().includes(kw)
                || (it.summary || '').toLowerCase().includes(kw)
                || (it.category || '').toLowerCase().includes(kw);
              return hitSource && hitKw;
            });
          }

          /* 从候选池中随机抽取一批 */
          function pickBatch() {
            var pool = filtered();
            var arr = pool.slice();
            for (var i = arr.length - 1; i > 0; i--) {
              var j = Math.floor(Math.random() * (i + 1));
              var t = arr[i]; arr[i] = arr[j]; arr[j] = t;
            }
            currentBatch = arr.slice(0, BATCH);
          }

          function updateStats() {
            var jokeCount = 0, soupCount = 0;
            allItems.forEach(function (it) {
              if (it.source === 'JOKE') jokeCount++;
              else if (it.source === 'SOUP') soupCount++;
            });
            document.getElementById('statJoke').textContent = jokeCount;
            document.getElementById('statSoup').textContent = soupCount;
            document.getElementById('statTotal').textContent = allItems.length;
          }

          function render() {
            var pool = filtered();
            metaEl.textContent = pool.length ? ('库存 ' + pool.length + ' 条 · 本批随机 ' + BATCH + ' 条') : '';
            if (!pool.length) {
              currentBatch = [];
              listEl.innerHTML = '<div class="empty"><span class="icon">🐛</span>暂无内容，点击右上角〈换一批〉试试</div>';
              return;
            }
            if (!currentBatch.length) { pickBatch(); }
            var items = currentBatch;
            listEl.innerHTML = items.map(function (it, i) {
              var isJoke = it.source === 'JOKE';
              var full = it.summary || '';
              var cat = it.category ? '<span class="cat">' + esc(it.category) + '</span>' : '';
              var body;
              var trimmedTitle = it.title ? String(it.title).replace(/\u2026$/, '') : '';
              var isPrefix = full && trimmedTitle && full.startsWith(trimmedTitle);
              if (isJoke && isPrefix) {
                body = '<div class="content">' + esc(full) + '</div>';
              } else {
                body = '<div class="title">' + esc(it.title) + '</div>' +
                  (full ? '<div class="from">' + esc(full) + '</div>' : '');
              }
              var delay = i * 0.14;
              var idx = (i + 1 < 10 ? '0' : '') + (i + 1);
              return '<div class="card" style="animation-delay:' + delay + 's">' +
                '<div class="idx">' + idx + '</div>' +
                '<div class="head">' +
                  '<span class="badge ' + sourceCls(it.source) + '">' + sourceName(it.source) + '</span>' + cat +
                '</div>' + body + '</div>';
            }).join('');
          }

          function renderMoyu() {
            var items = currentBatch.length ? currentBatch : allItems.slice(0, 3);
            moyuListEl.innerHTML = items.map(function (it) {
              return '<li>' + esc(it.title) + '</li>';
            }).join('');
          }

          function load() {
            fetch(BASE + '/hotspot-news')
              .then(function (r) { if (!r.ok) throw new Error('HTTP ' + r.status); return r.json(); })
              .then(function (data) {
                allItems = data || [];
                updateStats();
                pickBatch();
                render();
                renderMoyu();
              })
              .catch(function (e) {
                listEl.innerHTML = '<div class="empty"><span class="icon">\uD83D\uDCA9</span>\u52a0\u8f7d\u5931\u8d25\uff1a' + esc(e.message) + '</div>';
                showToast('\u52a0\u8f7d\u5931\u8d25\uff1a' + e.message);
              });
          }

          /* 换一批：旧卡淡出 → 重新随机抽 → 新卡依次弹入 */
          function swapBatch() {
            listEl.classList.add('swapping');
            setTimeout(function () {
              pickBatch();
              render();
              renderMoyu();
              listEl.classList.remove('swapping');
            }, 320);
          }

          /* 换一批：先立即前端随机重抽（所有访客可用，永不报错），
             再静默尝试服务端同步（仅管理员生效，匿名 403 直接降级） */
          function refresh() {
            var btn = document.getElementById('refreshBtn');
            if (btn.disabled) { return; }
            btn.disabled = true;
            btn.classList.add('loading');
            swapBatch();
            fetch(BASE + '/hotspot-news/refresh', { method: 'POST' })
              .then(function (r) {
                if (!r.ok) { return null; }
                return fetch(BASE + '/hotspot-news').then(function (r2) {
                  if (!r2.ok) { return null; }
                  return r2.json();
                });
              })
              .then(function (data) {
                if (data) {
                  allItems = data || [];
                  updateStats();
                  showToast('已同步新一批 ✨');
                }
              })
              .catch(function () { /* 静默降级，不弹错 */ })
              .finally(function () {
                btn.disabled = false;
                btn.classList.remove('loading');
              });
          }

          var toastTimer = null;
          function showToast(msg) {
            toastEl.textContent = msg;
            toastEl.classList.add('show');
            clearTimeout(toastTimer);
            toastTimer = setTimeout(function () { toastEl.classList.remove('show'); }, 2400);
          }

          document.getElementById('tabs').addEventListener('click', function (e) {
            var btn = e.target.closest('.tab');
            if (!btn) return;
            document.querySelectorAll('.tab').forEach(function (t) { t.classList.remove('active'); });
            btn.classList.add('active');
            activeSource = btn.dataset.source;
            pickBatch();
            render();
          });

          document.getElementById('searchInput').addEventListener('input', function (e) {
            keyword = e.target.value;
            pickBatch();
            render();
          });

          document.getElementById('refreshBtn').addEventListener('click', refresh);
          document.getElementById('moyuBtn').addEventListener('click', function () {
            document.body.classList.toggle('moyu');
            var isMoyu = document.body.classList.contains('moyu');
            this.textContent = isMoyu ? '\uD83D\uDC68\u200D\uD83D\uDCBB' : '\uD83D\uDC73 \u6478\u9c7c';
            document.title = isMoyu ? '\u5185\u90e8\u8d44\u6599' : '\u6478\u9c7c\u4e00\u523b \u00b7 \u7b11\u8bdd\u4e0e\u9e21\u6c64';
          });

          load();
        })();
        </script>
        </body>
        </html>
        """;
}