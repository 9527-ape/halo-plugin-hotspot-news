package run.hotspotnews.web;

/**
 * 摸鱼页面（单文件 HTML）。
 *
 * <p>页面通过 {@code /apis/api.news.moyu.run/v1alpha1/news} 拉取聚合数据，
 * 支持来源筛选与“摸鱼模式”（一键伪装为工作文档样式）。</p>
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
        <title>热点摸鱼 · 时政速览</title>
        <style>
          :root {
            --weibo: #e6162d;
            --lianbo: #1e63c8;
            --bg: #f4f5f7;
            --card: #ffffff;
            --text: #1f2329;
            --muted: #8a9099;
            --line: #e6e8eb;
          }
          * { box-sizing: border-box; margin: 0; padding: 0; }
          body { background: var(--bg); color: var(--text); font-family: "PingFang SC", "Microsoft YaHei", sans-serif; }
          .wrap { max-width: 720px; margin: 0 auto; padding: 16px; }
          header { display: flex; align-items: center; justify-content: space-between; padding: 14px 0; }
          header .logo { display: flex; align-items: center; gap: 8px; font-size: 18px; font-weight: 700; }
          header .logo .dot { width: 10px; height: 10px; border-radius: 50%; background: var(--weibo); box-shadow: 0 0 0 4px rgba(230,22,45,.12); }
          .toolbar { display: flex; gap: 8px; }
          .btn { border: 0; border-radius: 8px; padding: 7px 12px; font-size: 13px; cursor: pointer; background: #fff; color: var(--text); box-shadow: 0 1px 2px rgba(0,0,0,.06); }
          .btn.primary { background: var(--weibo); color: #fff; }
          .btn.ghost { background: transparent; box-shadow: none; color: #5f6773; }
          .tabs { display: flex; gap: 6px; margin: 12px 0; }
          .tab { padding: 6px 14px; border-radius: 999px; font-size: 13px; cursor: pointer; background: #e9ebef; color: #5f6773; border: 0; }
          .tab.active { background: var(--text); color: #fff; }
          .search { width: 100%; padding: 10px 14px; border: 1px solid var(--line); border-radius: 10px; font-size: 14px; background: var(--card); outline: none; }
          .search:focus { border-color: #c0c4cc; }
          .meta { font-size: 12px; color: var(--muted); margin: 10px 2px; }
          .list { display: grid; gap: 10px; }
          .card { background: var(--card); border-radius: 12px; padding: 14px 16px; box-shadow: 0 1px 2px rgba(0,0,0,.04); display: flex; gap: 12px; align-items: flex-start; }
          .rank { min-width: 26px; height: 26px; border-radius: 8px; background: #f0f1f3; color: #8a8f99; font-size: 13px; font-weight: 600; display: flex; align-items: center; justify-content: center; margin-top: 2px; }
          .rank.top { background: var(--weibo); color: #fff; }
          .card .body { flex: 1; min-width: 0; }
          .card .title { font-size: 15px; line-height: 1.5; font-weight: 600; color: var(--text); }
          .card .title a { color: inherit; text-decoration: none; }
          .card .title a:hover { color: var(--weibo); }
          .card .foot { margin-top: 6px; display: flex; gap: 8px; align-items: center; font-size: 12px; color: var(--muted); flex-wrap: wrap; }
          .badge { font-size: 11px; padding: 2px 8px; border-radius: 999px; }
          .badge.weibo { background: rgba(230,22,45,.08); color: var(--weibo); }
          .badge.lianbo { background: rgba(30,99,200,.08); color: var(--lianbo); }
          .hot { color: var(--weibo); font-weight: 600; }
          .empty { text-align: center; color: var(--muted); padding: 48px 0; font-size: 14px; }
          .toast { position: fixed; left: 50%; bottom: 24px; transform: translateX(-50%); background: #1f2329; color: #fff; padding: 10px 18px; border-radius: 10px; font-size: 13px; opacity: 0; transition: opacity .3s; pointer-events: none; z-index: 99; }
          .toast.show { opacity: 1; }
          footer { text-align: center; color: var(--muted); font-size: 12px; padding: 24px 0 40px; }
          /* 摸鱼模式：伪装成工作文档 */
          .moyu { background: #fff !important; }
          .moyu .wrap { max-width: 640px; }
          .moyu #newsApp { display: none !important; }
          #moyuView { display: none; }
          .moyu #moyuView { display: block; }
          .moyu .moyu-title { font-size: 16px; font-weight: 700; letter-spacing: 1px; text-align: center; border-bottom: 1px solid #999; padding-bottom: 8px; margin-bottom: 12px; }
          .moyu .moyu-sub { text-align: center; font-size: 12px; color: #666; margin-bottom: 20px; }
          .moyu .moyu-list { font-size: 13px; line-height: 2.1; color: #333; font-family: "SimSun", serif; }
          .moyu .moyu-list li { list-style: decimal inside; }
          .moyu .moyu-tip { margin-top: 24px; text-align: center; font-size: 11px; color: #aaa; }
        </style>
        </head>
        <body>
        <div class="wrap">
          <header id="newsView">
            <div class="logo"><span class="dot"></span>热点摸鱼</div>
            <div class="toolbar">
              <button class="btn" id="refreshBtn">刷新</button>
              <button class="btn" id="moyuBtn">摸鱼模式</button>
            </div>
          </header>

          <div id="newsView">
            <div class="tabs" id="tabs">
              <button class="tab active" data-source="all">全部</button>
              <button class="tab" data-source="weibo">微博热搜</button>
              <button class="tab" data-source="lianbo">新闻联播</button>
            </div>
            <input class="search" id="searchInput" placeholder="搜索关键词…">
            <div class="meta" id="meta"></div>
            <div class="list" id="list"></div>
            <footer>数据来源：微博热搜 / 央视网《新闻联播》 · 仅作个人参考，请勿用于商业用途</footer>
          </div>

          <div id="moyuView">
            <div class="moyu-title">【内部资料】本周舆情信息汇总（勿外传）</div>
            <div class="moyu-sub">XX科技有限公司 · 品牌市场部 · 内部文件</div>
            <ul class="moyu-list" id="moyuList"></ul>
            <div class="moyu-tip">— 文档内容加密，请勿截图传播 —</div>
          </div>
        </div>

        <div class="toast" id="toast"></div>

        <script>
        (function () {
          const BASE = '/apis/api.plugin.halo.run/v1alpha1';
          let allItems = [];
          let activeSource = 'all';
          let keyword = '';
          const listEl = document.getElementById('list');
          const metaEl = document.getElementById('meta');
          const moyuListEl = document.getElementById('moyuList');
          const toastEl = document.getElementById('toast');

          function esc(s) {
            return String(s == null ? '' : s).replace(/[&<>"']/g, function (c) {
              return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c];
            });
          }

          function sourceName(s) { return s === 'weibo' ? '微博热搜' : '新闻联播'; }
          function sourceCls(s) { return s === 'weibo' ? 'weibo' : 'lianbo'; }

          function filtered() {
            return allItems.filter(function (it) {
              const hitSource = activeSource === 'all' || it.source.toLowerCase() === activeSource;
              const kw = keyword.trim().toLowerCase();
              const hitKw = !kw || (it.title || '').toLowerCase().includes(kw) || (it.category || '').toLowerCase().includes(kw);
              return hitSource && hitKw;
            });
          }

          function render() {
            const items = filtered();
            metaEl.textContent = '共 ' + allItems.length + ' 条 · 更新于 ' + (allItems.length ? allItems[0].fetchedAt ? '同步数据' : '' : '等待同步');
            if (!items.length) {
              listEl.innerHTML = '<div class="empty">暂无数据，请稍候或点击右上角刷新</div>';
              return;
            }
            listEl.innerHTML = items.map(function (it, i) {
              const rank = it.rank != null ? it.rank : (i + 1);
              const top = it.source === 'weibo' && rank <= 3;
              const hot = it.hotValue ? '<span class="hot">热度 ' + it.hotValue + '</span>' : '';
              const date = it.publishedDate || (it.fetchedAt || '').slice(0, 10);
              const url = it.url || '#';
              const target = it.url ? ' target="_blank" rel="noopener"' : '';
              return '<div class="card">' +
                '<div class="rank' + (top ? ' top' : '') + '">' + rank + '</div>' +
                '<div class="body">' +
                  '<div class="title"><a href="' + esc(url) + '"' + target + '>' + esc(it.title) + '</a></div>' +
                  '<div class="foot">' +
                    '<span class="badge ' + sourceCls(it.source) + '">' + sourceName(it.source) + '</span>' +
                    (it.category ? '<span>' + esc(it.category) + '</span>' : '') +
                    hot + '<span>' + esc(date) + '</span>' +
                  '</div>' +
                '</div></div>';
            }).join('');
          }

          function renderMoyu() {
            const items = allItems.slice(0, 30);
            moyuListEl.innerHTML = items.map(function (it, i) {
              return '<li>' + (i + 1) + '. ' + esc(it.title) + '</li>';
            }).join('');
          }

          function load() {
            fetch(BASE + '/hotspot-news')
              .then(function (r) { if (!r.ok) throw new Error('HTTP ' + r.status); return r.json(); })
              .then(function (data) { allItems = data || []; render(); renderMoyu(); })
              .catch(function (e) { showToast('加载失败：' + e.message); });
          }

          function refresh() {
            fetch(BASE + '/hotspot-news/refresh', { method: 'POST' })
              .then(function (r) {
                if (r.status === 403) { showToast('无权限，请以管理员身份在控制台刷新'); load(); return; }
                if (!r.ok) throw new Error('HTTP ' + r.status);
                return r.json();
              })
              .then(function (s) {
                showToast('同步完成');
                load();
              })
              .catch(function (e) { showToast('刷新失败：' + e.message); });
          }

          let toastTimer = null;
          function showToast(msg) {
            toastEl.textContent = msg;
            toastEl.classList.add('show');
            clearTimeout(toastTimer);
            toastTimer = setTimeout(function () { toastEl.classList.remove('show'); }, 2200);
          }

          document.getElementById('tabs').addEventListener('click', function (e) {
            const btn = e.target.closest('.tab');
            if (!btn) return;
            document.querySelectorAll('.tab').forEach(function (t) { t.classList.remove('active'); });
            btn.classList.add('active');
            activeSource = btn.dataset.source;
            render();
          });

          document.getElementById('searchInput').addEventListener('input', function (e) {
            keyword = e.target.value;
            render();
          });

          document.getElementById('refreshBtn').addEventListener('click', refresh);
          document.getElementById('moyuBtn').addEventListener('click', function () {
            document.body.classList.toggle('moyu');
            this.textContent = document.body.classList.contains('moyu') ? '退出摸鱼' : '摸鱼模式';
            if (document.body.classList.contains('moyu')) { document.title = '内部资料'; } else { document.title = '热点摸鱼 · 时政速览'; }
          });

          load();
          setInterval(load, 5 * 60 * 1000);
        })();
        </script>
        </body>
        </html>
        """;
}