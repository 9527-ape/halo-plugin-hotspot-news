---
AIGC:
  ContentProducer: '001191110102MAD55U9H0F10002'
  ContentPropagator: '001191110102MAD55U9H0F10002'
  Label: '1'
  ProduceID: '083b1d7b-edfc-4da5-97ee-222191c8123e'
  PropagateID: '083b1d7b-edfc-4da5-97ee-222191c8123e'
  ReservedCode1: '4448b6ad-7344-4be0-8187-fe09faf60a68'
  ReservedCode2: '4448b6ad-7344-4be0-8187-fe09faf60a68'
---

# 摸鱼一刻 · Halo 插件

上班摸鱼专用内容聚合插件：**内置精选笑话集** + **一言（hitokoto）心灵鸡汤**，
定时随机换一批，支持一键伪装成「内部工作文档」，适合日常上班人群休闲放松使用。

- 笑话：插件内置 100 条精选语料（职场 / 生活 / 冷笑话 / 谐音梗 / 校园 / 家庭 / 程序员等分类），离线可用
- 鸡汤：从一言句子库的 jsDelivr CDN 拉取，每次同步随机取文学 / 诗词 / 哲学一类，带出处与作者

## 功能特性

- **双内容源**：内置笑话集（本地，永不失效）+ 一言句子库（在线，随机轮换）
- **定时换一批**：按配置间隔自动刷新内容（默认 30 分钟），管理员可手动换一批
- **持久化**：抓取结果保存为 Halo 自定义模型，重启不丢数据
- **摸鱼页面**：内置单文件 HTML 页面，支持来源筛选、关键词搜索
- **摸鱼模式**：一键把页面伪装成「员工关怀与企业文化学习材料」，领导路过也不慌
- **公开访问**：只读接口对匿名用户开放，游客也能直接看
- **标准规范**：遵循 Halo 插件开发规范（自定义模型 / 自定义 API / 配置表单 / AdditionalWebFilter 扩展点 / DevTools 组件索引）

## 环境要求

- Halo **>= 2.20.0**（Java 17 或 21 运行时均可）
- JDK **17+**（构建插件时使用，Halo 运行环境已自带）

## 快速开始

### 1. 构建插件

```bash
# Windows
gradlew.bat build

# macOS / Linux
./gradlew build
```

构建产物位于 `build/libs/hotspot-news-1.2.0.jar`。

> 首次执行 `./gradlew` 会自动下载 Gradle 发行包。国内网络若下载缓慢，可将
> `gradle/wrapper/gradle-wrapper.properties` 中的 `distributionUrl` 临时替换为
> 华为云镜像：`https://mirrors.huaweicloud.com/gradle/gradle-9.7.1-bin.zip`。
> 依赖下载缓慢时，可在 `build.gradle` 的 `repositories` 中加入阿里云 Maven 镜像。

### 2. 安装插件

1. 进入 Halo 管理后台 → 插件 → 安装
2. 上传 `hotspot-news-1.1.0.jar`
3. 安装完成后启用插件，等待首次同步（约 20 秒后自动开始）

### 3. 使用方式

| 入口 | 地址 |
| --- | --- |
| 摸鱼页面（推荐直接收藏） | `https://你的域名/moyu` |
| 内容聚合 JSON | `https://你的域名/apis/api.plugin.halo.run/v1alpha1/hotspot-news` |
| 笑话 JSON | `https://你的域名/apis/api.plugin.halo.run/v1alpha1/hotspot-news?source=joke` |
| 鸡汤 JSON | `https://你的域名/apis/api.plugin.halo.run/v1alpha1/hotspot-news?source=soup` |
| 同步状态 | `https://你的域名/apis/api.plugin.halo.run/v1alpha1/hotspot-news/status` |

**嵌入主题或文章**：在独立页面 / 文章中加入 iframe 即可：

```html
<iframe src="/moyu" width="100%" height="800" frameborder="0"></iframe>
```

也可以到「外观 → 菜单」把 `https://你的域名/moyu` 添加为导航菜单。

## 配置说明

插件设置页可配置以下项（控制台 → 插件 → 摸鱼一刻 → 设置）：

| 配置项 | 说明 | 默认值 |
| --- | --- | --- |
| 启用笑话 | 是否展示内置笑话集 | 开启 |
| 启用心灵鸡汤 | 是否同步一言句子 | 开启 |
| 自动同步间隔（分钟） | 定时换一批的频率 | 30 |
| 每个来源保留条数 | 每个来源最多保存的条目数 | 50 |
| 一言句子库地址 | 鸡汤数据源 CDN 地址 | `https://cdn.jsdelivr.net/gh/hitokoto-osc/sentences-bundle@master/sentences/` |
| 请求 User-Agent | 抓取外部站点携带的 UA | 浏览器 UA |

> 提示：笑话库为插件内置，无需任何外部服务即可工作；心灵鸡汤依赖 jsDelivr CDN，
> 若网络不通会自动跳过，不影响笑话展示。修改内置笑话可编辑源码
> `src/main/resources/jokes.json` 后重新构建。

## API 与权限

| 接口 | 权限 | 说明 |
| --- | --- | --- |
| `GET /apis/api.plugin.halo.run/v1alpha1/hotspot-news` | 匿名可访问 | 聚合列表，支持 `?source=joke\|soup\|all` |
| `GET /apis/api.plugin.halo.run/v1alpha1/hotspot-news/status` | 匿名可访问 | 同步状态 |
| `GET /moyu` | 匿名可访问 | 摸鱼页面（短链入口） |
| `POST /apis/api.plugin.halo.run/v1alpha1/hotspot-news/refresh` | 需登录（管理员） | 手动换一批 |
| 自定义模型 CRUD `/apis/news.moyu.run/v1alpha1/hotspotnewsitems` | 需登录 | 持久化数据 |

接口使用 Halo 约定的插件公开 API 组 `api.plugin.halo.run`：Halo 内置的匿名角色已对该组放开 GET/list，因此站点访客无需登录即可读取数据。插件同时内置两个可选角色模板：`查看热点新闻`（只读）与`管理热点新闻`（可手动刷新），需要授权其他登录用户时使用。

## 项目结构

```text
halo-plugin-hotspot-news/
├── build.gradle                     # 构建配置（含 Halo DevTools 组件索引生成）
├── settings.gradle
├── gradle.properties
├── gradlew / gradlew.bat
├── README.md
└── src/main/
    ├── resources/
    │   ├── plugin.yaml              # 插件清单
    │   ├── jokes.json               # 内置笑话库（100 条）
    │   └── extensions/
    │       ├── settings.yaml        # 设置表单
    │       ├── 00-roles.yaml        # 角色模板
    │       └── 02-extension-definition.yaml # /moyu 路由的扩展点声明
    └── java/run/hotspotnews/
        ├── HotspotNewsPlugin.java    # 插件入口（注册自定义模型）
        ├── model/HotspotNewsItem.java
        ├── config/PluginSetting.java / WebClientConfig.java
        ├── dto/NewsFeedItem.java
        ├── service/NewsFetcher.java / JokeFetcher.java / SoupFetcher.java
        ├── service/NewsSyncService.java / NewsQueryService.java
        ├── scheduler/NewsSyncScheduler.java
        └── web/NewsEndpoint.java / NewsPage.java / MoyuRouteFilter.java
```

## 开发调试

使用 Halo DevTools（需要 Docker）或传统方式：

```bash
# DevTools 方式
gradlew haloServer
```

更完整的开发方式请参考 [Halo 插件开发文档](https://docs.halo.run/developer-guide/plugin/)。

## 注意事项

- 一言句子库内容版权归 hitokoto 及其提交者所有，本插件仅做聚合展示，请勿用于商业用途。
- 内置笑话为流传较广的民间段子整理，如有不妥内容可自行编辑 `jokes.json` 后重新构建。
- 插件卸载不会自动删除已保存的数据；如需彻底清理，可在控制台删除对应自定义模型数据。
- 本插件仅供学习与个人娱乐使用。

> AI生成